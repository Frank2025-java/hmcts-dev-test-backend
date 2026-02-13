package uk.co.frankz.hmcts.dts.aws.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import uk.co.frankz.hmcts.dts.model.Status;

public class StatusConverter implements AttributeConverter<Status> {

    @Override
    public AttributeValue transformFrom(Status input) {
        return AttributeValue.fromS(input.toString());
    }

    @Override
    public Status transformTo(AttributeValue input) {
        return Status.parse(input.s());
    }

    @Override
    public EnhancedType<Status> type() {
        return EnhancedType.of(Status.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
