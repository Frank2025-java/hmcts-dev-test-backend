package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyMismatchException;
import uk.co.frankz.hmcts.dts.service.ServletFilterMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.time.LocalDateTime.now;
import static uk.co.frankz.hmcts.dts.model.IdemPotencyHash.sha256Hex;

@Component
public class WrapperResponseFilter {

    public void doFilterUsingIdemPotency(
        IdemPotencyRecord idemPotencyRecord,
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, IdemPotencyMismatchException {

        byte[] requestBody = request.getInputStream().readAllBytes();
        IdemPotencyHash hash = sha256Hex(requestBody);

        if (idemPotencyRecord.requestHash().equals(hash)) {

            writeResponse(response, idemPotencyRecord);

        } else {
            throw new IdemPotencyMismatchException();
        }
    }

    public IdemPotencyRecord doFilterExtractIdemPotency(
        IdemPotencyScopeKey key,
        HttpServletRequest request,
        HttpServletResponse response,
        ServletFilterMethod<HttpServletRequest, HttpServletResponse> doFilter
    ) throws IOException, ServletException {

        byte[] requestBody = request.getInputStream().readAllBytes();
        HttpServletRequest wrappedRequest = replaceInputStream(request, requestBody);

        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        HttpServletResponse wrappedResponse = replaceOutputStream(response, responseBuffer);

        // let wrapped response stream get written into
        doFilter.accept(wrappedRequest, wrappedResponse);

        byte[] responseBody = responseBuffer.toByteArray();
        int status = wrappedResponse.getStatus();
        String contentType = wrappedResponse.getContentType();

        IdemPotencyRecord record = new IdemPotencyRecord(
            key.sha256(),
            now(),
            sha256Hex(requestBody),
            responseBody,
            status,
            contentType
        );

        // Restore re-write output stream for the client
        writeResponse(response, record);

        return record;
    }

    private @NonNull HttpServletRequest replaceInputStream(final HttpServletRequest request, final byte[] requestBody) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public ServletInputStream getInputStream() {
                return new ServletInputStreamWrapper(requestBody);
            }
        };
    }

    private @NonNull HttpServletResponse replaceOutputStream(
        final HttpServletResponse response,
        final ByteArrayOutputStream buffer) {

        return new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() {
                return new ServletOutputStreamWrapper(buffer);
            }
        };
    }

    private void writeResponse(HttpServletResponse response, IdemPotencyRecord rec)
        throws IOException {

        response.setStatus(rec.statusCode());
        response.setContentType(rec.contentType());
        response.getOutputStream().write(rec.responseBody());
    }
}


