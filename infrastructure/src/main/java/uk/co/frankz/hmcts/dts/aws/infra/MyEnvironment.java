package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MyEnvironment {

    private final String account;
    private final String region;

    private MyEnvironment() {

        account = System.getenv("CDK_DEFAULT_ACCOUNT");
        region = System.getenv("AWS_REGION");

        requireNonNull(account,"Environment variable \"CDK_DEFAULT_ACCOUNT\" should be set but is null." );
        requireNonNull(region,"Environment variable \"AWS_REGION\" should be set but is null." );
    }

    public static StackProps awsDefaultProfile() {

        MyEnvironment my = new MyEnvironment();
        Environment env = Environment.builder().account(my.account).region(my.region).build();
        return StackProps.builder().env(env).build();
    }
}
