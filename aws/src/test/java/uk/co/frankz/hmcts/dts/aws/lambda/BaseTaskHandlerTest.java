package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseTaskHandlerTest {

    class TestBaseTaskHandler extends BaseTaskHandler {

        private final Supplier<Pair<String, Integer>> handleResultProvider;

        public TestBaseTaskHandler(TaskService<TaskWithId> service, Mapper json,
                                   Supplier<Pair<String, Integer>> mockResult) {
            super(service, json);
            this.handleResultProvider = mockResult;
        }

        @Override
        protected Pair<String, Integer> handle(Action action, String requestBody, Map<String, String> pathParams)
            throws Exception {
            return handleResultProvider.get();
        }
    }

    BaseTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    static Mapper realMapper = new Mapper();

    @Mock
    Context mockContext;

    @Mock
    LambdaLogger mockLogger;

    @Mock
    Supplier<Pair<String, Integer>> mockResult;

    APIGatewayV2HTTPEvent testEvent = event("CREATE", Action.PATH.CREATE, null, null);

    @BeforeEach
    void setup() {

        testSubject = new TestBaseTaskHandler(mockService, realMapper, mockResult);

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
        when(mockResult.get()).thenReturn(Pair.of(givenResultBody, givenStatus));
        Action expectedAction = Action.fromPath(givenEvent.getRawPath());
        String expectedBody = givenEvent.getBody();

        BaseTaskHandler spyTestSubject = spy(testSubject);

        // when
        var response = spyTestSubject.handleRequest(givenEvent, mockContext);

        // then
        verify(spyTestSubject).handle(same(expectedAction), eq(expectedBody), any());
        assertEquals(givenStatus, response.getStatusCode());
        assertEquals(givenResultBody, response.getBody());
    }

    @Test
    void shouldThrowNiceErrorWithUnexpectedEvent() throws Exception {
        // given
        String givenWrongPath = "/taskssss";
        APIGatewayV2HTTPEvent givenEvent = event("GET", givenWrongPath, null, null);
        int expectedStatus = 400;

        // when
        var response = testSubject.handleRequest(givenEvent, mockContext);

        // then
        assertEquals(expectedStatus, response.getStatusCode());
        assertTrue(response.getBody().contains(givenWrongPath));
    }

    @Test
    void shouldWrapHandleException() throws Exception {
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

}
