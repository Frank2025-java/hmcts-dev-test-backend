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
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class RetrieveTaskControllerTest {

    RetrieveTaskController testSubject;

    @Mock
    TaskService mockService;

    @Mock
    Mapper mockMapper;

    @Captor
    ArgumentCaptor<TaskWithId> taskCaptor;

    final String testId = "123";
    final TaskWithId testResultEntity = new TaskWithId();
    final Stream<TaskWithId> testResultEntities = Arrays.asList(new TaskWithId(), new TaskWithId()).stream();
    final TaskDto testResultDto = new TaskDto();
    final TaskDto[] testResultDtos = Arrays.asList(new TaskDto(), new TaskDto()).toArray(TaskDto[]::new);

    @BeforeEach
    void setup() {
        testSubject = new RetrieveTaskController(mockService, mockMapper);

        testResultDto.setId(testId);
        testResultEntity.setId(testId);
    }

    @Test
    void shouldRetrieveEntityById() {

        // given
        String given = testId;
        when(mockService.get(any())).thenReturn(testResultEntity);
        when(mockMapper.toDto((TaskWithId) any())).thenReturn(testResultDto);

        // when
        ResponseEntity<TaskDto> actual = testSubject.getTask(given);

        // then
        assertEquals(OK, actual.getStatusCode());
        verify(mockService).get(eq(given));
        assertEquals(given, actual.getBody().getId());
    }

    @Test
    void shouldPropagateServiceErrorOnGetTask() {

        // given
        String given = testId;
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockService.get(any())).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.getTask(given));

        // then
        verify(mockService).get(eq(given));
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldRetrieveAllEntities() {

        // given
        when(mockService.getAll()).thenReturn(testResultEntities);
        Stream<? extends Task> anyTaskStream = any();
        when(mockMapper.toDto(anyTaskStream)).thenReturn(testResultDtos);

        // when
        ResponseEntity<TaskDto[]> actual = testSubject.getAllTasks();

        // then
        assertEquals(OK, actual.getStatusCode());
        verify(mockService).getAll();
        assertSame(testResultDtos, actual.getBody());
    }

    @Test
    void shouldPropagateServiceErrorOnGetAll() {

        // given
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockService.getAll()).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.getAllTasks());

        // then
        verify(mockService).getAll();
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldHandleMapperDtoErrorOnGetAll() {

        // given
        when(mockService.getAll()).thenReturn(testResultEntities);

        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        Stream<? extends Task> streamAny = any();
        when(mockMapper.toDto(streamAny)).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.getAllTasks());

        // then
        verify(mockService).getAll();
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

}
