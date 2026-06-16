package uk.co.frankz.hmcts.dts.aws.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@DynamoDbBean
public class IdemPotencyEntity {

    public static final String KEY_FIELD = "id";

    public static final String EXPIRE_TTL_FIELD = "expiresAt";

    private String id;

    private String createdAt;

    private Long expiresAt;

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

        // expiryAt is set at safeSave

        return entity;
    }

    @DynamoDbAttribute(KEY_FIELD)
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute(EXPIRE_TTL_FIELD)
    public Long getExpiresAt() {
        return expiresAt;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
