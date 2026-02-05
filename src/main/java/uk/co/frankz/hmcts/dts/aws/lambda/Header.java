package uk.co.frankz.hmcts.dts.aws.lambda;

import java.util.Map;

public interface Header {

    Map<String, String> JSON = Map.of("Content-Type", "application/json");

    Map<String, String> HTML = Map.of("Content-Type", "text/html");
}
