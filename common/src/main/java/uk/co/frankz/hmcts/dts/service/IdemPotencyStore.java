package uk.co.frankz.hmcts.dts.service;

import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;

import java.util.Optional;

/**
 * Persistence abstraction for storing and retrieving idempotency records.
 *
 * <p>An {@code IdemPotencyStore} ensures that HTTP operations identified by an
 * {@link IdemPotencyScopeKey} are executed at most once.
 *
 * <p>Implementations must be thread‑safe and guarantee that concurrent requests
 * for the same idempotency key do not result in duplicate processing.
 */
public interface IdemPotencyStore {

    /**
     * Retrieves a previously idempotency record for the given key.
     *
     * <p>If a record exists, the stored
     * response body, status code, and content type can be returned directly
     * to the client without re‑executing the underlying operation.
     *
     * @param key the scoped idempotency key derived from method, path, and header
     * @return an {@link Optional} containing the record, or empty if no record exists
     */
    Optional<IdemPotencyRecord> findById(IdemPotencyScopeKey key);

    /**
     * Stores the idempotency entry with response,
     * so that subsequent identical requests can return the cached response
     * without reprocessing.
     *
     * @param record the idempotency record
     */
    void saveSafe(IdemPotencyRecord record) throws IdemPotencyAlreadyProcessedException;

    /**
     * Removes expired idempotency records whose creation timestamp is older
     * than the given cutoff.
     * AWS DynamoDB can have a TTL attribute on the table, so will not need
     * an implementation, but EclipseStore will need some regular clean-up.
     *
     * @param cutoffTimestamp records older than this epoch‑millis timestamp
     *                        should be removed
     */
    void cleanupExpired(long cutoffTimestamp);
}
