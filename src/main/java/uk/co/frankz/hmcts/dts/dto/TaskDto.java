package uk.co.frankz.hmcts.dts.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
     * <br/>
     * Note that we allow here to have a Spring reference
     * as the annotation, to complicate things not more
     * like we do with the id field for Task.
     *
     * @see org.springframework.format.annotation.DateTimeFormat.ISO DATE_TIME
     **/
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
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

