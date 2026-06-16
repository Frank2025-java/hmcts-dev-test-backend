package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.aws.http.IdemPotencyScopeKeyBuilder;
import uk.co.frankz.hmcts.dts.aws.http.ResponseFields;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseTaskHandlerTest {

    static class TestBaseTaskHandler extends BaseTaskHandler {

        private final Supplier<ResponseFields> handleResultProvider;

        public TestBaseTaskHandler(TaskService<TaskWithId> service, Mapper json,
                                   IdemPotencyScopeKeyBuilder idemPotency,
                                   IdemPotencyStore idemPotencyStore,
                                   Supplier<ResponseFields> mockResult) {
            super(service, json, idemPotency, idemPotencyStore);
            this.handleResultProvider = mockResult;
        }

        @Override
        protected ResponseFields handle(Action action, String requestBody, Map<String, String> pathParams) {
            return handleResultProvider.get();
        }
    }

    BaseTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    static Mapper realMapper = new Mapper();

    @Mock
    IdemPotencyScopeKeyBuilder mockIdemPotency;

    @Mock
    IdemPotencyStore mockIdemPotencyStore;

    @Mock
    Context mockContext;

    @Mock
    LambdaLogger mockLogger;

    @Mock
    Supplier<ResponseFields> mockResult;

    APIGatewayV2HTTPEvent testEvent = event("CREATE", Action.PATH.CREATE, null, null);

    @Captor
    ArgumentCaptor<IdemPotencyRecord> captorIdemPotencyRecord;

    @BeforeEach
    void setup() {

        testSubject = new TestBaseTaskHandler(
            mockService,
            realMapper,
            mockIdemPotency,
            mockIdemPotencyStore,
            mockResult
        );

        lenient().when(mockIdemPotency.build(any())).thenReturn(Optional.empty());
        lenient().when(mockIdemPotencyStore.findById(any())).thenReturn(Optional.empty());

        when(mockContext.getLogger()).thenReturn(mockLogger);
    }

    static Stream<APIGatewayV2HTTPEvent> expectedEventProvider() {

        Map<String, String> mappedId = Map.of(Action.PARM.ID, "123");
        Map<String, String> mappedIdStatus = Map.of(Action.PARM.ID, "123", Action.PARM.STATUS, "Deleted");

        return Stream.of(
            event("GET", Action.PATH.GET_ALL, null, null),
            event("GET", Action.PATH.ROOT, null, null),
            event("DELETE", Action.PATH.DELETE, mappedId, null),
            event("GET", Action.PATH.GET, mappedId, null),
            event("PUT", Action.PATH.UPDATE_STATUS, mappedIdStatus, null),
            event("PUT", Action.PATH.UPDATE, null, new TaskDto()),
            event("POST", Action.PATH.CREATE, null, new TaskDto())
        );
    }

    private static APIGatewayV2HTTPEvent event(
        String method,
        String path,
        Map<String, String> queryParams,
        TaskDto dto) {

        APIGatewayV2HTTPEvent.RequestContext request =
            APIGatewayV2HTTPEvent.RequestContext.builder()
                .withHttp(
                    APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                        .withMethod(method)
                        .withPath(path)
                        .build())
                .build();

        return APIGatewayV2HTTPEvent.builder()
            .withRawPath(path)
            .withRouteKey(method + " " + path)
            .withVersion("2.0")
            .withRawQueryString(buildQueryString(queryParams))
            .withQueryStringParameters(queryParams)
            .withRequestContext(request)
            .withHeaders(Header.JSON)
            .withBody(dto == null ? "" : realMapper.toJsonString(dto))
            .build();
    }

    private static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        } else {
            return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
        }
    }

    @ParameterizedTest
    @MethodSource("expectedEventProvider")
    void shouldWorkWithExpectedEvents(APIGatewayV2HTTPEvent givenEvent) throws Exception {
        // given - argument
        String givenResultBody = "{\"body\"=\"vlaue\"}";
        int givenStatus = 203;
        when(mockResult.get()).thenReturn(new ResponseFields(givenResultBody, givenStatus, Header.JSON));
        Action expectedAction = Action.fromPath(givenEvent.getRawPath());
        String expectedBody = givenEvent.getBody();

        BaseTaskHandler spyTestSubject = spy(testSubject);

        // when
        var response = spyTestSubject.handleRequest(givenEvent, mockContext);

        // then
        verify(spyTestSubject).handle(same(expectedAction), eq(expectedBody), any());
        assertEquals(givenStatus, response.getStatusCode());
        assertEquals(givenResultBody, response.getBody());
        verify(mockIdemPotency).build(givenEvent);
    }

    @Test
    void shouldThrowNiceErrorWithUnexpectedEvent() {
        // given
        String givenWrongPath = "/taskssss";
        APIGatewayV2HTTPEvent givenEvent = event("GET", givenWrongPath, null, null);
        int expectedStatus = 400;

        // when
        var response = testSubject.handleRequest(givenEvent, mockContext);

        // then
        assertEquals(expectedStatus, response.getStatusCode());
        assertTrue(response.getBody().contains(givenWrongPath));
        verify(mockIdemPotency).build(givenEvent);
    }

    @Test
    void shouldWrapHandleException() {
        // given
        String givenEx = "test message";
        when(mockResult.get()).thenThrow(new RuntimeException(givenEx));

        int expectedStatus = 500;

        // when
        var response = testSubject.handleRequest(testEvent, mockContext);

        // then
        assertEquals(expectedStatus, response.getStatusCode());
        assertTrue(response.getBody().contains(givenEx));
    }

    @ParameterizedTest
    @MethodSource("expectedEventProvider")
    void shouldStoreIdemPotencyResponse(APIGatewayV2HTTPEvent givenEvent) throws Exception {
        // given
        final ResponseFields givenResponse = new ResponseFields("{\"body\"=\"vlaue\"}", 203, Header.JSON);
        when(mockResult.get()).thenReturn(givenResponse);
        final IdemPotencyHash expectedRequestHash = testSubject.getRequestBodyHash(givenEvent);

        final IdemPotencyScopeKey givenKey = new IdemPotencyScopeKey("m", "p", "1234");
        when(mockIdemPotency.build(any())).thenReturn(Optional.of(givenKey));
        final IdemPotencyHash expectedKeyHash = givenKey.sha256();

        // when
        final var response = testSubject.handleRequest(givenEvent, mockContext);

        // then
        final int responseStatus = response.getStatusCode();
        final byte[] responseBody = response.getBody().getBytes(UTF_8);
        final String responseContentType = response.getHeaders().get(Header.CONTENT_TYPE);

        verify(mockIdemPotency).build(givenEvent);
        verify(mockIdemPotencyStore).saveSafe(captorIdemPotencyRecord.capture());
        final var actualRecord = captorIdemPotencyRecord.getValue();
        assertEquals(expectedKeyHash, actualRecord.key());
        assertEquals(expectedRequestHash, actualRecord.requestHash());
        assertEquals(new String(responseBody, UTF_8), new String(actualRecord.responseBody(), UTF_8));
        assertEquals(responseStatus, actualRecord.statusCode());
        assertEquals(responseContentType, actualRecord.contentType());
    }

    @ParameterizedTest
    @MethodSource("expectedEventProvider")
    void shouldReturnStoredIdemPotencyResponse(APIGatewayV2HTTPEvent givenEvent) throws Exception {
        // given
        final IdemPotencyHash givenRequestHash = testSubject.getRequestBodyHash(givenEvent);
        final String givenResponseBody = "{\"body\"=\"vlaue\"}";
        final int givenStatus = 203;
        final String givenContent = "my content type";

        final var givenKey = new IdemPotencyScopeKey("m", "p", "1234");
        final var givenStored = new IdemPotencyRecord(
            givenKey.sha256(),
            LocalDateTime.of(2026, 6, 15, 17, 51, 33, 12345),
            givenRequestHash,
            givenResponseBody.getBytes(UTF_8),
            givenStatus,
            givenContent
        );

        when(mockIdemPotency.build(any())).thenReturn(Optional.of(givenKey));
        when(mockIdemPotencyStore.findById(any())).thenReturn(Optional.of(givenStored));

        // when
        final var actualResponse = testSubject.handleRequest(givenEvent, mockContext);

        // then
        verify(mockIdemPotency).build(givenEvent);
        verify(mockResult, never()).get();
        verify(mockIdemPotencyStore).findById(givenKey);
        verify(mockIdemPotencyStore, never()).saveSafe(captorIdemPotencyRecord.capture());

        assertEquals(givenStatus, actualResponse.getStatusCode());
        assertEquals(givenResponseBody, actualResponse.getBody());
        assertEquals(givenContent, actualResponse.getHeaders().get(Header.CONTENT_TYPE));
    }

    @ParameterizedTest
    @MethodSource("expectedEventProvider")
    void shouldThrowIdemPotencyExceptionOnDifferentRequest() throws IdemPotencyAlreadyProcessedException {
        // given
        final var givenEvent = event("GET", Action.PATH.GET, Map.of(Action.PARM.ID, "123"), null);
        final var givenDifferentRequestHash = IdemPotencyHash.sha256Hex("different request".getBytes(UTF_8));
        final IdemPotencyScopeKey givenKey = new IdemPotencyScopeKey("m", "p", "1234");
        final IdemPotencyRecord givenStored = new IdemPotencyRecord(
            givenKey.sha256(),
            LocalDateTime.of(2026, 6, 15, 17, 51, 33, 12345),
            givenDifferentRequestHash,
            "xxxxxx".getBytes(UTF_8),
            203,
            "my content"
        );

        when(mockIdemPotency.build(any())).thenReturn(Optional.of(givenKey));
        when(mockIdemPotencyStore.findById(any())).thenReturn(Optional.of(givenStored));

        // when
        final var actualResponse = testSubject.handleRequest(givenEvent, mockContext);

        // then
        verify(mockIdemPotency).build(givenEvent);
        verify(mockResult, never()).get();
        verify(mockIdemPotencyStore).findById(givenKey);
        verify(mockIdemPotencyStore, never()).saveSafe(any());

        // IdemPotencyMismatchException
        assertEquals(409, actualResponse.getStatusCode());
    }
}
