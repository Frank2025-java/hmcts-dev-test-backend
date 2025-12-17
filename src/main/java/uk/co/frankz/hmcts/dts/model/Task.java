package uk.co.frankz.hmcts.dts.model;

import java.time.LocalDateTime;

/**
 * Class Task represents a domain entity, which
 * has properties title, description (optional),
 * status and due date/time.
 * It is required to be able to modify the status,
 * and tasks are searchable with identifier.
 */
public class Task {
    private final long id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime due;

    public Task() {
        this.id = LongIdentifierGenerator.nextLongIdentifier();
        this.status = Status.Created;
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

    public Task update(Status updateStatus) {
        this.status = updateStatus;
        return this;
    }
}

