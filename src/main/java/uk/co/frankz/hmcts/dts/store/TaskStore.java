package uk.co.frankz.hmcts.dts.store;

import uk.co.frankz.hmcts.dts.model.Task;

import java.util.List;

/**
 *  TaskStore represents the API of the back-end
 */
public interface TaskStore {

    Task create(Task task);

    Task get(long id);

    Task update(Task task);

    Task delete(long id);

    List<Task> getAll();
}
