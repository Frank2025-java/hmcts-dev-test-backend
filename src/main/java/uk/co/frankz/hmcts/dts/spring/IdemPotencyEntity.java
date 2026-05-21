package uk.co.frankz.hmcts.dts.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Getter
@Setter
public class IdemPotencyEntity {

    @Id
    private String id;

    private String createdAt;

    private String requestHash;

    private byte[] responseBody;

    private int statusCode;

    private String contentType;

    public IdemPotencyRecord toDomain() {
        return new IdemPotencyRecord(
            new IdemPotencyHash(id),
            LocalDateTime.parse(createdAt, ISO_DATE_TIME),
            new IdemPotencyHash(requestHash),
            responseBody,
            statusCode,
            contentType
        );
    }

    public static IdemPotencyEntity toEntity(IdemPotencyRecord record) {
        IdemPotencyEntity entity = new IdemPotencyEntity();
        entity.setId(record.key().value());
        entity.setCreatedAt(record.createdAt().format(ISO_DATE_TIME));
        entity.setRequestHash(record.requestHash().value());
        entity.setResponseBody(record.responseBody());
        entity.setStatusCode(record.statusCode());
        entity.setContentType(record.contentType());
        return entity;
    }

}
