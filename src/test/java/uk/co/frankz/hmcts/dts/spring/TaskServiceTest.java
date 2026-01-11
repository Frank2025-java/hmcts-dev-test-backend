package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNotFoundException;
import uk.co.frankz.hmcts.dts.model.exception.TaskStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    TaskService testSubject;

    @Mock
    TaskStore mockTaskStore;

    @Captor
    ArgumentCaptor<TaskWithId> taskCaptor;

    @BeforeEach
    void setup() {
        testSubject = new TaskService(mockTaskStore);
    }

    @Test
    void shouldStoreEntity() {

        // given
        TaskWithId given = new TaskWithId();
        TaskWithId mockStored = new TaskWithId();
        when(mockTaskStore.save(any())).thenReturn(mockStored);

        // when
        Task actual = testSubject.createTask(given);

        // then
        verify(mockTaskStore).save(taskCaptor.capture());
        assertSame(given, taskCaptor.getValue());
    }

    @Test
    void shouldExceptCreateTaskOnNull() {

        // given
        TaskWithId given = null;

        // when
        Exception actual = assertThrows(TaskInvalidArgumentException.class, () -> testSubject.createTask(given));

        // then
        verify(mockTaskStore, never()).save(any());
    }

    @Test
    void shouldExceptCreateTaskOnExistingTask() {

        // given
        TaskWithId given = mock(TaskWithId.class);
        when(given.isNew()).thenReturn(Boolean.FALSE);

        // when
        Exception actual = assertThrows(TaskInvalidArgumentException.class, () -> testSubject.createTask(given));

        // then
        verify(mockTaskStore, never()).save(any());
    }

    @Test
    void shouldPropagateStoreExceptionOnCreateTask() {

        // given
        TaskWithId given = new TaskWithId();
        String expectedMsg = "hello";
        when(mockTaskStore.save(any())).thenThrow(new RuntimeException(expectedMsg));

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.createTask(given));

        // then
        verify(mockTaskStore).save(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldFindEntityById() {

        // given
        String given = "1234";
        Optional<TaskWithId> mockStored = Optional.of(new TaskWithId());
        when(mockTaskStore.findById(any())).thenReturn(mockStored);

        // when
        Task actual = testSubject.get(given);

        // then
        verify(mockTaskStore).findById(eq(given));
        assertSame(mockStored.get(), actual);
    }

    @Test
    void shouldExceptOnNotFoundById() {

        // given
        String given = "1234";
        Optional<TaskWithId> mockStored = Optional.empty();
        when(mockTaskStore.findById(any())).thenReturn(mockStored);

        // when
        Exception actual = assertThrows(TaskNotFoundException.class, () -> testSubject.get(given));

        // then
        verify(mockTaskStore).findById(any());
        assertTrue(actual.getMessage().contains(given));
    }

    @Test
    void shouldPropagateStoreExceptionOnFindById() {

        // given
        String given = "1234";
        String expectedMsg = "hello";
        Exception expectedEx = new RuntimeException(expectedMsg);
        when(mockTaskStore.findById(any())).thenThrow(expectedEx);

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.get(given));

        // then
        verify(mockTaskStore).findById(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldFindAllEntities() {

        // given
        List<TaskWithId> given = Arrays.asList(new TaskWithId(), new TaskWithId());
        when(mockTaskStore.findAll()).thenReturn(given);

        // when
        Stream<TaskWithId> actual = testSubject.getAll();

        // then
        verify(mockTaskStore).findAll();
        List<TaskWithId> actualList = actual.toList();
        assertEquals(given.size(), actualList.size());
    }

    @Test
    void shouldExceptOnNoneFound() {

        // given
        List<TaskWithId> givenEmpty = new ArrayList<>();
        when(mockTaskStore.findAll()).thenReturn(givenEmpty);

        // when
        Exception actual = assertThrows(TaskNotFoundException.class, () -> testSubject.getAll());

        // then
        verify(mockTaskStore).findAll();
    }

    @Test
    void shouldPropagateStoreExceptionOnFindAll() {

        // given
        String expectedMsg = "hello";
        Exception expectedEx = new RuntimeException(expectedMsg);
        when(mockTaskStore.findAll()).thenThrow(expectedEx);

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.getAll());

        // then
        verify(mockTaskStore).findAll();
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldDeleteEntityById() {

        // given
        String given = "1234";

        // when
        testSubject.delete(given);

        // then
        verify(mockTaskStore).deleteById(eq(given));
    }

    @Test
    void shouldPropagateStoreExceptionOnDelete() {

        // given
        String given = "1234";
        String expectedMsg = "hello";
        Exception expectedEx = new RuntimeException(expectedMsg);
        doThrow(expectedEx).when(mockTaskStore).deleteById(any());

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.delete(given));

        // then
        verify(mockTaskStore).deleteById(eq(given));
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldUpdateStatusEntityById() {

        // given - spy to stub some methods using doReturn, but otherwise invoke the real method
        final TaskService mockedTestSubject = spy(testSubject);

        final String givenId = "1234";
        final Status givenStatus = Status.Deleted;

        final TaskWithId givenStored = new TaskWithId();
        givenStored.setId(givenId);
        doReturn(givenStored).when(mockedTestSubject).get(any());

        final TaskWithId expectedUpdated = new TaskWithId();
        doReturn(expectedUpdated).when(mockedTestSubject).update(any());

        // when
        TaskWithId actual = mockedTestSubject.update(givenId, givenStatus.name());

        // then
        assertSame(expectedUpdated, actual);
        verify(mockedTestSubject).get(eq(givenId));
        verify(mockedTestSubject).update(taskCaptor.capture());
        assertSame(givenStored, taskCaptor.getValue());
    }

    @Test
    void shouldExceptOnInvalidStatus() {

        // given
        String givenId = "1234";
        String givenStatus = "no such status";

        // when
        Exception actual = assertThrows(
            TaskInvalidArgumentException.class,
            () -> testSubject.update(givenId, givenStatus)
        );

        // then
        assertTrue(actual.getMessage().contains(givenStatus));
    }

    @Test
    void shouldUpdateEntity() {

        // given
        TaskWithId given = new TaskWithId();
        TaskWithId expectedUpdated = new TaskWithId();
        when(mockTaskStore.save(any())).thenReturn(expectedUpdated);

        // when
        TaskWithId actual = testSubject.update(given);

        // then
        verify(mockTaskStore).save(taskCaptor.capture());
        assertSame(given, taskCaptor.getValue());
        assertSame(expectedUpdated, actual);
    }

    @Test
    void shouldPropagateSaveExceptionOnUpdate() {

        // given
        TaskWithId given = new TaskWithId();
        String expectedMsg = "hello";
        doThrow(new RuntimeException(expectedMsg)).when(mockTaskStore).save(any());

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.update(given));

        // then
        verify(mockTaskStore).save(taskCaptor.capture());
        assertSame(given, taskCaptor.getValue());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

}
