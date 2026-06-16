package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyBlankFieldsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdemPotencyScopeKeyTest {

    @ParameterizedTest
    @CsvSource(value = {
        // single nulls
        "null, POST, key123",
        "POST, null, key123",
        "POST, /path, null",

        // single blanks
        "'', POST, key123",
        "POST, '', key123",
        "POST, /path, ''",

        // mixed blanks
        "'', '', key123",
        "'', /path, ''",
        "POST, '', ''",

        // mixed nulls
        "null, null, key123",
        "null, /path, null",
        "POST, null, null",

        // all blank
        "'', '', ''",

        // all nulls
        "null, null, null"
    }, nullValues = "null")
    void constructorRejectsNullOrBlanks(String givenMethod, String givenPath, String givenKey) {
        assertThrows(
            IdemPotencyBlankFieldsException.class,
            () -> new IdemPotencyScopeKey(givenMethod, givenPath, givenKey)
        );
    }

    @Test
    void sha256ProducesDeterministicHash() {
        var key1 = new IdemPotencyScopeKey("POST", "/p", "abc");
        var key2 = new IdemPotencyScopeKey("POST", "/p", "abc");

        assertEquals(key1.sha256(), key2.sha256());
    }

    @Test
    void shouldBeEqualIgnoreCaseMethod() {
        var key1 = new IdemPotencyScopeKey("POST", "/p", "abc");
        var key2 = new IdemPotencyScopeKey("Post", "/p", "abc");
        assertEquals(key1, key2);
    }

    @Test
    void shouldNotBeEqualOnDifferentMethods() {
        var key1 = new IdemPotencyScopeKey("PUT", "/id/123", "abc");
        var key2 = new IdemPotencyScopeKey("DELETE", "/id/123", "abc");
        assertNotEquals(key1, key2);
    }

}
