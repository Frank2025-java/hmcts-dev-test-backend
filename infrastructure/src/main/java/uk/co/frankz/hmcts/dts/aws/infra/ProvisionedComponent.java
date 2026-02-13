package uk.co.frankz.hmcts.dts.aws.infra;

/**
 * The available AWS components needed to generate constructs and are part of our infrastructure.
 * <br>
 * The main domain is generated when signing up for AWS.
 * The certificate for the subdomain has a verification step that can take a bit of time,
 * and can take days for external verification or when it is not configured properly.
 */
public interface ProvisionedComponent {

    String DOMAIN = "frankz.co.uk";

    String SUBDOMAIN_PREFIX = "api";

    String SUBDOMAIN_NAME = SUBDOMAIN_PREFIX + "." + DOMAIN;

    String SUBDOMAIN_CERT_ARN = "arn:aws:acm:eu-west-1:624325726410:certificate/fe385a47-e2d4-4c52-b55e-dd9c1769dcf0";

    SubDomainCertFinder certFinder = new SubDomainCertFinder(SUBDOMAIN_PREFIX, SUBDOMAIN_CERT_ARN);
}
