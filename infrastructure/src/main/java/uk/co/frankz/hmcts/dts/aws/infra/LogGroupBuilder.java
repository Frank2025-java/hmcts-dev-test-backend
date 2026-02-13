package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import static software.amazon.awscdk.RemovalPolicy.RETAIN;
import static software.amazon.awscdk.services.logs.RetentionDays.ONE_WEEK;

/**
 * Utility class to create a Log Group construct for CDK.
 */
public class LogGroupBuilder {

    private final String name;  // e.g. my-api-execution-logs to get "/aws/apigateway/api-access-logs"

    public LogGroupBuilder(String name) {
        this.name = name;
    }

    /**
     * Constructor for this utility class.
     *
     * @param scope of construct
     * @param id    as CDK identifier in the generated asset files
     * @return LogGroup construct
     */
    public LogGroup build(Construct scope, String id) {

        return LogGroup.Builder.create(scope, id)
            .logGroupName(name)
            .retention(ONE_WEEK)
            .removalPolicy(RETAIN)
            .build();
    }

}
