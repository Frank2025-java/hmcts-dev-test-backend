package uk.co.frankz.hmcts.dts.aws.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdemPotencyScopeKeyBuilderTest {

    IdemPotencyScopeKeyBuilder testSubject = new IdemPotencyScopeKeyBuilder();

    @Mock
    Map<String, String> mockHeaders;

    @Mock
    APIGatewayV2HTTPEvent.RequestContext mockRequest;

    @Mock
    APIGatewayV2HTTPEvent.RequestContext.Http mockHttp;

    static APIGatewayV2HTTPEvent TEST_EVENT = new APIGatewayV2HTTPEvent();

    @BeforeEach
    void setUp() {
        when(mockRequest.getHttp()).thenReturn(mockHttp);
        TEST_EVENT.setHeaders(mockHeaders);
        TEST_EVENT.setRequestContext(mockRequest);
    }

    @Test
    void shouldBuildScopeKeyFromHttpRequestOfApiGateway() {
        // given
        when(mockHttp.getMethod()).thenReturn("PUT");
        when(mockHttp.getPath()).thenReturn("/orders/123");
        when(mockHeaders.get("idempotency-key")).thenReturn("xyz");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(TEST_EVENT);

        // then
        assertNotNull(result);
        assertTrue(result.isPresent());
        IdemPotencyScopeKey key = result.get();
        assertEquals("PUT", key.method());
        assertEquals("/orders/123", key.path());
        assertEquals("xyz", key.idempotencyKey());
    }

    @Test
    void shouldReturnEmptyKeyWithHeaderNotNormalisedApiGateway() {
        // given
        when(mockHttp.getMethod()).thenReturn("PUT");
        when(mockHttp.getPath()).thenReturn("/orders/123");
        when(mockHeaders.get("Idempotency-Key")).thenReturn("xyz");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(TEST_EVENT);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent(), "\"Idempotency-Key\" should be normalised to lower case for ApiGateway.");
    }

    @Test
    void shouldReturnEmptyKeyWithEmptyHeader() {
        // given
        when(mockHttp.getMethod()).thenReturn("PUT");
        when(mockHttp.getPath()).thenReturn("/orders/123");
        when(mockHeaders.get("Idempotency-Key")).thenReturn(null);

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(TEST_EVENT);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent(), "\"idempotency-key\" should be present.");
    }

    @Test
    void shouldReturnEmptyKeyWithNoHeader() {
        // given
        TEST_EVENT.setHeaders(null);
        when(mockHttp.getMethod()).thenReturn("PUT");
        when(mockHttp.getPath()).thenReturn("/orders/123");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(TEST_EVENT);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyKeyWithEmptyPath() {
        // given
        when(mockHttp.getMethod()).thenReturn("PUT");
        when(mockHttp.getPath()).thenReturn("");
        when(mockHeaders.get("idempotency-key")).thenReturn("123");

        // when
        Optional<IdemPotencyScopeKey> result = testSubject.build(TEST_EVENT);

        // then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

}
