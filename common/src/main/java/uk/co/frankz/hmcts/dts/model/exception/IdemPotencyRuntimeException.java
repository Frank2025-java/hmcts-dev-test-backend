package uk.co.frankz.hmcts.dts.model.exception;

/**
 * Custom unchecked exception for idempotency functionality.
 */
public class IdemPotencyRuntimeException extends RuntimeException {

    private final IdemPotencyIssue issue;

    public IdemPotencyRuntimeException(Throwable cause) {
        super(cause);
        this.issue = new IdemPotencyIssue(cause);
    }

    public IdemPotencyRuntimeException(String message) {
        super(message);
        this.issue = new IdemPotencyIssue(message);
    }

    public IdemPotencyIssue getIssue() {
        return issue;
    }
}
