package uk.co.frankz.hmcts.dts.model.exception;

import jakarta.servlet.http.HttpServletResponse;

/**
 * If the same idempotency key is used with a different request payload
 * an error should be returned. Advised is to return HTTP status 409.
 */
public class IdemPotencyMismatchException extends IdemPotencyException {

    public IdemPotencyMismatchException() {
        super(HttpServletResponse.SC_CONFLICT, "Idempotency key mismatch");
    }
}
