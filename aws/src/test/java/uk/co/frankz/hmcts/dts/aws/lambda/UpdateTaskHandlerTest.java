package uk.co.frankz.hmcts.dts.aws.lambda;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTaskHandlerTest {

    UpdateTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    @Mock
    Mapper mockMapper;

    private static final String TEST_REQUEST = "json request";

    private static final String TEST_RESPONSE = "json response";

    private static final UUID TEST_ID = UUID.fromString("20188092-9ef5-425f-8066-8f698a917002");

    private static final Status TEST_STATUS_BEFORE = Status.Initial;

    private static final Status TEST_STATUS_AFTER = Status.Deleted;

    private static final Map<String, String> TEST_PARM = Map.of(
        Action.PARM.ID, TEST_ID.toString(),
        Action.PARM.STATUS, TEST_STATUS_AFTER.name()
    );

    private TaskWithId testTaskBefore;

    private TaskWithId testTaskAfter;

    @BeforeEach
    void setup() {

        testSubject = new UpdateTaskHandler(mockService, mockMapper);

        testTaskBefore = new TaskWithId();
        testTaskBefore.setUUID(TEST_ID);
        testTaskBefore.setStatus(TEST_STATUS_BEFORE);

        testTaskAfter = new TaskWithId();
        testTaskAfter.setUUID(TEST_ID);
        testTaskAfter.setStatus(TEST_STATUS_AFTER);

        lenient().when(mockMapper.toEntity(anyString())).thenReturn(testTaskBefore);
        lenient().when(mockService.get(any())).thenReturn(testTaskBefore);
        lenient().when(mockService.update(any())).thenReturn(testTaskAfter);
        lenient().when(mockService.update(any(), anyString())).thenReturn(testTaskAfter);
        lenient().when(mockMapper.toJsonString(any(TaskDto.class))).thenReturn(TEST_RESPONSE);
        lenient().when(mockMapper.toJsonString(any(TaskWithId.class))).thenReturn(TEST_RESPONSE);
    }

    @Test
    void shouldInvokeUpdateOnService() throws Exception {
        // given
        Action given = Action.UPDATE;

        // when
        testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).update(any(TaskWithId.class));
    }

    @Test
    void shouldInvokeUpdateStatusOnService() throws Exception {
        // given
        Action given = Action.UPDATE_STATUS;
        String expectedId = TEST_PARM.get(Action.PARM.ID);

        // when
        testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).update(eq(expectedId), anyString());
    }

    @Test
    void shouldUpdate() {
        // given

        // when
        Pair<String, Integer> actual = testSubject.update(TEST_REQUEST);

        // then
        verify(mockService).update(eq(testTaskBefore));
        verify(mockMapper).toJsonString(eq(testTaskAfter));
        assertNotNull(actual);
        assertEquals(200, actual.getRight());
        assertEquals(TEST_RESPONSE, actual.getLeft());
    }

    @Test
    void shouldUpdateStatus() {
        // given
        String givenId = TEST_PARM.get(Action.PARM.ID);
        String givenStatus = TEST_PARM.get(Action.PARM.STATUS);

        // when
        Pair<String, Integer> actual = testSubject.update(TEST_PARM);

        // then
        verify(mockService).update(eq(givenId), eq(givenStatus));
        verify(mockMapper).toJsonString(eq(testTaskAfter));
        assertNotNull(actual);
        assertEquals(200, actual.getRight());
        assertEquals(TEST_RESPONSE, actual.getLeft());
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"UPDATE", "UPDATE_STATUS"}
    )
    void shouldNotInvokeOnService(Action givenOther) {
        // given - parameter

        // when
        TaskException actual = assertThrows(
            TaskNoMatchException.class,
            () -> testSubject.handle(givenOther, TEST_REQUEST, TEST_PARM)
        );

        // then
        verify(mockService, never()).update(anyString(), anyString());
        verify(mockService, never()).update(any(TaskWithId.class));
        assertTrue(actual.getMessage().contains(givenOther.name()));
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        names = {"UPDATE", "UPDATE_STATUS"}
    )
    void shouldPropagateServiceExceptions(Action given) {
        // given
        RuntimeException givenEx = new RuntimeException("test message");
        lenient().when(mockService.update(any(TaskWithId.class))).thenThrow(givenEx);
        lenient().when(mockService.update(anyString(), anyString())).thenThrow(givenEx);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(given, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        names = {"UPDATE", "UPDATE_STATUS"}
    )
    void shouldPropagateJsonExceptions(Action given) {
        // given
        TaskException givenEx = new TaskException("test message");
        when(mockMapper.toJsonString(any(TaskWithId.class))).thenThrow(givenEx);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(given, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

}
