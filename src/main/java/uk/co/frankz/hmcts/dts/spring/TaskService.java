package uk.co.frankz.hmcts.dts.spring;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.model.exception.TaskNotFoundException;
import uk.co.frankz.hmcts.dts.model.exception.TaskStoreException;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class TaskService {

    @Autowired
    private final TaskStore store;

    @Autowired
    public TaskService(TaskStore taskStore) {
        this.store = taskStore;
    }

    public TaskWithId createTask(TaskWithId newTask) {

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

    public @NotNull TaskWithId get(String id) {

        final Optional<TaskWithId> found;

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

    public Stream<TaskWithId> getAll() {

        final Iterable<TaskWithId> found;

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

    public TaskWithId update(String id, String status) {

        final Status newStatus;

        try {
            newStatus = Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new TaskInvalidArgumentException(status, e);
        }

        TaskWithId toBeStored = this.get(id);

        toBeStored.setStatus(newStatus);

        return update(toBeStored);
    }

    public TaskWithId update(TaskWithId input) {
        try {
            return store.save(input);
        } catch (Exception e) {
            throw new TaskStoreException(input, e);
        }
    }
}
