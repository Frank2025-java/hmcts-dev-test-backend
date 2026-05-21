package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyBlankFieldsException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyInvalidKeyException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class IdemPotencyKeyBuilderTest {

    @Mock
    HttpServletRequest request;

    private IdemPotencyKeyBuilder testSubject;

    private static final IdemPotencyScopeKey TEST_KEY = new IdemPotencyScopeKey("a", "b", "c");

    // static mock must be opened/closed manually
    private MockedStatic<IdemPotencyScopeKey> scopeKeyMock;

    @BeforeEach
    void setUp() {
        testSubject = new IdemPotencyKeyBuilder();
        scopeKeyMock = mockStatic(IdemPotencyScopeKey.class);
    }

    @AfterEach
    void tearDown() {
        scopeKeyMock.close();
    }

    @Test
    void returnsOptionalOfScopeKeyWhenNoExceptionThrown() {
        // given
        scopeKeyMock.when(() -> IdemPotencyScopeKey.scopeKey(request)).thenReturn(TEST_KEY);

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(request);

        // then
        assertTrue(result.isPresent());
        assertSame(TEST_KEY, result.get());
    }

    @Test
    void returnsEmptyOptionalWhenIdemPotencyExceptionThrown() {
        // given
        IdemPotencyException given = new IdemPotencyInvalidKeyException(new IdemPotencyBlankFieldsException());

        scopeKeyMock.when(() -> IdemPotencyScopeKey.scopeKey(request)).thenThrow(given);

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(request);

        // then
        assertTrue(result.isEmpty());
    }
}
