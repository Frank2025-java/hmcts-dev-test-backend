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
 * The identifier is added by TaskId class, which is an extension
 * of this class, and is dependent on a Spring implementation
 * for generating the id.
 * <br/>
 * The setters in this class, return "this" to allow chaining,
 * which makes coding a bit less verbose.
 */
@Getter
public class Task {

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

    public Task setTitle(String title) {

        requireNonNull(title);
        this.title = title;
        return this;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Task setDue(LocalDateTime due) {
        requireNonNull(due);
        this.due = due == null ? null : ISO_DATE_TIME.format(due);
        return this;
    }

    public Task setStatus(Status status) {
        requireNonNull(status);
        this.status = status.name();
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public LocalDateTime getDue() {
        return LocalDateTime.parse(due, ISO_DATE_TIME);
    }
}

