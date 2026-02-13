package uk.co.frankz.hmcts.dts.aws.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;

import java.util.UUID;

public class AutoUuidConverter
    extends BaseConvertor<UUID>
    implements AttributeConverter<UUID> {

    public AutoUuidConverter() {
        super(UUID::fromString, UUID.class);
    }
}
