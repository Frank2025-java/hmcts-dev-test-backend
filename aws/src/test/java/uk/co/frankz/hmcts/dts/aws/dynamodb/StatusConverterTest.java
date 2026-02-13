package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.co.frankz.hmcts.dts.model.Status;

import static org.junit.jupiter.api.Assertions.assertSame;

class StatusConverterTest {

    StatusConverter testSubject = new StatusConverter();

    @ParameterizedTest
    @EnumSource(Status.class)
    void shouldTransform(Status given) {
        assertSame(given, testSubject.transformTo(testSubject.transformFrom(given)));
    }
}
