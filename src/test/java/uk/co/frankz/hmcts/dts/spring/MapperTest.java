package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest extends uk.co.frankz.hmcts.dts.dto.MapperTest {

    @BeforeEach
    @Override
    public void setup() {
        // override with spring mapper that maps id
        testSubject = new Mapper();

        // override with entity that has id
        testTask = new TaskWithId();
        ((TaskWithId) testTask).setId(TEST_ID);
        testTask.setDescription("ddd");
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        testTask.setDue(givenTime);
        testTask.setTitle("t");
        testTask.setStatus(Status.Deleted);
    }

    @Override
    protected void assertEqual(TaskDto given, TaskDto actual) {
        assertEquals(given.getId(), actual.getId());

        super.assertEqual(given, actual);
    }

    @Override
    protected void assertEqual(Task given, Task actual) throws ClassCastException {
        super.assertEqual(given, actual);

        // support tests that only return Task instance
        if (given instanceof TaskWithId || actual instanceof TaskWithId) {
            assertEquals(((TaskWithId) given).getId(), ((TaskWithId) actual).getId());
        }
    }

    @Test
    void shouldConvertTaskWithIdToDTOAndBack() {

        // given
        TaskWithId given = (TaskWithId) testTask;

        // when
        TaskWithId actual = (TaskWithId) testSubject.toEntity(testSubject.toDto(given));

        // then
        assertEqual(given, actual);
    }

}
