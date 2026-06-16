package uk.co.frankz.hmcts.dts.aws.dynamodb;

import uk.co.frankz.hmcts.dts.aws.Properties;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyRuntimeException;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;

import java.time.ZoneOffset;
import java.util.Optional;

public class IdemPotencyStoreImpl implements IdemPotencyStore {

    private final DynamoDbTableProvider<IdemPotencyEntity> table;

    public IdemPotencyStoreImpl() {

        // no extensions needed like in TaskStoreImpl
        this(new DynamoDbTableProvider<>(Properties.TABLE_IDEMPOTENCY, IdemPotencyEntity.class));
    }

    public IdemPotencyStoreImpl(DynamoDbTableProvider<IdemPotencyEntity> table) {
        this.table = table;
    }

    @Override
    public Optional<IdemPotencyRecord> findById(IdemPotencyScopeKey key) {
        IdemPotencyEntity foundEntity = table.find(key.sha256().value());
        IdemPotencyRecord foundDomain = foundEntity == null ? null : foundEntity.toDomain();
        return Optional.ofNullable(foundDomain);
    }

    @Override
    public void saveSafe(IdemPotencyRecord record) throws IdemPotencyAlreadyProcessedException {
        synchronized (this) {
            if (table.find(record.key().value()) != null) {
                throw new IdemPotencyAlreadyProcessedException();
            }

            try {
                IdemPotencyEntity entity = IdemPotencyEntity.toEntity(record);

                long ttlSeconds = record.createdAt()
                    .plus(Properties.TTL_IDEMPOTENCY)
                    .toInstant(ZoneOffset.UTC)
                    .getEpochSecond();

                entity.setExpiresAt(ttlSeconds);

                table.save(entity);
            } catch (Exception e) {
                throw new IdemPotencyRuntimeException(e);
            }
        }
    }
}
