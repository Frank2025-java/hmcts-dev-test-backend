package uk.co.frankz.hmcts.dts.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import software.xdev.spring.data.eclipse.store.repository.interfaces.EclipseStoreRepository;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;

import java.time.ZoneOffset;
import java.util.Optional;

import static uk.co.frankz.hmcts.dts.spring.IdemPotencyEntity.toEntity;

/**
 * IdemPotencyRepository is the API for storing and retrieving {@link IdemPotencyRecord} instances
 * in an EclipseStore-backed persistence layer.
 * <p>
 * This repository provides CRUD-style access to idempotency records used to enforce
 * exactly once semantics for HTTP operations. Each record is uniquely identified by
 * its idempotency key, which acts as the primary identifier.
 * <p>
 * The repository is intentionally storage-agnostic at the domain level: the
 * {@link IdemPotencyRecord} type contains no persistence annotations, and this
 * repository serves as the EclipseStore-specific adapter behind the generic
 * {@code IdempotencyStore} abstraction. Other persistence backends (e.g. DynamoDB)
 * implement the same abstraction without relying on this interface.
 * <p>
 * Typical usage:
 * <ul>
 *     <li>Check whether a completed idempotency record already exists for a key.</li>
 *     <li>Persist a new record when a request is processed.</li>
 *  </ul>
 * <p>
 *  Implementations of this repository are provided automatically by Spring Data
 *  EclipseStore when {@code @EnableEclipseStoreRepositories} is active.
 */
@Repository
public interface IdemPotencyRepository extends EclipseStoreRepository<IdemPotencyEntity, String>, IdemPotencyStore {

    @Override
    default void cleanupExpired(long cutoffTimestamp) {
        findAll().stream()
            .filter(record -> record
                .toDomain()
                .createdAt()
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
                < cutoffTimestamp)
            .forEach(this::delete);
    }

    /**
     * Saves the given idempotency record if it has a valid idem potency scope key,
     * and if it has not been added by a parallel thread.
     * <p>
     * This method provides a simple concurrency guard to ensure that two threads
     * do not create the same idempotency entry simultaneously. The operation is
     * performed inside a {@code synchronized} block on the repository instance,
     * making the check‑then‑save sequence atomic.
     *
     * @param record the idempotency record to persist
     */
    @Override
    @NonNull
    default void saveSafe(@NotNull IdemPotencyRecord record) throws IdemPotencyAlreadyProcessedException {
        synchronized (this) {
            if (existsById(record.key().value())) {
                throw new IdemPotencyAlreadyProcessedException();
            }

            save(toEntity(record));
        }
    }

    @Override
    default Optional<IdemPotencyRecord> findById(IdemPotencyScopeKey key) {
        Optional<IdemPotencyEntity> entity = findById(key.sha256().value());
        return entity.map(IdemPotencyEntity::toDomain);
    }
}
