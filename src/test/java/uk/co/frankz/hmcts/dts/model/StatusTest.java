package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTest {

    @ParameterizedTest
    @EnumSource(Status.class)
    void shouldReturnEnum(Status input) {
        // given
        String given = input.name();

        // when
        Status actual = Status.parse(given);

        //
        assertSame(input, actual);
    }

    @Test
    void shouldGiveExceptionOnInvalid() {
        // given
        String given = "invalid status name";

        // when
        TaskNoMatchException actual = assertThrows(TaskNoMatchException.class, () -> Status.parse(given));

        // then
        assertTrue(actual.getMessage().contains(given));
    }

    @Test
    void shouldGiveExceptionOnNull() {
        // given
        String given = null;
        String expected = "null";

        // when
        TaskNoMatchException actual = assertThrows(TaskNoMatchException.class, () -> Status.parse(given));

        // then
        assertTrue(actual.getMessage().contains(expected));
    }
}
