package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.route53.ARecord;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneProviderProps;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.route53.targets.ApiGatewayv2DomainProperties;
import software.constructs.Construct;
import uk.co.frankz.hmcts.dts.aws.infra.SubDomainBuilder.SubDomain;

/**
 * Utility class to generate CDK constructs to do with Route53.
 */
public class DnsEntryBuilder {

    private static IHostedZone hostedZone = null;

    private final HostedZoneProviderProps domainQuery;

    public DnsEntryBuilder(String name) {
        this.domainQuery = HostedZoneProviderProps.builder().domainName(name).build();
    }

    /**
     * Hosted zones are created by AWS as part of domain,
     * and this method creates a CDK constructs to look up the zone from the (constructor) provided domain name.
     *
     * @param scope for CDK construction
     * @return my hosted zones created by AWS as part of domain frankz.co.uk hosted and created on AWS
     */
    synchronized public IHostedZone find(Construct scope) {

        if (hostedZone == null) {

            hostedZone = HostedZone.fromLookup(scope, "MyZone", domainQuery);
        }

        return hostedZone;
    }

    /**
     * Create an ARecord entry (CDK construct) targeting the ApiGateway which has lambdas behind it.
     *
     * @param scope     of construct
     * @param id        as CDK identifier in the generated asset files
     * @param name      of the Route53 entry
     * @param subDomain wrapping the name and reference to CDK domain construct
     */
    public void build(Construct scope, String id, String name, SubDomain subDomain) {

        var route53Zone = find(scope);

        var gateway = new ApiGatewayv2DomainProperties(subDomain.name(), subDomain.obj().getRegionalHostedZoneId());

        ARecord.Builder.create(scope, id)
            .zone(route53Zone)
            .recordName(name)
            .target(RecordTarget.fromAlias(gateway))
            .build();
    }

}
