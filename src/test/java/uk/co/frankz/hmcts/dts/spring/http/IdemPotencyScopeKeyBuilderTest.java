package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdemPotencyScopeKeyBuilderTest {

    private final IdemPotencyScopeKeyBuilder testSubject = new IdemPotencyScopeKeyBuilder();

    @ParameterizedTest
    @CsvSource({"POST", "PUT", "DELETE"})
    void shouldBuildScopeKeyFromHttpServletRequest(String givenMethod) {
        // given
        HttpServletRequest given = mock(HttpServletRequest.class);
        when(given.getMethod()).thenReturn(givenMethod);
        when(given.getRequestURI()).thenReturn("/orders/123");
        when(given.getHeader("Idempotency-Key")).thenReturn("xyz");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertTrue(result.isPresent());
        IdemPotencyScopeKey key = result.get();
        assertEquals(givenMethod, key.method());
        assertEquals("/orders/123", key.path());
        assertEquals("xyz", key.idempotencyKey());
    }

    @ParameterizedTest
    @CsvSource({"GET", "Options"})
    void shouldNotBuildScopeKeyFromGetOrOptions(String givenMethod) {
        // given
        HttpServletRequest given = mock(HttpServletRequest.class);
        when(given.getMethod()).thenReturn(givenMethod);
        lenient().when(given.getRequestURI()).thenReturn("/orders/123");
        lenient().when(given.getHeader("Idempotency-Key")).thenReturn("xyz");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnOptionalEmptyOnThrowCheckedExceptionOnScopeKeyBuildsFromHttpServletRequest() {
        // given
        HttpServletRequest given = mock(HttpServletRequest.class);
        when(given.getMethod()).thenReturn("Post");
        when(given.getRequestURI()).thenReturn("/orders/123");
        when(given.getHeader("Idempotency-Key")).thenReturn(null);

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

}
