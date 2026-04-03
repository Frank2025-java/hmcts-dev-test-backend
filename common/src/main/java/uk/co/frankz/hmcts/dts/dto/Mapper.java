package uk.co.frankz.hmcts.dts.dto;

import jakarta.validation.constraints.NotNull;
import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

/**
 * Mapper is a straight forward conversion between
 * a Data Transfer Object and Domain class for entity Task.
 * <br/>
 * By keeping the DTO and Domain separated, the
 * implementation avoids challenges for more
 * challenging requirements. It is a good practice for
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
        entity.setDue(local(dto.getDue()));

        return entity;
    }

    public TaskDto toDto(Task entity) {

        TaskDto dto = new TaskDto();
        dto.setId(getEntityId(entity));
        dto.setStatus(entity.getStatus().name());
        dto.setTitle(entity.getTitle());
        dto.setDue(zoned(entity.getDue()));
        dto.setDescription(entity.getDescription());

        return dto;
    }

    public @NotNull TaskDto[] toDto(@NotNull Stream<? extends Task> entities) {
        return entities.map(this::toDto).toArray(TaskDto[]::new);
    }

    /**
     * Convert to a ZonedDateTime instance using the server's timezone.
     *
     * @param local a LocalDateTime instance.
     * @return a ZonedDateTime instance with the server's zone and region.
     */
    public static ZonedDateTime zoned(LocalDateTime local) {
        return local == null ? null : local.atZone(ZoneId.systemDefault());
    }

    /**
     * Convert to a local datetime, by dropping the region and set it to the system zone.
     * It maintains the instance in time, and not the user's perception,
     * which is left for a browser to convert.
     * User and server can work in different timezones, which is common in cloud applications.
     *
     * @param zoned a ZonedDateTime instance.
     * @return a LocalDateTime instance based on the server's zone.
     */
    public static LocalDateTime local(ZonedDateTime zoned) {
        return zoned == null ? null : zoned.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Returns the offset part, e.g. "+01:00", of "+01:00[Europe/London]" as a string.
     * This is the zone and daylight saving time setting for the region.
     * The region is not returned in the string, because there is
     * no iso standard for the naming in ISO‑8601.
     * Because there is no standard, Jackson json serialization will not do that either.
     *
     * @param local a LocalDateTime instance.
     * @return string that is the offset for the server's zone and region.
     */
    public static String isoZoneOffset(LocalDateTime local) {
        return zoned(local).getOffset().toString();
    }
}
