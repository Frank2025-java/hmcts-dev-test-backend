package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod;
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions;
import software.constructs.Construct;

import java.util.List;

/**
 * Utility class for generating CORS CDK level 2 construct for the API Gateway.
 * <br>
 * Modern browsers (or rather web application resource) will ask the API Gateway
 * to check if the browser's origin is whitelisted, with a quick pre-flight http call.
 * This preflight call is answered with either the absence of fields in the header,
 * or presence. Among the fields are "access-control-allow-origin",
 * "access-control-allow-methods" and "access-control-allow-headers".
 * The browser would block further action with a CORS violation reason,
 * which protects against a malicious website using
 * your cookies and identity, to secretly making requests with your browser.
 */
public class CrossOriginResourcesBuilder {

    /**
     * Creates CDK Construct to allow b to pass into the API Gateway Construct.
     *
     * @param scope of construct
     * @param id    as CDK identifier in the generated asset files
     * @param list  whitelisted origins
     * @return instance of the level 2 CDK construct for ApiGateway of type Http API
     */
    public CorsPreflightOptions build(Construct scope, String id, String[] list) {

        return CorsPreflightOptions.builder()
            .allowOrigins(List.of(list))
            .allowMethods(List.of(CorsHttpMethod.values()))
            .allowHeaders(List.of("*"))
            .allowCredentials(true)
            .build();
    }

}
