package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    private final Task testSubject = new Task();

    @Test
    void shouldHaveInitialStatus() {
        assertSame(Status.Initial, new Task().getStatus());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void shouldUpdateInitialStatus(Status updateStatus) {

        testSubject.setStatus(updateStatus);

        assertEquals(updateStatus, testSubject.getStatus());
    }

    @Test
    void shouldBeAbleToSetPropertyTitle() {
        // given

        // when
        testSubject.setTitle("x");

        // then
        assertEquals("x", testSubject.getTitle());
    }

    @Test
    void shouldBeAbleToSetPropertyDescription() {
        // given

        // when
        testSubject.setDescription("y");

        // then
        assertEquals("y", testSubject.getDescription());
    }

    @Test
    void shouldBeAbleToSetPropertyStatus() {
        // given

        // when
        testSubject.setStatus(Status.Deleted);

        // then
        assertSame(Status.Deleted, testSubject.getStatus());
    }

    @Test
    void shouldBeAbleToSetPropertyDue() {
        // given
        LocalDateTime givenTime = LocalDateTime.parse("2025-12-18T16:45:30");

        // when
        testSubject.setDue(givenTime);

        // then
        assertEquals(givenTime, testSubject.getDue());
    }

    @Test
    void shouldAllowNullOnDescription() {
        // given

        // when
        testSubject.setDescription(null);

        // then
        assertNull(testSubject.getDescription());
    }

    @Test
    void shouldNotAllowNullOnRequiredProperties() {
        assertThrows(NullPointerException.class, () -> testSubject.setTitle(null));
        assertThrows(NullPointerException.class, () -> testSubject.setStatus(null));
        assertThrows(NullPointerException.class, () -> testSubject.setDue(null));
    }
}
