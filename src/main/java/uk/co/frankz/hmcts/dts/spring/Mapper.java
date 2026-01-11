package uk.co.frankz.hmcts.dts.spring;

import org.springframework.stereotype.Component;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;

/**
 * Mapper represents the implementation of the conversion of a Data Transfer Object
 * to an Entity that can be stored/retrieved in/from the repository.
 * <br>
 * It extends the @See uk.co.frankz.hmcts.dts.dto.Mapper for the
 * basic domain properties, with an implementation which is
 * Spring dependent, which generates and keeps the id unique.
 */
@Component
public class Mapper extends uk.co.frankz.hmcts.dts.dto.Mapper {

    @Override
    protected Task newEntityWitId(String id) {
        TaskWithId entityWithId = new TaskWithId();
        entityWithId.setId(id);
        return entityWithId;
    }

    @Override
    protected String getEntityId(Task taskWithId) {

        if (taskWithId instanceof TaskWithId) {
            try {
                return ((TaskWithId) taskWithId).getId();
            } catch (Exception e) {
                throw new TaskInvalidArgumentException(taskWithId, e);
            }
        } else {
            throw new TaskInvalidArgumentException(taskWithId, "Argument taskWithId is not an instance of TaskWithId");
        }
    }

    /**
     * This override makes the usage easier by adding an upcast.
     *
     * @param dto Data Transfer Object
     * @return Entity with id.
     */
    @Override
    public TaskWithId toEntity(TaskDto dto) {

        try {
            return (TaskWithId) super.toEntity(dto);
        } catch (Exception e) {
            throw new TaskInvalidArgumentException(dto.toString(), e);
        }
    }
}
