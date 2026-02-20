package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.apigatewayv2.CfnApiMapping;
import software.amazon.awscdk.services.apigatewayv2.CfnStage;
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

/**
 * Utility class for generating API Gateway constructs for CDK.
 */
public class ApiGatewayBuilder {

    private final String displayName;

    public ApiGatewayBuilder(String name) {
        this.displayName = name;
    }

    /**
     * Creates CDK Constructs for an API Gateway on AWS.
     *
     * @param scope     of construct
     * @param id        as CDK identifier in the generated asset files
     * @param apiDomain reference to the subdomain for this api gateway
     * @param cors      the allowed origins of a browser CORS request
     * @return instance of the level 2 CDK construct for ApiGateway of type Http API
     */
    public HttpApi build(Construct scope, String id, DomainName apiDomain, CorsPreflightOptions cors) {

        HttpApiProps props = HttpApiProps.builder()
            .apiName(displayName)
            .createDefaultStage(false)
            .corsPreflight(cors)
            .build();

        return new HttpApi(scope, id, props);
    }

    /**
     * Creates constructs for the subdomain that is served by the API Gateway.
     * <br>
     * This code is setting up construction for the Default Stage,
     * i.e. for setting api up different environments (Production, Development)
     * the code needs to be amended.
     *
     * @param scope     of construct
     * @param id        as CDK identifier in the generated asset files
     * @param api       constructed (build) HttpApi
     * @param subDomain sub domain
     * @param basePath  like "/task"
     * @param stage     like the default stage level 1 CDK construct
     */
    public void buildMap(Construct scope, String id,
                         HttpApi api,
                         DomainName subDomain,
                         String basePath,
                         CfnStage stage) {

        var map = CfnApiMapping.Builder
            .create(scope, id)
            .apiId(api.getHttpApiId())
            .domainName(subDomain)
            .stage(stage.getStageName())
            .apiMappingKey(basePath)
            .build();

        // Tell Cloud Formation to create map after stage created
        map.addDependency(stage);
    }

    public CfnStage buildStage(Construct scope, String id, HttpApi api, LogGroup log) {

        var settings = CfnStage.AccessLogSettingsProperty
            .builder()
            .destinationArn(log.getLogGroupArn())
            .format("{\"requestId\":\"$context.requestId\""
                        + ",\"status\":\"$context.status\""
                        + ",\"httpMethod\":\"$context.httpMethod\""
                        + ",\"path\":\"$context.path\""
                        + ",\"responseLatency\":\"$context.responseLatency\"}")
            .build();

        return CfnStage.Builder
            .create(scope, id)
            .apiId(api.getHttpApiId())
            .stageName("$default")
            .autoDeploy(true)
            .accessLogSettings(settings)
            .build();
    }
}
