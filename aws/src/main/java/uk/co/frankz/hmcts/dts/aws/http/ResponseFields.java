package uk.co.frankz.hmcts.dts.aws.http;

import uk.co.frankz.hmcts.dts.service.Header;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public record ResponseFields(
    String body,
    int status,
    Map<String, String> header
) {
    public ResponseFields(byte[] body, int status) {
        this(new String(body, StandardCharsets.UTF_8), status, Header.JSON);
    }

    public ResponseFields(byte[] body, int status, Map<String,String> contentType) {
        this(new String(body, StandardCharsets.UTF_8), status, contentType);
    }
}
