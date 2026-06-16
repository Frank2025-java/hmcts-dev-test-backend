package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyIssue;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyMismatchException;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdemPotencyFilterTest {

    private IdemPotencyFilter testSubject;

    @Mock
    private IdemPotencyStore mockStore;

    @Mock
    private IdemPotencyScopeKeyBuilder mockKeyBuilder;

    @Mock
    private MockFilterChain mockFilterChain;

    @Mock
    private WrapperResponseFilter mockWrapperFilterChain;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private IdemPotencyMismatchException mockMismatchEx;

    @Mock
    private IdemPotencyIssue mockIssue;

    private static final IdemPotencyScopeKey TEST_KEY = new IdemPotencyScopeKey("POST", "/test", "123");

    private static final byte[] TEST_BODY = "stored".getBytes(UTF_8);

    private static final int TEST_STATUS = 200;

    private static final String TEST_CONTENT = "text/plain";

    private static final IdemPotencyHash TEST_HASH = IdemPotencyHash.sha256Hex("test hash".getBytes(UTF_8));

    private static final IdemPotencyRecord TEST_IDEM_RECORD = new IdemPotencyRecord(
        TEST_KEY.sha256(),
        LocalDateTime.of(2026, 5, 11, 14, 57, 30, 123456),
        TEST_HASH,
        TEST_BODY,
        TEST_STATUS,
        TEST_CONTENT
    );

    @BeforeEach
    void setup() {
        testSubject = new IdemPotencyFilter(mockStore, mockKeyBuilder, mockWrapperFilterChain);

        lenient().when(mockKeyBuilder.build(any())).thenReturn(Optional.of(TEST_KEY));
        lenient().when(mockMismatchEx.getIssue()).thenReturn(mockIssue);
    }

    @Test
    void shouldStoreResponse() throws Exception {

        // given
        when(mockStore.findById(any())).thenReturn(Optional.empty());
        when(mockWrapperFilterChain.doFilterExtractIdemPotency(eq(TEST_KEY), any(), any(), any())).thenReturn(
            TEST_IDEM_RECORD);

        // when
        testSubject.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockStore).saveSafe(TEST_IDEM_RECORD);
    }

    @Test
    void shouldReturnStoredResponse() throws Exception {

        // given
        when(mockStore.findById(any())).thenReturn(Optional.of(TEST_IDEM_RECORD));

        // when
        testSubject.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockWrapperFilterChain).doFilterUsingIdemPotency(TEST_IDEM_RECORD, mockRequest, mockResponse);
        verifyNoInteractions(mockFilterChain);
        verify(mockStore, never()).saveSafe(TEST_IDEM_RECORD);
    }

    @Test
    void shouldWriteErrorOnIdemPotencyException() throws Exception {

        // given
        when(mockStore.findById(any())).thenReturn(Optional.of(TEST_IDEM_RECORD));
        doThrow(mockMismatchEx).when(mockWrapperFilterChain).doFilterUsingIdemPotency(any(), any(), any());

        // when
        testSubject.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verifyNoInteractions(mockFilterChain);
        verify(mockStore, never()).saveSafe(TEST_IDEM_RECORD);
        verify(mockResponse).sendError(anyInt(), any());
    }

    @Test
    void shouldPropagateRuntimeExceptions() {

        // given
        RuntimeException givenEx = new RuntimeException();
        when(mockKeyBuilder.build(any())).thenThrow(givenEx);

        // when
        RuntimeException actual = assertThrows(
            RuntimeException.class,
            () -> testSubject.doFilter(mockRequest, mockResponse, mockFilterChain)
        );

        // then
        verifyNoInteractions(mockFilterChain);
        assertSame(givenEx, actual);
    }

    @Test
    void shouldPropagateRuntimeExceptionsFromWrappedFilter() throws Exception {

        // given
        when(mockStore.findById(any())).thenReturn(Optional.of(TEST_IDEM_RECORD));
        RuntimeException givenEx = new RuntimeException();
        doThrow(givenEx).when(mockWrapperFilterChain).doFilterUsingIdemPotency(any(), any(), any());

        // when
        RuntimeException actual = assertThrows(
            RuntimeException.class,
            () -> testSubject.doFilter(mockRequest, mockResponse, mockFilterChain)
        );

        // then
        verifyNoInteractions(mockFilterChain);
        assertSame(givenEx, actual);
    }

    @Test
    void passesThroughWhenNoKey() throws Exception {
        // given
        when(mockKeyBuilder.build(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        // when
        testSubject.doFilter(mockRequest, mockResponse, mockFilterChain);

        // them
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        verifyNoInteractions(mockStore);
    }
}
