package uk.co.frankz.hmcts.dts.model.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class IdemPotencyIssue {

    private final int httpStatus;

    private final String message;

    public IdemPotencyIssue(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public IdemPotencyIssue(String message) {
        this(SC_INTERNAL_SERVER_ERROR, message);
    }

    public IdemPotencyIssue(Throwable cause) {
        this(SC_INTERNAL_SERVER_ERROR, toString(cause));
    }

    public String getMessage() {
        return message;
    }

    public static String toString(Throwable e) {
        if (e instanceof IdemPotencyException || e instanceof IdemPotencyRuntimeException) {
            // Do not double wrap IdemPotencyError
            return e.getClass().getSimpleName() + ":" + e.getMessage();
        } else if (StringUtils.isBlank(e.getMessage())) {
            // Do not rely on e.getMessage, as, for example, null pointers have a blank message
            return e.toString();
        } else {
            // Return some feedback on exception, but do not return stacktrace to reveal to much
            return e.getMessage();
        }
    }

    /**
     * Handles both checked and unchecked idempotency exceptions.
     *
     * <p>Checked exceptions indicate expected, recoverable idempotency failures
     * (for example, a mismatched request hash). These should be caught by the caller
     * and translated into an appropriate HTTP error response using {@code sendError}.
     *
     * <p>Unchecked exceptions indicate unexpected internal failures (for example,
     * hashing errors or programming mistakes). These are not meant to be recovered
     * from and should result in an HTTP 500 Internal Server Error.
     *
     * @param response the HTTP response to write the status code and message to
     * @throws IOException if writing to the response fails
     */
    public void sendError(HttpServletResponse response) throws IOException {
        response.sendError(httpStatus, message);
    }
}
