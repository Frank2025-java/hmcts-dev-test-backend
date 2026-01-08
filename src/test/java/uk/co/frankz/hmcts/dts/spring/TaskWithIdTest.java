package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class TaskWithIdTest {

    private final TaskWithId testSubject = new TaskWithId();

    @Test
    void shouldHaveDefaultInitialId() {
        assertNull(testSubject.getId());
    }

    @Test
    void shouldBeAbleToSetProperties() {
        String given = "123";

        // when
        testSubject.setId("123");

        // then
        String actual = testSubject.getId();
        assertEquals(given, actual);
    }

}
