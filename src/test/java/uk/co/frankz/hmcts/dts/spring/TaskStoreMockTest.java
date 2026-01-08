package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.frankz.hmcts.dts.model.Status;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TaskStoreMockTestConfig.class)
class TaskStoreMockTest {

    @Autowired
    TaskStore testSubject;

    static final LocalDateTime TEST_TIME = LocalDateTime.parse("2026-01-05T16:45:30");

    private TaskWithId testTask;

    @BeforeEach
    void setup() {
        testTask = new TaskWithId();
        testTask.setDescription("ddd");
        testTask.setDue(TEST_TIME);
        testTask.setTitle("t");
        testTask.setStatus(Status.Deleted);
    }

    @Test
    void shouldBeMock() {
        assertTrue(MockUtil.isMock(testSubject));
    }

    @Test
    void shouldDelete() {
        testSubject.delete(testTask);
        testSubject.deleteAll();
    }

    @Test
    void shouldSave() {
        testSubject.save(testTask);
    }

    @Test
    void shouldFindById() {
        testSubject.findById("");
    }

    @Test
    void shouldFindAllOnEmpty() {
        testSubject.findAll();
    }
}
