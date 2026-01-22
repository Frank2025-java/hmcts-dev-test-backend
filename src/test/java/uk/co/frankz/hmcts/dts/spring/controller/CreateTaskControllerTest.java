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
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;

@ExtendWith(MockitoExtension.class)
class CreateTaskControllerTest {

    CreateTaskController testSubject;

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

        testSubject = new CreateTaskController(mockService, mockMapper);

        testResultDto.setId(testId);
        testResultEntity.setId(testId);
    }

    @Test
    void shouldStoreEntityFromDtoOnCreate() {

        // given
        TaskDto given = new TaskDto();
        when(mockMapper.toEntity(any())).thenReturn(testInputEntity);
        when(mockService.createTask(any())).thenReturn(testResultEntity);
        when(mockMapper.toDto((TaskWithId) any())).thenReturn(testResultDto);

        // when
        ResponseEntity<TaskDto> actual = testSubject.createTask(given);

        // then
        assertEquals(CREATED, actual.getStatusCode());
        verify(mockService).createTask(taskCaptor.capture());
        assertSame(testInputEntity, taskCaptor.getValue());
        assertEquals(testId, actual.getBody().getId());
    }

    @Test
    void shouldPropagateServiceError() {

        // given
        TaskDto given = new TaskDto();
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        when(mockService.createTask(any())).thenThrow(expectedEx);

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.createTask(given));

        // then
        verify(mockService).createTask(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

}
