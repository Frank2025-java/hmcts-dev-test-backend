package uk.co.frankz.hmcts.dts.service;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.IterableUtils;
import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.ITask;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNotFoundException;
import uk.co.frankz.hmcts.dts.model.exception.TaskStoreException;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * TaskService represents the API of the back-end for the Task model.
 * <br>
 * The entity fields are not needed to know for the API, only that there
 * is a unique identifier, which is to be assumed a string.
 * The exceptions are wrapped to application specific TaskExceptions.
 * <br>
 * The generic TASK is also inheriting from ITask
 * because there is one action, namely setStatus,
 * for which it is needed.
 */
public class TaskService<T extends EntityWithId & ITask> {

    private final TaskStore<T> store;

    public TaskService(TaskStore<T> taskStore) {
        this.store = taskStore;
    }

    public T createTask(T newTask) {

        if (newTask == null) {
            throw new TaskInvalidArgumentException(newTask, "Cannot create task");
        }

        if (!newTask.isNew()) {
            throw new TaskInvalidArgumentException(newTask, "Existing Task");
        }

        try {
            return store.save(newTask);
        } catch (Exception e) {
            throw new TaskStoreException(newTask, e.toString());
        }
    }

    public @NotNull T get(String id) {

        final Optional<T> found;

        try {
            found = store.findById(id);
        } catch (Exception e) {
            throw new TaskStoreException(e);
        }

        if (found.isPresent()) {
            return found.get();
        } else {
            throw new TaskNotFoundException(id);
        }

    }

    public Stream<T> getAll() {

        final Iterable<T> found;

        try {
            found = store.findAll();
        } catch (Exception e) {
            throw new TaskStoreException(e);
        }

        if (IterableUtils.isEmpty(found)) {
            throw new TaskNotFoundException("none");
        }

        return StreamSupport.stream(found.spliterator(), true);
    }

    public void delete(String id) {

        try {
            // Spring notes: If the entity is not found in the persistence store it is silently ignored.
            store.deleteById(id);
        } catch (Exception e) {
            throw new TaskStoreException(e);
        }
    }

    public T update(T input) {
        try {
            return store.save(input);
        } catch (Exception e) {
            throw new TaskStoreException(input, e);
        }
    }

    public T update(String id, String status) {

        final Status newStatus = Status.parse(status);

        T toBeStored = this.get(id);

        // only for
        toBeStored.setStatus(newStatus);

        return update(toBeStored);
    }
}
