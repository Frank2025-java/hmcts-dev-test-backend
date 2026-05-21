package uk.co.frankz.hmcts.dts.model;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyBlankFieldsException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyHash256Exception;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyInvalidKeyException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static uk.co.frankz.hmcts.dts.model.IdemPotencyHash.sha256Hex;

public record IdemPotencyScopeKey(
    String method,
    String path,
    String idempotencyKey
) {
    public IdemPotencyScopeKey {
        if (isBlank(method) || isBlank(path) || isBlank(idempotencyKey)) {
            throw new IdemPotencyBlankFieldsException();
        }
    }

    public static IdemPotencyScopeKey scopeKey(HttpServletRequest request) throws IdemPotencyException {

        try {
            return new IdemPotencyScopeKey(
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("Idempotency-Key") // returns null when not present
            );
        } catch (IdemPotencyBlankFieldsException e) {
            throw new IdemPotencyInvalidKeyException(e);
        } catch (Exception e) {
            throw new IdemPotencyException(e);
        }
    }

    /**
     * Canonical string representation used for hashing or storage.
     * Example: "POST:/payments:abc123"
     */
    private String canonical() {
       return method.toUpperCase() + ":" + path + ":" + idempotencyKey;
    }

    /**
     * SHA‑256 hash of the canonical key.
     * Useful when storing keys in DynamoDB or EclipseStore.
     */
    public IdemPotencyHash sha256() throws IdemPotencyHash256Exception {
            return sha256Hex(canonical().getBytes(UTF_8));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdemPotencyScopeKey other) {

                return this.sha256().equals(other.sha256());

        }
        if (obj instanceof IdemPotencyHash other) {

            return this.sha256().equals(other);

        }
        return false;
    }

    @Override
    @NotNull
    public int hashCode() {
        return sha256().hashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        return reflectionToString(this);
    }
}
