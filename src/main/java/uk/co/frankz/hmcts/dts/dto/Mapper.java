package uk.co.frankz.hmcts.dts.dto;

import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;

/**
 * Mapper is a straight forward conversion between
 * a Data Transfer Object and Domain class for entity Task.
 * <p>By keeping the DTO and Domain separated, the
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

    protected abstract String getEntityId(Task taskWitId);

    public Task toEntity(TaskDto dto) {

        Status status = Status.valueOf(dto.getStatus());

        return newEntityWitId(dto.getId())
            .setTitle(dto.getTitle())
            .setDescription(dto.getDescription())
            .setStatus(status)
            .setDue(dto.getDue());
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
}
