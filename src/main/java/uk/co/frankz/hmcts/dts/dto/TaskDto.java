package uk.co.frankz.hmcts.dts.dto;

import uk.co.frankz.hmcts.dts.model.LongIdentifierGenerator;
import uk.co.frankz.hmcts.dts.model.Status;

import java.time.LocalDateTime;

/**
 * Class TaskDto represents the data transfer object for the task domain entity.
 * Separating transfer and entities is wise, to avoid complex issues when maintaining the code.
 */
public class TaskDto {

    //required
    private long id;

    // required
    private String title;

    // optional
    private String description;

    // required
    private String status;

    // required
    private LocalDateTime due;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDue() {
        return due;
    }

    // use @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) in Spring rest api
    public void setDue(LocalDateTime due) {
        this.due = due;
    }
}

