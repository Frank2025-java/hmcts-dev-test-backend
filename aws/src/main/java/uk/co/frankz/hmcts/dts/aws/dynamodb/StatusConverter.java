package uk.co.frankz.hmcts.dts.aws.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import uk.co.frankz.hmcts.dts.model.Status;

public class StatusConverter
    extends BaseConvertor<Status>
    implements AttributeConverter<Status> {

    public StatusConverter() {
        super((s) -> s.name(), Status::parse, Status.class);
    }
}
