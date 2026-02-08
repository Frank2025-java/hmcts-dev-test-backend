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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrieveTaskHandlerTest {

    RetrieveTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    @Mock
    Mapper mockMapper;

    private static final String TEST_REQUEST = "";

    private static final Map<String, String> TEST_PARM = Map.of(Action.PARM.ID, "123");

    @BeforeEach
    void setup() {

        testSubject = new RetrieveTaskHandler(mockService, mockMapper);

        Stream<TaskWithId> taskStream = Arrays.stream(new TaskWithId[]{new TaskWithId()});

        lenient().when(mockMapper.toEntity(anyString())).thenReturn(new TaskWithId());
        lenient().when(mockMapper.toDto(any(TaskWithId.class))).thenReturn(new TaskDto());
        lenient().when(mockMapper.toDto(eq(taskStream))).thenReturn(new TaskDto[]{new TaskDto()});
        lenient().when(mockMapper.toJsonString(any(TaskDto.class))).thenReturn("");
        lenient().when(mockMapper.toJsonString(any(TaskWithId.class))).thenReturn("");
        lenient().when(mockService.createTask(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.get(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.update(any())).thenReturn(new TaskWithId());
        lenient().when(mockService.update(any(), anyString())).thenReturn(new TaskWithId());
        lenient().when(mockService.getAll()).thenReturn(taskStream);
    }

    @Test
    void shouldInvokeGetOnService() throws Exception {
        // given
        Action given = Action.GET;
        String expectedId = TEST_PARM.get(Action.PARM.ID);

        // when
        Pair<String, Integer> actual = testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).get(eq(expectedId));
    }

    @Test
    void shouldInvokeScanOnService() throws Exception {
        // given
        Action given = Action.GET_ALL;

        // when
        Pair<String, Integer> actual = testSubject.handle(given, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).getAll();
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"GET", "GET_ALL"}
    )
    void shouldNotInvokeOnService(Action givenOther) throws Exception {
        // given - parameter

        // when
        TaskException actual = assertThrows(
            TaskNoMatchException.class,
            () -> testSubject.handle(givenOther, TEST_REQUEST, TEST_PARM)
        );

        // then
        verify(mockService, never()).get(anyString());
        verify(mockService, never()).getAll();
    }

    @ParameterizedTest
    @EnumSource(
        value = Action.class,
        names = {"GET", "GET_ALL"}
    )
    void shouldPropagateServiceExceptions(Action given) {
        // given
        RuntimeException givenEx = new RuntimeException("test message");
        lenient().when(mockService.get(anyString())).thenThrow(givenEx);
        lenient().when(mockService.getAll()).thenThrow(givenEx);

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
        names = {"GET", "GET_ALL"}
    )
    void shouldPropagateJsonExceptions(Action given) {
        // given
        TaskException givenEx = new TaskException("test" + given.name());
        lenient().when(mockMapper.toJsonString(any(TaskDto.class))).thenThrow(givenEx);
        lenient().when(mockMapper.toJsonString(any(TaskDto[].class))).thenThrow(givenEx);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(given, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

}
