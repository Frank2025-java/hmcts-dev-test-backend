package uk.co.frankz.hmcts.dts.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Class TaskDto represents the data transfer object for the task domain entity.
 * Separating transfer and entities is wise, to avoid complex issues when maintaining the code.
 */
public class TaskDto {

    /**
     * required value, initialised with 0.
     */
    private long id = 0;

    /**
     * required non-null value, initialised with empty string.
     */
    private String title = "";

    /**
     * optional value, initialised with null.
     */
    private String description;

    /**
     * required non-null value with the name of an enum in @see uk.co.frankz.hmcts.dts.model.Status,
     * initialised with "Initial".
     **/
    private String status = "Initial";

    /**
     * required non-null value, initialised with current time.
     * In the REST API String will be converted using ISO standard,
     * which is left for Spring REST implementation to do.
     *
     * @see org.springframework.format.annotation.DateTimeFormat.ISO DATE_TIME
     **/
    private LocalDateTime due = LocalDateTime.now();

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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public void setDue(LocalDateTime due) {
        this.due = due;
    }
}

