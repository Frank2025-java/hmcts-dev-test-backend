package uk.co.frankz.hmcts.dts.model;

import java.time.LocalDateTime;

public record IdemPotencyRecord(
    IdemPotencyHash key,
    LocalDateTime createdAt,
    IdemPotencyHash requestHash,
    byte[] responseBody,
    int statusCode,
    String contentType
) {
}
