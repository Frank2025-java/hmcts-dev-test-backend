package uk.co.frankz.hmcts.dts.dto;

import jakarta.validation.constraints.NotNull;
import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;

import java.util.stream.Stream;

/**
 * Mapper is a straight forward conversion between
 * a Data Transfer Object and Domain class for entity Task.
 * <br/>
 * By keeping the DTO and Domain separated, the
 * implementation avoids challenges for more
 * difficult requirements. It is a good practise for
 * better maintainable code.
 * <br/>
 * The id implementation is in the Spring version
 * of Mapper, because id is generated/implemented with
 * the Spring persistence framework,
 * and this class is made abstract, with abstract methods
 * for that id property.
 */
public abstract class Mapper {

    protected abstract Task newEntityWitId(String id);

    protected String getEntityId(Task taskWithId) {

        if (taskWithId instanceof EntityWithId) {
            try {
                return ((EntityWithId) taskWithId).getId();
            } catch (Exception e) {
                throw new TaskInvalidArgumentException(taskWithId, e);
            }
        } else {
            throw new TaskInvalidArgumentException(
                taskWithId,
                "Argument taskWithId is not an instance of EntityWithId"
            );
        }
    }

    public Task toEntity(TaskDto dto) {

        Status status = Status.parse(dto.getStatus());

        Task entity = newEntityWitId(dto.getId());

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStatus(status);
        entity.setDue(dto.getDue());

        return entity;
    }

    public TaskDto toDto(Task entity) {

        TaskDto dto = new TaskDto();
        dto.setId(getEntityId(entity));
        dto.setStatus(entity.getStatus().name());
        dto.setTitle(entity.getTitle());
        dto.setDue(entity.getDue());
        dto.setDescription(entity.getDescription());

        return dto;
    }

    public @NotNull TaskDto[] toDto(@NotNull Stream<? extends Task> entities) {
        return entities.map(this::toDto).toArray(TaskDto[]::new);
    }
}
