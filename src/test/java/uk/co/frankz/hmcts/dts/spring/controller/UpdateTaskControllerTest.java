package uk.co.frankz.hmcts.dts.spring.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class UpdateTaskControllerTest {

    UpdateTaskController testSubject;

    @Mock
    TaskService mockService;

    @Mock
    Mapper mockMapper;

    @Captor
    ArgumentCaptor<TaskWithId> taskCaptor;

    final String testId = "123";
    final TaskWithId testInputEntity = new TaskWithId();
    final TaskWithId testResultEntity = new TaskWithId();
    final TaskDto testResultDto = new TaskDto();

    @BeforeEach
    void setup() {
        testSubject = new UpdateTaskController(mockService, mockMapper);

        testResultDto.setId(testId);
        testResultEntity.setId(testId);
    }

    @Test
    void shouldUpdateEntityFromDtoOnUpdateTaskById() {

        // given
        Status givenStatus = Status.Initial;

        when(mockService.update(any(), any())).thenReturn(testResultEntity);
        when(mockMapper.toDto((TaskWithId) any())).thenReturn(testResultDto);

        // when
        ResponseEntity<TaskDto> actual = testSubject.updateTaskStatus(testId, givenStatus.name());

        // then
        assertEquals(OK, actual.getStatusCode());
        verify(mockService).update(eq(testId), eq(givenStatus.name()));
        assertSame(testResultDto, actual.getBody());
    }

    @Test
    void shouldUpdateEntityFromDtoOnUpdateTask() {

        // given
        TaskDto given = new TaskDto();
        given.setId(testId);
        when(mockMapper.toEntity(any())).thenReturn(testInputEntity);
        when(mockService.update(any())).thenReturn(testResultEntity);
        when(mockMapper.toDto((TaskWithId) any())).thenReturn(testResultDto);

        // when
        ResponseEntity<TaskDto> actual = testSubject.updateTask(given);

        // then
        assertEquals(OK, actual.getStatusCode());
        verify(mockService).update(taskCaptor.capture());
        assertSame(testInputEntity, taskCaptor.getValue());
        assertSame(testResultDto, actual.getBody());
    }

    @Test
    void shouldPropagateConvertErrorOnUpdateById() {

        // given
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockMapper.toDto((TaskWithId) any())).thenThrow(expectedEx);
        when(mockService.update(any(), any())).thenReturn(new TaskWithId());

        String givenStatus = Status.Initial.name();

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.updateTaskStatus(testId, givenStatus));

        // then
        verify(mockService).update(eq(testId), eq(givenStatus));
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldPropagateConvertErrorOnUpdateTask() {

        // given
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockMapper.toEntity(any())).thenThrow(expectedEx);

        TaskDto given = new TaskDto();
        given.setId(testId);
        given.setStatus("not a status");

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.updateTask(given));

        // then
        verify(mockService, never()).update(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldPropagateServiceErrorOnUpdateStatusById() {

        // given
        String givenStatus = Status.Initial.name();
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockService.update(any(), any())).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.updateTaskStatus(testId, givenStatus));

        // then
        verify(mockService).update(eq(testId), eq(givenStatus));
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldPropagateServiceErrorOnUpdateTask() {

        // given
        TaskDto given = new TaskDto();
        given.setId(testId);

        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockMapper.toEntity(any())).thenReturn(testInputEntity);
        when(mockService.update(any())).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.updateTask(given));

        // then
        verify(mockService).update(taskCaptor.capture());
        assertSame(testInputEntity, taskCaptor.getValue());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

}
