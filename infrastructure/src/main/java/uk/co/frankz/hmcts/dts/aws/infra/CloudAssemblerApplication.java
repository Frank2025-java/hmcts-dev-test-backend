package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.App;

public class CloudAssemblerApplication {
    public static void main(final String[] args) {

        App rootConstruct = new App();

        new BackEndStack(rootConstruct, "BackEndStack");

        rootConstruct.synth();
    }
}

