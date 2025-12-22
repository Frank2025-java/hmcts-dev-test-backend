package uk.co.frankz.hmcts.dts.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class Tasks is extending and ArrayList, which is a simple
 * implementation to do something with a collection of Tasks
 * which suits the basic requirements that are added using
 * TDD and the test class.
 */
public class Tasks extends ArrayList<Task> {

    /**
     * Returns a Task or null, if there is no Task in the list with this id.
     * @param id
     * @return Task or null
     */
    public Task getById(long id) {
        if (isEmpty()) {
            return null;
        }

        return stream()
            .filter(task -> id == task.getId())
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns all elements this list.
     * @return a collection of all elements in this Task list.
     */
    public Collection<Task> getAll() {
        // note by returning the list instance is not safe, because the actual list instance is returned
        // rather than cloned instances.
        // Currently there is no requirement for returning a clone with cloned elements.
        return this;
    }
}
