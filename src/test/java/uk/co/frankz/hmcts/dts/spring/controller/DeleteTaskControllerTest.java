package uk.co.frankz.hmcts.dts.spring.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@ExtendWith(MockitoExtension.class)
class DeleteTaskControllerTest {

    DeleteTaskController testSubject;

    @Mock
    TaskService mockService;

    @Captor
    ArgumentCaptor<TaskWithId> taskCaptor;

    final String testId = "123";

    @BeforeEach
    void setup() {
        testSubject = new DeleteTaskController(mockService);
    }

    @Test
    void shouldDeleteEntityById() {

        // given
        String givenId = testId;

        // when
        ResponseEntity<Void> actual = testSubject.deleteTask(givenId);

        // then
        assertEquals(NO_CONTENT, actual.getStatusCode());
        verify(mockService).delete(eq(givenId));
    }

    @Test
    void shouldPropagateServiceError() {

        // given
        String given = testId;
        String expectedMsg = "Hi there";
        Exception expectedEx = new TaskException(expectedMsg);
        doThrow(expectedEx).when(mockService).delete(any());

        // when, then exception which will be handled by Spring
        Exception actual = assertThrows(TaskException.class, () -> testSubject.deleteTask(given));

        // then
        verify(mockService).delete(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

}
