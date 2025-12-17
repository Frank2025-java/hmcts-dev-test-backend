package uk.co.frankz.hmcts.dts.model;

import java.util.UUID;

/**
 * LongIdentifierGenerator provides a method for a unique
 * identifier, which is required for searching by ID.
 *
 * When asked for, we can add a persistency framework
 * or store that replaces this functionality.
 * If sandbox org.apache.commons comes out with a good versioned
 * release, we could use org.apache.commons.id.LongIdentifierGenerator
 * instead.
 */
public interface LongIdentifierGenerator {
    static long nextLongIdentifier() {
        return UUID.randomUUID().getMostSignificantBits();
    }
}
