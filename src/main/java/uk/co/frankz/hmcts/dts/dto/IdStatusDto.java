package uk.co.frankz.hmcts.dts.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Class IdStatusDto represents the data transfer object for the task id and task status.
 */
@Setter
@Getter
public class IdStatusDto {

    /**
     * required non-null value, initialised with empty.
     */
    private String id = "";

    /**
     * required non-null value with the name of an enum in @see uk.co.frankz.hmcts.dts.model.Status,
     * initialised with "Initial".
     **/
    private String status = "Initial";

    public IdStatusDto() {
    }

    public IdStatusDto(String id, String status) {
        this.id = id;
        this.status = status;
    }

    @Override
    public String toString() {
        return "IdStatusDto{"
            + "id='" + id + '\''
            + ", status='" + status + '\''
            + '}';
    }
}

