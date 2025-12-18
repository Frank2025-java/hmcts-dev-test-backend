package uk.co.frankz.hmcts.dts.model;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * Class Task represents a domain entity, which
 * has properties title, description (optional),
 * status and due date/time.
 * It is required to be able to modify the status,
 * and tasks are searchable with identifier.
 */
public class Task {
    private long id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime due;

    public Task() {
        setId(LongIdentifierGenerator.nextLongIdentifier());
        setTitle("");
        setDescription(null);
        setStatus(Status.Created);
        setDue(LocalDateTime.now());
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getDue() {
        return due;
    }

    public Task setId(long id) {
        this.id = id;
        return this;
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
        this.due = due;
        return this;
    }

    public Task setStatus(Status status) {
        requireNonNull(status);
        this.status = status;
        return this;
    }
}

