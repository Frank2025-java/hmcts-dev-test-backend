package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RootTaskHandlerTest {

    RootTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    @Mock
    Mapper mockMapper;

    @Mock
    APIGatewayV2HTTPEvent mockEvent;

    @Mock
    Context mockContext;

    private static final Action TEST_ACTION = Action.ROOT;

    private static final String TEST_REQUEST = "";

    private static final Map<String, String> TEST_PARM = null;

    @BeforeEach
    void setup() {

        testSubject = new RootTaskHandler(mockService, mockMapper);
    }

    @Test
    void shouldInvokeHealthCheckOnService() throws Exception {
        // given

        // when
        Pair<String, Integer> actual = testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).healthCheck();
    }

    @Test
    void shouldReturnHtml() throws Exception {
        // given
        Map<String, String> expectedFormat = Header.HTML;

        // when
        APIGatewayV2HTTPResponse actual = testSubject.handleRequest(mockEvent, mockContext);

        // then
        verify(mockService).healthCheck();
        assertEquals(expectedFormat, actual.getHeaders());
    }

    @Test
    void shouldReturnHtmlOnError() throws Exception {
        // given
        Map<String, String> expectedFormat = Header.HTML;
        String given = "test message";
        doThrow(new RuntimeException(given)).when(mockService).healthCheck();

        // when
        APIGatewayV2HTTPResponse actual = testSubject.handleRequest(mockEvent, mockContext);

        // then
        verify(mockService).healthCheck();
        assertEquals(expectedFormat, actual.getHeaders());
        assertEquals(500, actual.getStatusCode());
        assertTrue(actual.getBody().contains(given));
    }

    @Test
    void shouldPropagateServiceExceptions() {
        // given
        RuntimeException givenEx = new RuntimeException("test message");
        doThrow(givenEx).when(mockService).healthCheck();

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

    @Test
    void shouldNotCallJson() throws Exception {
        // given

        // when
        testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM);

        // then
        verifyNoInteractions(mockMapper);
    }

}
