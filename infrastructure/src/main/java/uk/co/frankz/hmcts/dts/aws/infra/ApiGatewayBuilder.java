package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.apigatewayv2.ApiMapping;
import software.amazon.awscdk.services.apigatewayv2.ApiMappingProps;
import software.amazon.awscdk.services.apigatewayv2.DomainMappingOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpRouteIntegration;
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
     * @param defaultLambda will be the default lambda target in this domain
     * @return
     */
    public HttpApi build(Construct scope, String id, DomainName apiDomain, HttpRouteIntegration defaultLambda) {

        // Leave mappingKey undefined for the root path mapping.
        var rootMap = DomainMappingOptions.builder().domainName(apiDomain).build();

        HttpApiProps props = HttpApiProps.builder()
            .apiName(displayName)
            .defaultIntegration(defaultLambda)
            .defaultDomainMapping(rootMap)
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
     * @param subDomain
     * @param basePath   which is
     */
    public void buildMap(Construct scope, String id, HttpApi api, DomainName subDomain, String basePath) {

        ApiMappingProps props = ApiMappingProps.builder()
            .api(api)
            .domainName(subDomain)
            .stage(api.getDefaultStage())
            .apiMappingKey(basePath)
            .build();

        new ApiMapping(scope, id, props);

    }
}
