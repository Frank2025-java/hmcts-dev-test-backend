package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IdemPotencyScopeKeyBuilderTest {

    record TestRequestType(
        String method,
        String path,
        String key
    ) {
    }

    IdemPotencyScopeKeyBuilder<TestRequestType> testSubject
        = new IdemPotencyScopeKeyBuilder<>(
        TestRequestType::method,
        TestRequestType::path,
        TestRequestType::key
    );

    @ParameterizedTest
    @CsvSource({"POST", "PUT", "DELETE", "Delete"})
    void shouldBuildScopeKey(String givenMethod) {
        // given
        var given = new TestRequestType(givenMethod, "/orders/123", "xyz");

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

    @Test
    void shouldReturnEmptyKeyWithEmptyHeader() {
        // given
        var given = new TestRequestType("PUT", "/orders/123", "");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyKeyWithNoHeader() {
        // given
        var given = new TestRequestType("PUT", "/orders/123", null);

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyKeyWithEmptyPath() {
        // given
        var given = new TestRequestType("PUT", "", "123");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @ParameterizedTest
    @CsvSource(value = {"GET", "Options", "''", "NULL"}, nullValues = "NULL")
    void shouldNotBuildScopeKeyFromGetOrOptionsOrEmptyOrNull(String givenMethod) {
        // given
        var given = new TestRequestType(givenMethod, "/orders/123", "xyz");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(given);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }
}
