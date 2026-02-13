package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsoDateTimeConverterTest {

    IsoDateTimeConverter testSubject = new IsoDateTimeConverter();

    @Test
    void shouldTransform() {
        //
        LocalDateTime given = LocalDateTime.parse("2024-02-29T16:45:30");

        // when
        LocalDateTime actual = testSubject.transformTo(testSubject.transformFrom(given));

        // then
        assertEquals(given, actual);
    }
}
