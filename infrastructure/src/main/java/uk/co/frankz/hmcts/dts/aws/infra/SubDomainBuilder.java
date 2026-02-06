package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.constructs.Construct;

import static software.amazon.awscdk.services.apigatewayv2.EndpointType.REGIONAL;
import static software.amazon.awscdk.services.apigatewayv2.SecurityPolicy.TLS_1_2;

/**
 * Utility class for generating a subdomain construct.
 */
public class SubDomainBuilder {

    /**
     * @param scope         of construct
     * @param id            as CDK identifier in the generated asset files
     * @param subDomainName full name of subdomain like api.xxx.co.uk
     * @param certificate   provisioned certificate registered with subDomainName
     * @return wrapper for generated subdomain construct, name and certificate.
     */
    public DomainName build(Construct scope, String id, String subDomainName, ICertificate certificate) {

        return DomainName.Builder
            .create(scope, id)
            .domainName(subDomainName)
            .certificate(certificate)
            .endpointType(REGIONAL)
            .securityPolicy(TLS_1_2)
            .build();
    }
}
