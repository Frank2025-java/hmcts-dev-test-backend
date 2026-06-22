package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.DescribeTableEnhancedResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;
import uk.co.frankz.hmcts.dts.model.exception.TaskStoreException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskStoreImplTest {

    private TaskStoreImpl testSubject;

    @Mock
    private TaskWithId testTaskNew;

    @Mock
    private TaskWithId testTaskExisting;

    @Mock
    private DynamoDbTableProvider<TaskWithId> mockProvider;

    @BeforeEach
    void setup() {
        testSubject = new TaskStoreImpl(mockProvider);

        lenient().when(testTaskNew.isNew()).thenReturn(true);
        lenient().when(testTaskExisting.isNew()).thenReturn(false);
    }

    @Test
    void shouldSaveNewEntityUsingSave() {
        // given
        TaskWithId task = testTaskNew;

        // when
        testSubject.save(task);

        // then
        verify(mockProvider, times(1)).save(task);
        verify(mockProvider, never()).update(any());
    }

    @Test
    void shouldSaveExistingEntityUsingUpdate() {
        // given
        TaskWithId task = testTaskExisting;

        // when
        testSubject.save(task);

        // then
        verify(mockProvider, times(1)).update(task);
        verify(mockProvider, never()).save(any());
    }

    @Test
    void shouldWrapExceptionsThrownDuringSave() {
        // given
        TaskWithId task = testTaskNew;
        doThrow(new RuntimeException("boom")).when(mockProvider).save(task);

        // when / then
        assertThrows(TaskStoreException.class, () -> testSubject.save(task));
    }

    @Test
    void shouldReturnOptionalWhenFindingById() {
        // given
        when(mockProvider.find("123")).thenReturn(testTaskExisting);

        // when
        Optional<TaskWithId> result = testSubject.findById("123");

        // then
        assertTrue(result.isPresent());
        assertEquals(testTaskExisting, result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenNotFound() {
        // given
        when(mockProvider.find("123")).thenReturn(null);

        // when
        Optional<TaskWithId> result = testSubject.findById("123");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteById() {
        // given
        String id = "123";

        // when
        testSubject.deleteById(id);

        // then
        verify(mockProvider, times(1)).delete(id);
    }

    @Test
    void shouldReturnAllTasks() {
        // given
        List<TaskWithId> tasks = List.of(testTaskExisting);
        when(mockProvider.findAll()).thenReturn(tasks);

        // when
        Iterable<TaskWithId> result = testSubject.findAll();

        // then
        assertSame(tasks, result);
    }

    @Test
    void shouldWrapExceptionsThrownDuringFindAll() {
        // given
        when(mockProvider.findAll()).thenThrow(new RuntimeException("boom"));

        // when / then
        assertThrows(TaskStoreException.class, () -> testSubject.findAll());
    }

    @Test
    void shouldPassHealthCheckWhenTableIsActive() {
        // given
        TableStatus given = TableStatus.ACTIVE;
        DescribeTableEnhancedResponse desc = mock(DescribeTableEnhancedResponse.class);
        TableDescription tableDesc = TableDescription.builder()
            .tableStatus(given)
            .build();
        when(desc.table()).thenReturn(tableDesc);
        when(mockProvider.describe()).thenReturn(desc);

        // when / then
        assertDoesNotThrow(() -> testSubject.healthCheck());
    }

    @Test
    void shouldFailHealthCheckWhenTableIsNotActive() {
        // given
        TableStatus given = TableStatus.CREATING;
        DescribeTableEnhancedResponse desc = mock(DescribeTableEnhancedResponse.class);
        TableDescription tableDesc = TableDescription.builder()
            .tableStatus(given)
            .build();
        when(desc.table()).thenReturn(tableDesc);
        when(mockProvider.describe()).thenReturn(desc);

        // when / then
        assertThrows(TaskStoreException.class, () -> testSubject.healthCheck());
    }

    @Test
    void shouldWrapExceptionsThrownDuringHealthCheck() {
        // given
        when(mockProvider.describe()).thenThrow(new RuntimeException("boom"));

        // when / then
        assertThrows(TaskStoreException.class, () -> testSubject.healthCheck());
    }
}
