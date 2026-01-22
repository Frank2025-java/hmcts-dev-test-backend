package uk.co.frankz.hmcts.dts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.ITask;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // Simulates and implementation for a repository generating an id and storing a Task entity
    interface TestTaskWithId extends ITask, EntityWithId {
    }

    uk.co.frankz.hmcts.dts.service.TaskService<TestTaskWithId> testSubject;

    @Mock
    TestTaskWithId testTaskMaiden;

    private static final String TEST_ID = "123";

    @Mock
    TestTaskWithId testTaskWithId;

    @Mock
    TaskStore<TestTaskWithId> mockTaskStore;

    @Captor
    ArgumentCaptor<TestTaskWithId> taskCaptor;

    @BeforeEach
    void setup() {

        testSubject = new uk.co.frankz.hmcts.dts.service.TaskService<>(mockTaskStore);

        lenient().when(testTaskMaiden.isNew()).thenReturn(true);
        lenient().when(testTaskMaiden.getId()).thenReturn(null);
        lenient().when(testTaskWithId.isNew()).thenReturn(false);
        lenient().when(testTaskWithId.getId()).thenReturn(TEST_ID);
    }

    @Test
    void shouldStoreEntity() {

        // given
        TestTaskWithId given = testTaskMaiden;
        TestTaskWithId mockStored = testTaskWithId;
        when(mockTaskStore.save(any())).thenReturn(mockStored);

        // when
        TestTaskWithId actual = testSubject.createTask(given);

        // then
        verify(mockTaskStore).save(taskCaptor.capture());
        assertSame(given, taskCaptor.getValue());
    }

    @Test
    void shouldExceptCreateTaskOnNull() {

        // given
        TestTaskWithId given = null;

        // when
        Exception actual = assertThrows(TaskInvalidArgumentException.class, () -> testSubject.createTask(given));

        // then
        verify(mockTaskStore, never()).save(any());
    }

    @Test
    void shouldExceptCreateTaskOnExistingTask() {

        // given
        TestTaskWithId given = testTaskWithId;
        when(given.isNew()).thenReturn(Boolean.FALSE);

        // when
        Exception actual = assertThrows(TaskInvalidArgumentException.class, () -> testSubject.createTask(given));

        // then
        verify(mockTaskStore, never()).save(any());
    }

    @Test
    void shouldPropagateStoreExceptionOnCreateTask() {

        // given
        TestTaskWithId given = testTaskMaiden;
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
        String given = TEST_ID;
        Optional<TestTaskWithId> mockStored = Optional.of(testTaskWithId);
        when(mockTaskStore.findById(any())).thenReturn(mockStored);

        // when
        TestTaskWithId actual = testSubject.get(given);

        // then
        verify(mockTaskStore).findById(eq(given));
        assertSame(mockStored.get(), actual);
    }

    @Test
    void shouldExceptOnNotFoundById() {

        // given
        String given = TEST_ID;
        Optional<TestTaskWithId> mockStored = Optional.empty();
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
        String expectedMsg = "hello";
        Exception expectedEx = new RuntimeException(expectedMsg);
        when(mockTaskStore.findById(any())).thenThrow(expectedEx);

        // when
        Exception actual = assertThrows(TaskStoreException.class, () -> testSubject.get(TEST_ID));

        // then
        verify(mockTaskStore).findById(any());
        assertTrue(actual.getMessage().contains(expectedMsg));
    }

    @Test
    void shouldFindAllEntities() {

        // given
        List<TestTaskWithId> given = Arrays.asList(mock(TestTaskWithId.class), mock(TestTaskWithId.class));
        when(mockTaskStore.findAll()).thenReturn(given);

        // when
        Stream<TestTaskWithId> actual = testSubject.getAll();

        // then
        verify(mockTaskStore).findAll();
        List<TestTaskWithId> actualList = actual.toList();
        assertEquals(given.size(), actualList.size());
    }

    @Test
    void shouldExceptOnNoneFound() {

        // given
        List<TestTaskWithId> givenEmpty = new ArrayList<>();
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
        String given = TEST_ID;

        // when
        testSubject.delete(given);

        // then
        verify(mockTaskStore).deleteById(eq(given));
    }

    @Test
    void shouldPropagateStoreExceptionOnDelete() {

        // given
        String given = TEST_ID;
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
        final TaskService<TestTaskWithId> mockedTestSubject = spy(testSubject);

        final String givenId = TEST_ID;
        final Status givenStatus = Status.Deleted;

        final TestTaskWithId givenStored = testTaskWithId;
        doReturn(givenStored).when(mockedTestSubject).get(any());

        final TestTaskWithId expectedUpdated = mock(TestTaskWithId.class);
        doReturn(expectedUpdated).when(mockedTestSubject).update(any());

        // when
        TestTaskWithId actual = mockedTestSubject.update(givenId, givenStatus.name());

        // then
        assertSame(expectedUpdated, actual);
        verify(mockedTestSubject).get(eq(givenId));
        verify(mockedTestSubject).update(taskCaptor.capture());
        assertSame(givenStored, taskCaptor.getValue());
    }

    @Test
    void shouldExceptOnInvalidStatus() {

        // given
        String givenStatus = "no such status";

        // when
        Exception actual = assertThrows(
            TaskNoMatchException.class,
            () -> testSubject.update(TEST_ID, givenStatus)
        );

        // then
        assertTrue(actual.getMessage().contains(givenStatus), actual.getMessage());
    }

    @Test
    void shouldUpdateEntity() {

        // given
        TestTaskWithId given = testTaskWithId;
        TestTaskWithId expectedUpdated = mock(TestTaskWithId.class);
        when(mockTaskStore.save(any())).thenReturn(expectedUpdated);

        // when
        TestTaskWithId actual = testSubject.update(given);

        // then
        verify(mockTaskStore).save(taskCaptor.capture());
        assertSame(given, taskCaptor.getValue());
        assertSame(expectedUpdated, actual);
    }

    @Test
    void shouldPropagateSaveExceptionOnUpdate() {

        // given
        TestTaskWithId given = testTaskWithId;
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
