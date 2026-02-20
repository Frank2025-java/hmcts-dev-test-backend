package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegrationProps;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.lambda.Function;

import java.util.List;

import static software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion.VERSION_2_0;

/**
 * Utility class to create a CDK construct for Routes between APIGateway and Lambda.
 */
public class LambdaRouteBuilder {

    private final String resourcePath;

    private final List<HttpMethod> httpMethods;

    private final HttpLambdaIntegrationProps version2;

    public LambdaRouteBuilder(String resourcePath, HttpMethod httpMethod) {
        this(resourcePath, new HttpMethod[]{httpMethod});
    }

    public LambdaRouteBuilder(String resourcePath, HttpMethod... httpMethods) {
        this.resourcePath = resourcePath;
        this.httpMethods = List.of(httpMethods);

        this.version2 = HttpLambdaIntegrationProps.builder().payloadFormatVersion(VERSION_2_0).build();
    }

    /**
     * Creates a route construct for CDK to target a Lambda that can be added to an APIGateway.
     * The http path is provided with the constructor arguments.
     *
     * @param lambda target for APIGateway.
     * @return Instance of a CDK Level 2 construct for a Route on an Api Gateway
     */
    public AddRoutesOptions build(Function lambda, String id) {

        var httpApiIntegrate = new HttpLambdaIntegration(id, lambda, version2);

        return AddRoutesOptions
            .builder()
            .path(resourcePath)
            .methods(httpMethods)
            .integration(httpApiIntegrate)
            .build();
    }

}
