package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.constructs.Construct;

/**
 * Utility class to generate a construct to look up a certificate.
 */
public class SubDomainCertFinder {

    private final String prefix;

    private final String sslCert;

    public SubDomainCertFinder(String subDomainPrefix, String sslCertArn) {
        this.prefix = subDomainPrefix;
        this.sslCert = sslCertArn;
    }

    /**
     * Certificate are created on the console, after which those get validated for use on the domain.
     *
     * @param scope for CDK construction
     * @return certificate
     */
    public ICertificate find(Construct scope) {
        return Certificate.fromCertificateArn(scope, "MyCert" + prefix, sslCert);
    }
}


