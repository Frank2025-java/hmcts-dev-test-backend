package uk.co.frankz.hmcts.dts.model.exception;

import org.apache.commons.lang3.StringUtils;

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

    public int getHttpStatus() {
        return httpStatus;
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
}
