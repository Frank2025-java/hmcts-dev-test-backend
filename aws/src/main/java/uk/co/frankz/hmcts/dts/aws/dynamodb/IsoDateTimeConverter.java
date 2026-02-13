package uk.co.frankz.hmcts.dts.aws.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class IsoDateTimeConverter
    extends BaseConvertor<LocalDateTime>
    implements AttributeConverter<LocalDateTime> {

    public IsoDateTimeConverter() {
        super(ISO_DATE_TIME::format, (s) -> LocalDateTime.parse(s, ISO_DATE_TIME), LocalDateTime.class);
    }

}
