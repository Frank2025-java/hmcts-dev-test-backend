package uk.co.frankz.hmcts.dts.model.exception;

/**
 * A Checked exception base for idempotency issues.
 **/
public class IdemPotencyException extends Exception {

    private final IdemPotencyIssue issue;

    public IdemPotencyException(int httpStatus, String message) {
        super(message);
        this.issue = new IdemPotencyIssue(httpStatus, message);
    }

    public IdemPotencyIssue getIssue() {
        return issue;
    }
}
