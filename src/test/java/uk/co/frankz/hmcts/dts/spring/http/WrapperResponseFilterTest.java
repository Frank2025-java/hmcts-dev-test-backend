package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.service.ServletFilterMethod;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WrapperResponseFilterTest {

    private WrapperResponseFilter testSubject;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private ServletInputStream mockRequestStream;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private ServletOutputStream mockResponseStream;

    @Mock
    private ServletFilterMethod<HttpServletRequest, HttpServletResponse> mockDoFilterMethod;

    @Captor
    ArgumentCaptor<HttpServletRequest> wrappedRequest;

    @Captor
    ArgumentCaptor<HttpServletResponse> wrappedResponse;

    public static final IdemPotencyScopeKey TEST_KEY = new IdemPotencyScopeKey("POST", "/empty", "xyz");

    public static final byte[] TEST_REQ_BODY = "hello".getBytes();
    public static final byte[] TEST_RES_BODY = "world".getBytes();
    public static final int TEST_STATUS = 201;
    public static final String TEST_CONTENT = "text/plain";

    @BeforeEach
    void setUp() throws IOException {
        testSubject = new WrapperResponseFilter();

        lenient().when(mockRequest.getInputStream()).thenReturn(mockRequestStream);
        lenient().when(mockRequestStream.readAllBytes()).thenReturn(TEST_REQ_BODY);
        lenient().when(mockResponse.getOutputStream()).thenReturn(mockResponseStream);
    }

    @Test
    void onlyReadOnceRequest() throws IOException, ServletException {
        // given
        doAnswer(invocation -> null)
            .when(mockDoFilterMethod)
            .accept(wrappedRequest.capture(), wrappedResponse.capture());

        // when
        testSubject.doFilterExtractIdemPotency(TEST_KEY, mockRequest, mockResponse, mockDoFilterMethod);

        // then
        verify(mockRequestStream, times(1)).readAllBytes();
        assertInstanceOf(ServletInputStreamWrapper.class, wrappedRequest.getValue().getInputStream());
    }

    @Test
    void onlyWriteOnceResponse() throws IOException, ServletException {
        // given
        doAnswer(invocation -> {
            HttpServletResponse wrapped = invocation.getArgument(1);
            wrapped.getOutputStream().write(TEST_RES_BODY);
            return null;
        }).when(mockDoFilterMethod).accept(wrappedRequest.capture(), wrappedResponse.capture());

        // when
        testSubject.doFilterExtractIdemPotency(TEST_KEY, mockRequest, mockResponse, mockDoFilterMethod);

        // then
        verify(mockResponseStream, times(1)).write(TEST_RES_BODY);
        assertInstanceOf(ServletOutputStreamWrapper.class, wrappedResponse.getValue().getOutputStream());
    }

    @Test
    void wrapsRequestAndResponseAndReturnsIdempotencyRecord() throws IOException, ServletException {

        // given
        doAnswer(invocation -> {
            HttpServletResponse wrapped = invocation.getArgument(1);
            wrapped.getOutputStream().write(TEST_RES_BODY);
            wrapped.setStatus(TEST_STATUS);
            wrapped.setContentType(TEST_CONTENT);
            return null;
        }).when(mockDoFilterMethod).accept(wrappedRequest.capture(), wrappedResponse.capture());

        // when
        IdemPotencyRecord record = testSubject.doFilterExtractIdemPotency(
            TEST_KEY,
            mockRequest,
            mockResponse,
            mockDoFilterMethod
        );

        // then
        verify(mockDoFilterMethod).accept(any(), any());
        assertEquals(TEST_KEY.sha256(), record.key());
        assertNotNull(record.createdAt());
        assertNotNull(record.requestHash());
        verify(mockResponseStream).write(TEST_RES_BODY);
        verify(mockResponse).setStatus(TEST_STATUS);
        verify(mockResponse).setContentType(TEST_CONTENT);
    }

    @Test
    void handlesEmptyRequestBody() throws Exception {

        // given empty request
        byte[] givenEmpty = new byte[0];
        when(mockRequest.getInputStream()).thenReturn(new ServletInputStreamWrapper(givenEmpty));
        IdemPotencyHash expectedHash = IdemPotencyHash.sha256Hex(givenEmpty);

        // when
        IdemPotencyRecord record = testSubject.doFilterExtractIdemPotency(
            TEST_KEY, mockRequest, mockResponse, mockDoFilterMethod
        );

        // then
        assertEquals(expectedHash, record.requestHash());
    }

    @Test
    void propagatesExceptionsFromDoFilter() throws Exception {

        // given
        doThrow(new ServletException("boom"))
            .when(mockDoFilterMethod).accept(any(), any());

        // when, then propagate exception
        assertThrows(
            ServletException.class, () ->
                testSubject.doFilterExtractIdemPotency(
                    new IdemPotencyScopeKey("POST", "/err", "k"),
                    mockRequest,
                    mockResponse,
                    mockDoFilterMethod
                )
        );
    }
}
