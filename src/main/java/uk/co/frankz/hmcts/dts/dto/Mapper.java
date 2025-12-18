package uk.co.frankz.hmcts.dts.dto;

import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;

public class Mapper {
    public Task toEntity(TaskDto dto) {

        Status status = Status.valueOf(dto.getStatus());

        return new Task()
            .setId(dto.getId())
            .setTitle(dto.getTitle())
            .setDescription(dto.getDescription())
            .setStatus(status)
            .setDue(dto.getDue());
    }

    public TaskDto toDto(Task entity) {

        TaskDto dto = new TaskDto();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus().name());
        dto.setTitle(entity.getTitle());
        dto.setDue(entity.getDue());
        dto.setDescription(entity.getDescription());

        return dto;
    }
}
