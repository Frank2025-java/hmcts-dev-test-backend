package uk.co.frankz.hmcts.dts.model;

import lombok.Getter;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.requireNonNull;

/**
 * Class Task represents a domain entity, which
 * has properties title, description (optional),
 * status and due date/time.
 * It is required to be able to modify the status,
 * and tasks are searchable with identifier.
 * The identifier is added by TaskWithId class, which is an extension
 * of this class, and is dependent on a Spring implementation
 * for generating the id.
 */
@Getter
public class Task implements ITask {

    private String title;
    private String description;

    // Issue with org.eclipse.serializer.util.traversing.TraverserReflective
    // Unable to make field private final java.lang.String java.lang.Enum.name accessible
    private String status;

    // Issue with software.xdev.spring.data.eclipse.store.repository.access.modifier.FieldAccessibleMaker
    // java.lang.IllegalAccessException: Could not access field java.time.LocalDateTime#date
    // Use ISO 8601 for the moment and resolve issue later.
    private String due;

    public Task() {
        this("", null, Status.Initial, LocalDateTime.now());
    }

    public Task(Task original) {
        this(original.getTitle(), original.getDescription(), original.getStatus(), original.getDue());
    }

    public Task(String title, String description, Status status, LocalDateTime due) {

        requireNonNull(title);
        requireNonNull(status);
        requireNonNull(due);

        this.title = title;
        this.description = description;
        this.status = status.name();
        this.due = due == null ? null : ISO_DATE_TIME.format(due);
    }

    @Override
    public void setTitle(String title) {

        requireNonNull(title);
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setDue(LocalDateTime due) {
        requireNonNull(due);
        this.due = due == null ? null : ISO_DATE_TIME.format(due);
    }

    @Override
    public void setStatus(Status status) {
        requireNonNull(status);
        this.status = status.name();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Status getStatus() {
        return Status.valueOf(status);
    }

    @Override
    public LocalDateTime getDue() {
        return LocalDateTime.parse(due, ISO_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Task{"
            + "title='" + title + '\''
            + ", description='" + description + '\''
            + ", status='" + status + '\''
            + ", due='" + due + '\''
            + '}';
    }
}

