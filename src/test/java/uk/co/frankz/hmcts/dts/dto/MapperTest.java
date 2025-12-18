package uk.co.frankz.hmcts.dts.dto;

import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapperTest {

    Mapper testSubject = new Mapper();

    @Test
    void shouldConvertTaskDtoToTaskAndBack() {

        TaskDto given = new TaskDto();
        given.setId(123);
        given.setDescription("ddd");
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        given.setDue(givenTime);
        given.setTitle("t");
        given.setStatus("Deleted");

        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        extracted(given, actual);
    }

    private static void extracted(TaskDto given, TaskDto actual) {
        assertEquals(given.getId(), actual.getId());
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    private static void extracted(Task given, Task actual) {
        assertEquals(given.getId(), actual.getId());
        assertEquals(given.getStatus(), actual.getStatus());
        assertEquals(given.getDescription(), actual.getDescription());
        assertEquals(given.getTitle(), actual.getTitle());
        assertEquals(given.getDue(), actual.getDue());
    }

    @Test
    void shouldNotConvertTaskDtoInvalidStatus() {

        TaskDto dto = new TaskDto();
        dto.setStatus("Invalid status");

        assertThrows(
            IllegalArgumentException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldNotConvertTaskDtoNullStatus() {

        TaskDto dto = new TaskDto();
        dto.setStatus(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> testSubject.toEntity(dto)
        );
    }

    @Test
    void shouldConvertTaskDtoWithNullDescription() {

        TaskDto given = new TaskDto();
        given.setDescription(null);
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        extracted(given, actual);
    }

    @Test
    void shouldConvertTaskDtoWithBlankDescription() {

        TaskDto given = new TaskDto();
        given.setDescription("");
        TaskDto actual = testSubject.toDto(testSubject.toEntity(given));

        extracted(given, actual);
    }

}
