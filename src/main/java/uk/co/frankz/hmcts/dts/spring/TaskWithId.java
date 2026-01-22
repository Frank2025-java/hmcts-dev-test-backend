package uk.co.frankz.hmcts.dts.spring;

import jakarta.persistence.GeneratedValue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.Task;

import static jakarta.persistence.GenerationType.AUTO;

/**
 * Class TaskWithId represents entity Task, with the addition
 * that has an identity, which is implemented by Spring.
 * <br>
 * The separation of the id and the domain fields is a result
 * of adding the Spring implementation for storing entities
 * after the domain fields where created, and a possible
 * replacement of the spring specific persistency choice
 * by a different implementation, like Eclipse Store,
 * which promises to be much cheaper and more performant
 * in Cloud environments.
 */
@Getter
@Setter
public class TaskWithId extends Task implements EntityWithId {
    @Id
    @GeneratedValue(strategy = AUTO)
    private String id;

    public boolean isNew() {
        return id == null;
    }

    public TaskWithId() {
        super();
    }

    public TaskWithId(Task original) {
        super(original);
        if (original instanceof EntityWithId) {
            this.id = ((EntityWithId) original).getId();
        }
    }

    @Override
    public String toString() {
        return "TaskWithId{"
            + "id='" + id + '\''
            + ", " + super.toString()
            + '}';
    }
}
