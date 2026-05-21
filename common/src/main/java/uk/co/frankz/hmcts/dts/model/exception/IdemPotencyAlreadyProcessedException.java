package uk.co.frankz.hmcts.dts.model.exception;

/**
 * If the same idempotency key is used with a request,
 * but that was not detected when processing started,
 * then check again whether to respond with the stored response.
 */
public class IdemPotencyAlreadyProcessedException extends IdemPotencyException {

    // RFC 8470 — Early Data (TLS 1.3)
    public IdemPotencyAlreadyProcessedException() {
        super(
            425, "The original request with idempotency key was processed in parallel. "
                + "Do not retry, but check request again."
        );
    }
}
