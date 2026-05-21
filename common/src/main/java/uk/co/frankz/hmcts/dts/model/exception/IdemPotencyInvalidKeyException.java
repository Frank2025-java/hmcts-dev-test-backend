package uk.co.frankz.hmcts.dts.model.exception;

/**
 * Idempotency key cannot be generated from blank values.
 * You might want to continue without an idempotency key.
 */
public class IdemPotencyInvalidKeyException extends IdemPotencyException {

    public IdemPotencyInvalidKeyException(IdemPotencyBlankFieldsException e) {
        super(e);
    }
}
