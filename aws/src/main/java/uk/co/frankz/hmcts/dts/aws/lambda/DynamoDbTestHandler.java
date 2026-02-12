package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.TaskProperties;

import java.util.concurrent.CompletableFuture;

public class DynamoDbTestHandler implements RequestHandler<Object, String> {

    private static final CompletableFuture<Void> SLF4J = ColdStartRoutine.SLF4J.warmup();

    private static final CompletableFuture<Void> BACKGROUND =
        ColdStartRoutine.concurrentWarmup(ColdStartRoutine.DYNAMODB, ColdStartRoutine.STS);

    private static final String MARKER = "TEST LOG ----------->>>>>>>     ";

    private static final Mapper MAPPER = new Mapper();

    @Override
    public String handleRequest(Object input, Context context) {

        LambdaLogger out = context.getLogger();

        out.log(MARKER + "Waiting for background initialisation");

        // wait until initialised.
        BACKGROUND.join();

        // Detect API Gateway event
        out.log(MARKER + "Incoming object is of type :" + input.getClass().getName());

        try {
            out.log(MARKER + "DynamoDbClient");

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

            // do not finish, but wait for all logging appenders plugged in, in case of cold start
            SLF4J.join();
        }

    }

}
