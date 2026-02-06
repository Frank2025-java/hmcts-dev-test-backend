package uk.co.frankz.hmcts.dts.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ArrayUtils;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskJsonException;

/**
 * Mapper represents the implementation of the conversion of a Data Transfer Object
 * to an Entity that can be stored/retrieved in/from the repository.
 * <br>
 * It extends the @See uk.co.frankz.hmcts.dts.dto.Mapper for the
 * basic domain properties, with an implementation which is
 * Spring dependent, which generates and keeps the id unique.
 */
public class Mapper extends uk.co.frankz.hmcts.dts.dto.Mapper {

    public static final ObjectMapper JACKSON = new ObjectMapper();

    static {
        JACKSON.enable(SerializationFeature.INDENT_OUTPUT);
        JACKSON.registerModule(new JavaTimeModule());
        JACKSON.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JACKSON.enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
    }

    private final ObjectMapper json;

    public Mapper() {
        this(JACKSON);
    }

    /**
     * Constructor to allow unit testing with mocks.
     *
     * @param json Jackson ObjectMapper instance
     */
    Mapper(ObjectMapper json) {
        this.json = json;
    }

    @Override
    protected Task newEntityWitId(String id) {
        TaskWithId entityWithId = new TaskWithId();
        entityWithId.setId(id);
        return entityWithId;
    }

    /**
     * TaskDto instance from parsing a json string.
     * A TaskJsonException is thrown if the string parsing was unsuccessful.
     *
     * @param string with json
     * @return instance of TaskDto
     */
    public TaskDto toDto(String string) {

        try {
            return json.readValue(string, TaskDto.class);
        } catch (JsonProcessingException e) {
            throw new TaskJsonException("Parsing into TaskDto: " + string, e);
        }
    }

    /**
     * TaskWithId instance from parsing a json string, utilising the
     * toString(TaskDto) method.
     * A TaskJsonException is thrown if the string parsing was unsuccessful.
     *
     * @param json string
     * @return TaskWithId instance
     */
    public TaskWithId toEntity(String json) {
        return toEntity(toDto(json));
    }

    /**
     * This override makes the usage easier by adding an upcast.
     *
     * @param dto Data Transfer Object
     * @return Entity with id.
     */
    @Override
    public TaskWithId toEntity(TaskDto dto) {

        return (TaskWithId) super.toEntity(dto);
    }

    /**
     * json string representation of the task.
     *
     * @param dto instance
     * @return json string
     */
    public String toJsonString(TaskDto dto) {

        try {
            return json.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new TaskJsonException("Formating TaskDto: " + dto, e);
        }
    }

    /**
     * json string for the task.
     *
     * @param task instance
     * @return string
     */
    public String toJsonString(TaskWithId task) {
        return toJsonString(toDto(task));
    }

    /**
     * Json string for an array of DTO objects.
     *
     * @param dtos array of Taskdto instances
     * @return string
     */
    public String toJsonString(TaskDto[] dtos) {
        StringBuilder s = new StringBuilder();
        s.append('[');
        if (ArrayUtils.isNotEmpty(dtos)) {
            s.append(toJsonString(dtos[0]));
            for (int i = 1; i < dtos.length; i++) {
                s.append(',');
                s.append(toJsonString(dtos[i]));
            }
        }
        s.append(']');
        return s.toString();
    }
}
