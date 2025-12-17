package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task testSubject = new Task();

    @Test
    void shouldHaveInitialIdNotDefault0() {
        assertNotSame(0, testSubject.getId());
    }

    @Test
    void shouldHaveInitialStatusCreated() {
        assertSame(Status.Created, testSubject.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void shouldUpdateInitialStatus(Status updateStatus) {

       Task actual = testSubject.update(updateStatus);

       assertEquals(updateStatus, testSubject.getStatus());
    }
}
