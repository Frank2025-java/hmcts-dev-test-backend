package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongIdentifierGeneratorTest {

    @Test
    void shouldNotGenerateSame() {
        long given = LongIdentifierGenerator.nextLongIdentifier();
        long actual = LongIdentifierGenerator.nextLongIdentifier();

        assertNotEquals(given, actual);
    }
}
