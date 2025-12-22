package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

    Tasks testSubject;

    Task testTask1 = new Task();
    Task testTask2 = new Task();

    long testId1 = testTask1.getId();
    long testId2 = testTask2.getId();

    @BeforeEach
    void setup() {
        testSubject = new Tasks();
        testSubject.add(testTask1);
        testSubject.add(testTask2);
    }

    @Test
    void shouldRetieveTaskByID() {
        assertSame(testTask1, testSubject.getById(testId1));
        assertSame(testTask2, testSubject.getById(testId2));
    }

    @Test
    void shouldRetrieveAllTasks() {
        Collection<Task> actual = testSubject.getAll();
        assertEquals(2, actual.size());

        List<Long> expectedIds = Stream.of(testId1, testId2).sorted().toList();
        List<Long> actualIds = actual.stream().map(Task::getId).sorted().toList();

        assertEquals(expectedIds.get(0),actualIds.get(0));
        assertEquals(expectedIds.get(1),actualIds.get(1));
    }

    @Test
    void shouldDeleteTask() {
        testSubject.remove(testTask1);
        assertNull(testSubject.getById(testId1));
    }
}
