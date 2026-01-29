package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.frankz.hmcts.dts.model.Status;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "org.eclipse.store.storage-directory=build/eclipse-store-storage/taskstoretest"
    },
    classes = Application.class)
class TaskStoreTest {

    @Autowired
    TaskStore testSubject;

    static final LocalDateTime TEST_TIME = LocalDateTime.parse("2026-01-05T16:45:30");

    private TaskWithId testTask;

    @BeforeEach
    void setup() {
        testTask = null;
    }

    @AfterEach
    void tearDown() {
        testSubject.deleteAll();
    }

    void storeTestTask() {
        testTask = new TaskWithId();
        testTask.setDescription("ddd");
        testTask.setDue(TEST_TIME);
        testTask.setTitle("t");
        testTask.setStatus(Status.Deleted);

        testSubject.save(testTask);

        // asserts for all tests
        assertNotNull(testTask);
        assertNotNull(testTask.getId());
    }

    @Test
    void shouldNotBeMock() {
        assertFalse(MockUtil.isMock(testSubject));
    }

    @Test
    void shouldFindById() {
        // given, when
        storeTestTask();

        // then
        assertEqualsFoundInStore(testTask);
    }

    @Test
    void shouldNotFindByIdInvalidId() {
        // given
        storeTestTask();
        String givenInvalid = testTask.getId() + "000";

        // when
        Optional<TaskWithId> actualFound = testSubject.findById(givenInvalid);

        // then
        assertNotNull(actualFound);
        assertFalse(actualFound.isPresent());
    }

    @Test
    void shouldUpdate() {
        // given
        storeTestTask();

        TaskWithId givenClone = new TaskWithId(testTask);
        givenClone.setTitle("new title");
        givenClone.setDescription(null);
        givenClone.setDue(TEST_TIME.minusDays(1));

        // when
        testSubject.save(givenClone);

        // then
        assertEqualsFoundInStore(givenClone);
    }

    @Test
    void shouldRemove() {
        // given
        storeTestTask();

        // when
        testSubject.delete(testTask);

        // then
        shouldFindAllOnEmpty();
    }

    void assertEqualsFoundInStore(TaskWithId expected) {
        assertNotNull(expected);
        assertNotNull(expected.getId());

        Optional<TaskWithId> actualFound = testSubject.findById(expected.getId());

        assertNotNull(actualFound);
        assertTrue(actualFound.isPresent());
        TaskWithId actual = actualFound.get();

        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getDescription(), expected.getDescription());
        assertEquals(actual.getStatus(), expected.getStatus());
        assertEquals(actual.getDue(), expected.getDue());
        assertEquals(actual.getTitle(), expected.getTitle());
    }

    @Test
    void shouldFindAllOnEmpty() {
        // when
        Iterable<TaskWithId> actualAll = testSubject.findAll();

        // then
        assertNotNull(actualAll);
        assertFalse(actualAll.iterator().hasNext());
    }

    @Test
    void shouldFindAllOnSingle() {
        // given
        storeTestTask();

        // when
        Iterable<TaskWithId> actualAll = testSubject.findAll();

        // then
        assertNotNull(actualAll);
        Iterator<TaskWithId> actualIterator = actualAll.iterator();
        assertTrue(actualIterator.hasNext());
        assertNotNull(actualIterator.next());
        assertFalse(actualIterator.hasNext());
    }

    @Test
    void shouldFindAllOnThree() {
        storeTestTask();
        storeTestTask();
        storeTestTask();

        // when
        Iterable<TaskWithId> actualAll = testSubject.findAll();

        // then
        assertNotNull(actualAll);
        Iterator<TaskWithId> actualIterator = actualAll.iterator();
        assertTrue(actualIterator.hasNext());
        assertNotNull(actualIterator.next());
        assertTrue(actualIterator.hasNext());
        assertNotNull(actualIterator.next());
        assertTrue(actualIterator.hasNext());
        assertNotNull(actualIterator.next());
        assertFalse(actualIterator.hasNext());
    }

    @Test
    void shouldNotErrorOnCheckHealth() {
        testSubject.healthCheck();
    }
}
