package uk.co.frankz.hmcts.dts.service;

import java.util.Map;

public interface Header {

    String CONTENT_TYPE = "Content-Type";

    Map<String, String> JSON = Map.of(CONTENT_TYPE, "application/json");

    Map<String, String> HTML = Map.of(CONTENT_TYPE, "text/html");

    static Map<String, String> contentOfType(String contentType) {
        return Map.of(CONTENT_TYPE, contentType);
    }
}
