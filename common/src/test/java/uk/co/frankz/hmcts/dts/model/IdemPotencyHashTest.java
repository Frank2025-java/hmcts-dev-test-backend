package uk.co.frankz.hmcts.dts.model;

import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyHash256Exception;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdemPotencyHashTest {

    @Test
    void constructorRejectsNull() {
        assertThrows(
            IdemPotencyHash256Exception.class,
            () -> new IdemPotencyHash(null)
        );
    }

    @Test
    void constructorRejectsBlank() {
        assertThrows(
            IdemPotencyHash256Exception.class,
            () -> new IdemPotencyHash("   ")
        );
    }

    @Test
    void constructorRejectsInvalidLength() {
        assertThrows(
            IdemPotencyHash256Exception.class,
            () -> new IdemPotencyHash("abc123")
        ); // too short
    }

    @Test
    void constructorAcceptsValid64CharHex() {
        String hex64 = "a".repeat(64);
        IdemPotencyHash hash = new IdemPotencyHash(hex64);
        assertEquals(hex64, hash.value());
    }

    @Test
    void sha256HexComputesCorrectHash() throws Exception {
        byte[] input = "hello".getBytes(StandardCharsets.UTF_8);

        // expected using real SHA-256
        var digest = MessageDigest.getInstance("SHA-256");
        String expected = HexFormat.of().formatHex(digest.digest(input));

        IdemPotencyHash hash = IdemPotencyHash.sha256Hex(input);

        assertEquals(expected, hash.value());
        assertEquals(64, hash.value().length());
    }

    @Test
    void equalsAndHashCodeBasedOnValue() {
        String hex = "b".repeat(64);

        IdemPotencyHash h1 = new IdemPotencyHash(hex);
        IdemPotencyHash h2 = new IdemPotencyHash(hex);

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentValues() {
        IdemPotencyHash h1 = new IdemPotencyHash("c".repeat(64));
        IdemPotencyHash h2 = new IdemPotencyHash("d".repeat(64));

        assertNotEquals(h1, h2);
    }

    @Test
    void toStringReturnsUnderlyingValue() {
        String hex = "e".repeat(64);
        IdemPotencyHash hash = new IdemPotencyHash(hex);

        assertEquals(hex, hash.toString());
    }

    @Test
    void toStringIsNonNull() {
        IdemPotencyHash hash = new IdemPotencyHash("f".repeat(64));
        assertNotNull(hash.toString());
    }
}


