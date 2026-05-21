package uk.co.frankz.hmcts.dts.model;

import jakarta.annotation.Nonnull;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyHash256Exception;

import java.security.MessageDigest;
import java.util.HexFormat;

public record IdemPotencyHash(String value) {
    public IdemPotencyHash {
        if (value == null || value.isBlank()) {
            throw new IdemPotencyHash256Exception("Hash cannot be null or blank");
        }
        // enforce hex + length
        if (value.length() != 64) {
            throw new IdemPotencyHash256Exception("Invalid SHA-256 hex length");
        }
    }

    public static IdemPotencyHash sha256Hex(byte[] bytes) throws IdemPotencyHash256Exception {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return new IdemPotencyHash(HexFormat.of().formatHex(digest.digest(bytes)));
        } catch (Exception e) {
            throw new IdemPotencyHash256Exception(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IdemPotencyHash other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        return value;
    }
}
