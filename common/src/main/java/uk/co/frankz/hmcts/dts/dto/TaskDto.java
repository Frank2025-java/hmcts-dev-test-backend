package uk.co.frankz.hmcts.dts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Class TaskDto represents the data transfer object for the task domain entity.
 * Separating transfer and entities is wise, to avoid complex issues when maintaining the code.
 */
@Setter
@Getter
public class TaskDto {

    /**
     * optional value, initialised with null.
     */
    private String id = null;

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
     * @see org.springframework.format.annotation.DateTimeFormat.ISO
     **/
    private LocalDateTime due = LocalDateTime.now();

    @Override
    public String toString() {
        return "TaskDto{"
            + "id='" + id + '\''
            + ", title='" + title + '\''
            + ", description='" + description + '\''
            + ", status='" + status + '\''
            + ", due=" + due
            + '}';
    }
}

