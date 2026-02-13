package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoUuidConverterTest {

    AutoUuidConverter testSubject = new AutoUuidConverter();

    @Test
    void shouldTransform() {
        //
        UUID given = UUID.fromString("12345678-1234-1234-1234-123456789012");

        // when
        UUID actual = testSubject.transformTo(testSubject.transformFrom(given));

        // then
        assertEquals(given, actual);
    }
}
