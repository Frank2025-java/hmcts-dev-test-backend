package uk.co.frankz.hmcts.dts.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapperTest {

    // to be instantiated differently in spring MapperTest
    public Mapper testSubject;

    public static final String TEST_ID = "123";

    // to be instantiated differently in spring MapperTest
    public Task testTask;

    @BeforeEach
    public void setup() {
        testTask = new Task();
        testTask.setDescription("ddd");
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        testTask.setDue(givenTime);
        testTask.setTitle("t");
        testTask.setStatus(Status.Deleted);

        testSubject = new Mapper() {
            @Override
            protected Task newEntityWitId(String id) {
                // ignore id
                return new Task();
            }

            @Override
            protected String getEntityId(Task taskWitId) {
                return TEST_ID;
            }
        };
    }

    @Test
    void shouldConvertTaskDtoToTaskAndBack() {

        // given
        TaskDto given = new TaskDto();
        given.setId(TEST_ID);
        given.setDescription("ddd");
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        given.setDue(givenTime);
        given.setTitle("t");
        given.setStatus("Deleted");

        // when
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        // then
        assertEqual(given, actual);
    }

    protected void assertEqual(TaskDto given, TaskDto actual) {
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    protected void assertEqual(Task given, Task actual) {
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    @Test
    void shouldNotConvertTaskDtoInvalidStatus() {

        // given
        TaskDto dto = new TaskDto();
        dto.setStatus("Invalid status");

        // then exception, when
        assertThrows(
            TaskNoMatchException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldNotConvertTaskDtoNullStatus() {

        // given
        TaskDto dto = new TaskDto();
        dto.setStatus(null);

        // then exception, when
        assertThrows(
            TaskNoMatchException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldConvertTaskDtoWithNullDescription() {

        // given
        TaskDto given = new TaskDto();
        given.setDescription(null);

        // when
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        // then
        assertEqual(given, actual);
    }

    @Test
    void shouldConvertTaskDtoWithBlankDescription() {

        // given
        TaskDto given = new TaskDto();
        given.setDescription("");

        // when
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        // then
        assertEqual(given, actual);
    }

    @Test
    void shouldNotConvertTaskDtoNullTitle() {

        // given
        TaskDto dto = new TaskDto();
        dto.setTitle(null);

        // then exception, when
        assertThrows(
            NullPointerException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldNotConvertTaskDtoNullDue() {

        // given
        TaskDto dto = new TaskDto();
        dto.setDue(null);

        // then exception, when
        assertThrows(
            NullPointerException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldConvertTaskDtoWithInitialDefaultsAndBack() {

        // given
        TaskDto given = new TaskDto();

        // when
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        // then
        assertEqual(given, actual);
    }

    @Test
    void shouldConvertTaskToDTOAndBack() {

        // given
        Task given = testTask;

        // when
        TaskDto actualDto = testSubject.toDto(given);
        Task actual = testSubject.toEntity(actualDto);

        // then
        assertNotNull(actualDto);
        assertEqual(given, actual);
    }

    @Test
    void shouldConvertDTOStream() {
        // given
        List<Task> given = Arrays.asList(testTask, new Task(testTask));

        // when
        TaskDto[] actual = testSubject.toDto(given.stream());

        // then
        assertEquals(given.size(), actual.length);
    }

}
