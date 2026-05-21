package uk.co.frankz.hmcts.dts.model;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyBlankFieldsException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyInvalidKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void scopeKeyBuildsFromHttpServletRequest() throws IdemPotencyException {
        // given
        HttpServletRequest given = mock(HttpServletRequest.class);
        when(given.getMethod()).thenReturn("PUT");
        when(given.getRequestURI()).thenReturn("/orders/123");
        when(given.getHeader("Idempotency-Key")).thenReturn("xyz");

        // when
        IdemPotencyScopeKey key = IdemPotencyScopeKey.scopeKey(given);

        // then
        assertEquals("PUT", key.method());
        assertEquals("/orders/123", key.path());
        assertEquals("xyz", key.idempotencyKey());
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

    @Test
    void shouldThrowCheckedExceptionOnScopeKeyBuildsFromHttpServletRequest() {
        // given
        HttpServletRequest given = mock(HttpServletRequest.class);
        when(given.getMethod()).thenReturn("Post");
        when(given.getRequestURI()).thenReturn("/orders/123");
        when(given.getHeader("Idempotency-Key")).thenReturn(null);

        // when, then
        assertThrows(
            IdemPotencyInvalidKeyException.class,
            () -> IdemPotencyScopeKey.scopeKey(given)
        );
    }

}
