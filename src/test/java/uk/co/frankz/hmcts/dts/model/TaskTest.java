package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    private final Task testSubject = new Task();

    @Test
    void shouldHaveInitialStatusCreated() {
        assertSame(Status.Created, testSubject.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void shouldUpdateInitialStatus(Status updateStatus) {

        testSubject.setStatus(updateStatus);

        assertEquals(updateStatus, testSubject.getStatus());
    }

    @Test
    void shouldHaveNotHaveDefaultSameId() {
        long notExpectedId = new Task().getId();
        assertNotSame(notExpectedId, testSubject.getId());
    }

    @Test
    void shouldBeAbleToSetProperties() {
        assertEquals(123, testSubject.setId(123).getId());
        assertEquals("x", testSubject.setTitle("x").getTitle());
        assertEquals("y", testSubject.setDescription("y").getDescription());
        assertNull(testSubject.setDescription(null).getDescription());
        assertEquals(Status.Deleted, testSubject.setStatus(Status.Deleted).getStatus());
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");
        assertEquals(givenTime, testSubject.setDue(givenTime).getDue());
    }

    @Test
    void shouldNotAllowNullOnRequiredProperties() {
        assertThrows(NullPointerException.class, () -> testSubject.setTitle(null));
        assertThrows(NullPointerException.class, () -> testSubject.setStatus(null));
        assertThrows(NullPointerException.class, () -> testSubject.setDue(null));
    }
}
