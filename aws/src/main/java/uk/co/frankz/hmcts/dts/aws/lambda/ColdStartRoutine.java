package uk.co.frankz.hmcts.dts.aws.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.stream;

/**
 * Some Routines that should be run in the background, concurrently, of a Lambda starting up,
 * so that it saves time when actually needed.
 */
public enum ColdStartRoutine {
    SLF4J(ColdStartRoutine::logSomething),
    SDK_HTTP_SECURITY(ColdStartRoutine::forceInitSSL),
    HTTP_CLIENT(ColdStartRoutine::initHttpClient),
    STS(ColdStartRoutine::initSTS),
    AWSCREDENTIALS(ColdStartRoutine::initAwsCredentials),
    DYNAMODB(ColdStartRoutine::initDynamoDb);

    private CompletableFuture<Void> warmupFuture = null;

    private final Runnable function;

    ColdStartRoutine(Runnable function) {
        this.function = function;
    }

    /**
     * Start an (expensive) initialisation in the background
     *
     * @return CompletableFuture that can be waited for.
     */
    public CompletableFuture<Void> warmup() {
        return warmup(Executors.newSingleThreadExecutor(r -> new Thread(r, name())));
    }

    private CompletableFuture<Void> warmup(ExecutorService executor) {
        System.out.println("Warmup " + name());

        this.warmupFuture = CompletableFuture.supplyAsync(
            () -> {

                try {
                    long t = System.currentTimeMillis();
                    function.run();
                    System.out.println(this.name() + " warmed up in " + (System.currentTimeMillis() - t) + " ms");

                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
                return null;
            }, executor
        );

        return warmupFuture;
    }

    /**
     * Start the warm up of several in a concurrent way
     *
     * @param routines
     * @return
     */
    public static CompletableFuture<Void> concurrentWarmup(ColdStartRoutine... routines) {

        ExecutorService executor = Executors.newFixedThreadPool(routines.length);

        CompletableFuture<String>[] futures =
            stream(routines)
                .map(ColdStartRoutine::warmup)
                .toArray(size -> (CompletableFuture<String>[]) Array.newInstance(CompletableFuture.class, size));

        return CompletableFuture.allOf(futures);
    }

    /**
     * With a lot of appenders participating in logging, it can take a bit of time.
     * A first call triggers reading the underlying logging system like Log4j2.
     */
    private static void logSomething() {
        Logger log = LoggerFactory.getLogger(ColdStartRoutine.class);
        log.warn("Logger(ColdStartRoutine.class) level debug=" + log.isDebugEnabled());
        System.getProperties().forEach((k, v) -> log.debug(k + "=" + v));
    }

    /**
     * Both {@link software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient}
     * and {@link software.amazon.awssdk.http.apache.ApacheHttpClient}
     * use SDK routines to get initialised.
     * Only on first (cold) startup this will take expensive time, typically a few seconds.
     * Services like DynamoDb and STS save time, on cold startup when this has been
     * completed.
     */
    private static void forceInitSSL() {

        try {
            // Force SecureRandom initialization
            SecureRandom sr = SecureRandom.getInstanceStrong();
            sr.nextBytes(new byte[1]);

            // Force SSLContext initialization
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, null, null);
            SSLSocketFactory factory = ctx.getSocketFactory();

            // Force truststore load by creating a dummy SSLSocket
            SSLSocket socket = (SSLSocket) factory.createSocket();
            socket.getSupportedCipherSuites();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Initialises an http client, which includes SSL initialisation.
     * Takes about 4 seconds.
     */
    private static void initHttpClient() {

        forceInitSSL();

        var urlClientBuilder = UrlConnectionHttpClient.builder();
        urlClientBuilder.build();
    }

    /**
     * Does a first call to STS, which includes http client initialisation.
     * Takes about 7 seconds.
     */
    private static void initSTS() {

        initHttpClient();

        StsClient sts = StsClient.builder()
            .region(Region.EU_WEST_1)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();

        GetCallerIdentityResponse identity = sts.getCallerIdentity(
            GetCallerIdentityRequest.builder().build()
        );
    }

    /**
     * Does resolve default AWS credentials, which often are in a chain of providers,
     * that log errors in debug mode.
     * Takes about 1 second.
     */
    private static void initAwsCredentials() {
        AwsCredentialsProvider credProv = DefaultCredentialsProvider.create();
        AwsCredentials cred = credProv.resolveCredentials();
    }

    /**
     * Sets up an DynamoDb client, which depends on credentials.
     * The communication to DynamoDb goes via an http client.
     * Both http and credentials are initialised as well.
     * Takes about 9 seconds.
     */
    private static void initDynamoDb() {

        initHttpClient();

        initAwsCredentials();

        DynamoDbClient dynamo = DynamoDbClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.EU_WEST_1)
            .build();
    }
}
