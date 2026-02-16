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
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

import static java.util.Arrays.stream;
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

    private static final String TEST_REQUEST = "";

    private static final Map<String, String> TEST_PARM = Map.of(
        Action.PARM.ID, "123",
        Action.PARM.STATUS, "Deleted"
    );

    @BeforeEach
    void setup() {

        testSubject = new UpdateTaskHandler(mockService, mockMapper);

        lenient().when(mockMapper.toEntity(anyString())).thenReturn(new TaskWithId());
        lenient().when(mockMapper.toDto(any(TaskWithId.class))).thenReturn(new TaskDto());
        lenient().when(mockMapper.toJsonString(any(TaskDto.class))).thenReturn("");
        lenient().when(mockMapper.toJsonString(any(TaskWithId.class))).thenReturn("");
        lenient().when(mockService.createTask(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.get(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.update(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.update(any(), anyString())).thenReturn(new TaskWithId());
        lenient().when(mockService.getAll()).thenReturn(stream(new TaskWithId[]{new TaskWithId()}));
    }

    @Test
    void shouldInvokeUpdateOnService() throws Exception {
        // given
        Action given = Action.UPDATE;

        // when
        Pair<String, Integer> actual = testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).update(any(TaskWithId.class));
    }

    @Test
    void shouldInvokeUpdateStatusOnService() throws Exception {
        // given
        Action given = Action.UPDATE_STATUS;
        String expectedId = TEST_PARM.get(Action.PARM.ID);

        // when
        Pair<String, Integer> actual = testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).update(eq(expectedId), anyString());
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"UPDATE", "UPDATE_STATUS"}
    )
    void shouldNotInvokeOnService(Action givenOther) throws Exception {
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
        when(mockMapper.toDto(any(TaskWithId.class))).thenThrow(givenEx);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(given, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

}
