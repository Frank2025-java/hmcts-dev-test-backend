package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapperTest {

    public Mapper testSubject;

    public static final String TEST_ID = "123";

    public TaskWithId testTask;

    @BeforeEach
    public void setup() {
        // override with spring mapper that maps id
        testSubject = new Mapper();

        // override with entity that has id
        testTask = new TaskWithId();
        testTask.setId(TEST_ID);
        testTask.setDescription("ddd");
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        testTask.setDue(givenTime);
        testTask.setTitle("t");
        testTask.setStatus(Status.Deleted);
    }

    protected void assertEqual(TaskDto given, TaskDto actual) {
        assertEquals(given.getId(), actual.getId());
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    protected void assertEqual(TaskWithId given, TaskWithId actual) throws ClassCastException {
        assertEquals(given.getId(), actual.getId());
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    @Test
    void shouldConvertTaskWithIdToDTOAndBack() {

        // given
        TaskWithId given = testTask;

        // when
        TaskWithId actual = testSubject.toEntity(testSubject.toDto(given));

        // then
        assertEqual(given, actual);
    }

    @Test
    void shouldNotConvertTaskDtoInvalidStatus() {

        // given
        TaskDto dto = new TaskDto();
        dto.setStatus("Invalid status");

        // then exception, when
        assertThrows(
            TaskInvalidArgumentException.class,
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
            TaskInvalidArgumentException.class,
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
            TaskInvalidArgumentException.class,
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
            TaskInvalidArgumentException.class,
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
        TaskWithId given = testTask;

        // when
        TaskDto actualDto = testSubject.toDto(given);
        TaskWithId actual = testSubject.toEntity(actualDto);

        // then
        assertNotNull(actualDto);
        assertEqual(given, actual);
    }

    @Test
    void shouldConvertDTOStream() {
        // given
        List<TaskWithId> given = Arrays.asList(testTask, new TaskWithId(testTask));

        // when
        TaskDto[] actual = testSubject.toDto(given.stream());

        // then
        assertEquals(given.size(), actual.length);
    }

}
