package uk.co.frankz.hmcts.dts.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Class TaskDto represents the data transfer object for the task domain entity.
 * Separating transfer and entities is wise, to avoid complex issues when maintaining the code.
 */
@Setter
@Getter
public class TaskDto {

    /**
     * Optional value, initialised with null.
     */
    private String id = null;

    /**
     * Required non-null value, initialised with empty string.
     */
    private String title = "";

    /**
     * Optional value, initialised with null.
     */
    private String description;

    /**
     * Required non-null value with the name of an enum in @see uk.co.frankz.hmcts.dts.model.Status,
     * initialised with "Initial".
     **/
    private String status = "Initial";

    /**
     * Required non-null value, initialised with current time.
     * In the REST API String will be converted using ISO standard,
     * which is left for Spring REST implementation to do.
     * With the Time Zone, the Backend system works with local datetime in the system default timezone,
     * whereas the browser/user can work in a different time zone or with daylight saving.
     *
     * @see <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/format/annotation/DateTimeFormat.ISO.html">
     * DateTimeFormat.ISO Javadoc</a>
     **/
    private ZonedDateTime due = ZonedDateTime.now();

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

