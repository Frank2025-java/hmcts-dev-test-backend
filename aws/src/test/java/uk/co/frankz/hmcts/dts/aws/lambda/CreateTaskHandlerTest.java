package uk.co.frankz.hmcts.dts.aws.lambda;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskHandlerTest {

    CreateTaskHandler testSubject;

    @Mock
    TaskService<TaskWithId> mockService;

    @Mock
    Mapper mockMapper;

    private static final Action TEST_ACTION = Action.CREATE;

    private static final String TEST_REQUEST = "";

    private static final Map<String, String> TEST_PARM = null;

    @BeforeEach
    void setup() {

        testSubject = new CreateTaskHandler(mockService, mockMapper);

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
    void shouldInvokeCreateOnService() throws Exception {
        // given
        String given = " some json";
        when(mockMapper.toJsonString(any(TaskWithId.class))).thenReturn(given);

        // when
        Pair<String, Integer> actual = testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM);

        // then
        verify(mockService).createTask(any(TaskWithId.class));
        assertEquals(given, actual.getLeft());
        assertEquals(201, actual.getRight());
    }

    @Test
    void shouldPropagateServiceExceptions() {
        // given
        RuntimeException givenEx = new RuntimeException("test message");
        when(mockService.createTask(any())).thenThrow(givenEx);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(givenEx, actual);
    }

    @Test
    void shouldPropagateJsonExceptions() {
        // given
        TaskException given = new TaskException("test message");
        when(mockMapper.toEntity(anyString())).thenThrow(given);

        // when
        Exception actual = assertThrows(
            Exception.class,
            () -> testSubject.handle(TEST_ACTION, TEST_REQUEST, TEST_PARM)
        );

        // then
        assertSame(given, actual);
    }

}
