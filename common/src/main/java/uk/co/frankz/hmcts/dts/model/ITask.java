package uk.co.frankz.hmcts.dts.model;

import java.time.LocalDateTime;

/**
 * ITask is the java interface class with the
 * the getters and setters of Task.
 * Interfaces can be used for multi-inheritance,
 * more specific for a combination with EntityWithId.
 * <br>
 * There is only one place in the code where this currently
 * is needed, namely where setStatus is used.
 */
public interface ITask {
    void setTitle(String title);

    void setDescription(String description);

    void setDue(LocalDateTime due);

    void setStatus(Status status);

    String getTitle();

    String getDescription();

    Status getStatus();

    LocalDateTime getDue();
}
