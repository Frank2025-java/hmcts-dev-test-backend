package uk.co.frankz.hmcts.dts.model.exception;

import jakarta.servlet.ServletException;

/**
 * A Checked exception base for idempotency issues.
 **/
public class IdemPotencyException extends ServletException {

    private final IdemPotencyIssue issue;

    public IdemPotencyException(int httpStatus, String message) {
        super(message);
        this.issue = new IdemPotencyIssue(httpStatus, message);
    }

    public IdemPotencyException(Throwable cause) {
        super(cause);
        if (cause instanceof IdemPotencyRuntimeException runtimeException) {
            // convert to Checked Idem potency exception but keep the issue
            this.issue = runtimeException.getIssue();
        } else {
            this.issue = new IdemPotencyIssue(cause);
        }
    }

    public IdemPotencyIssue getIssue() {
        return issue;
    }
}
