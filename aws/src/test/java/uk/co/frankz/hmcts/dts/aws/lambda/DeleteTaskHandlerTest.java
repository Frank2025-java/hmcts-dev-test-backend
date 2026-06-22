package uk.co.frankz.hmcts.dts.aws.lambda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.aws.http.IdemPotencyScopeKeyBuilder;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DeleteTaskHandlerTest {

    DeleteTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    @Mock
    IdemPotencyScopeKeyBuilder mockIdemPotency;

    @Mock
    IdemPotencyStore mockIdemPotencyStore;

    @Mock
    Mapper mockMapper;

    private static final Action TEST_ACTION = Action.DELETE;

    private static final String TEST_REQUEST = "";

    private static final Map<String, String> TEST_PARM = Map.of(Action.PARM.ID, "123");

    @BeforeEach
    void setup() {

        testSubject = new DeleteTaskHandler(mockService, mockMapper, mockIdemPotency, mockIdemPotencyStore);

        lenient().when(mockIdemPotency.build(any())).thenReturn(Optional.empty());
        lenient().when(mockIdemPotencyStore.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void shouldInvokeDeleteOnService() throws Exception {
        // given
        String givenId = TEST_PARM.get(Action.PARM.ID);

        // when
        testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).delete(eq(givenId));
    }

    @Test
    void shouldPropagateServiceExceptions() {
        // given
        RuntimeException givenEx = new RuntimeException("test message");
        doThrow(givenEx).when(mockService).delete(anyString());

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
