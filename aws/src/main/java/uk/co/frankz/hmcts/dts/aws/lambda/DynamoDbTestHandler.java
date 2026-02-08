package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.TaskProperties;

public class DynamoDbTestHandler implements RequestHandler<Object, String> {

    private static final String MARKER = "TEST LOG ----------->>>>>>>     ";

    private static final Mapper MAPPER = new Mapper();

    @Override
    public String handleRequest(Object input, Context context) {

        LambdaLogger out = context.getLogger();

        // Detect API Gateway event
        out.log(MARKER + "Incoming object is of type :" + input.getClass().getName());

        try {
            logIdentity(out);

            AwsCredentialsProvider credProv = DefaultCredentialsProvider.create();

            out.log(MARKER + "Credentials setup: " + credProv.toString());

            AwsCredentials cred = credProv.resolveCredentials();

            out.log(MARKER + "Credentials resolved: " + cred.getClass().getName());

            DynamoDbClient dynamo = DynamoDbClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.EU_WEST_1)
                .build();

            out.log(MARKER + "Scan coming up from " + dynamo.serviceName() + " in region " + Region.EU_WEST_1.id() + "...");

            ScanResponse scan = dynamo.scan(ScanRequest.builder()
                                                .tableName(TaskProperties.TABLE)
                                                .build());

            out.log(MARKER + "Scan has been successful");

            return "Successful scan of table " + TaskProperties.TABLE + " with " + scan.count() + " items";

        } catch (AwsServiceException | SdkClientException e) {

            out.log(MARKER + "Scan has been unsuccessful: " + e.toString());
            return "UnSuccessful scan of table: " + e.toString();
        } finally {
            out.log(MARKER + "Scan attempt finished.");
        }

    }

    public void logIdentity(LambdaLogger log) {
        StsClient sts = StsClient.builder()
            .region(Region.EU_WEST_1)
            .build();

        GetCallerIdentityResponse identity = sts.getCallerIdentity(
            GetCallerIdentityRequest.builder().build()
        );

        log.log(MARKER + "Account: " + identity.account());
        log.log(MARKER + "Arn: " + identity.arn());
        log.log(MARKER + "UserId: " + identity.userId());
    }

}
