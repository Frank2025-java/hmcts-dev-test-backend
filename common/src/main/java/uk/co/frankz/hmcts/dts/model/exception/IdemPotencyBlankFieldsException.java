package uk.co.frankz.hmcts.dts.model.exception;

public class IdemPotencyBlankFieldsException extends IdemPotencyRuntimeException {
    public IdemPotencyBlankFieldsException() {
        super("Idempotency key cannot be created from blank fields.");
    }
}
