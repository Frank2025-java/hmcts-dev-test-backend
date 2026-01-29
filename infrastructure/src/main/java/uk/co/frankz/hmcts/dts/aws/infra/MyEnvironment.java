package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class MyEnvironment {

    private final String account;
    private final String region;

    private MyEnvironment() {

        account = System.getenv("CDK_DEFAULT_ACCOUNT");
        region = System.getenv("AWS_REGION");
    }

    public static StackProps awsDefaultProfile() {

        MyEnvironment my = new MyEnvironment();
        Environment env = Environment.builder().account(my.account).region(my.region).build();
        return StackProps.builder().env(env).build();
    }
}
