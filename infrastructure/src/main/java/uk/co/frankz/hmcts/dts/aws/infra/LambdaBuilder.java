package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.Map;

import static java.lang.System.out;

/**
 * Utility class to generate a CDK construct for a Lambda in AWS.
 */
public class LambdaBuilder {

    private final String displayName;

    private final String requestHandlerClassName;
    private final Code code;

    private final static Map<String, String> envVariables = Map.of(
        "LOG_LEVEL", "DEBUG"
        , "JACKSON_PRETTY_PRINT", "true"
        , "JACKSON_INCLUDE_NON_NULL", "true"
        , "JACKSON_DATE_FORMAT", "yyyy-MM-dd'T'HH:mm:ss.SSS"
    );

    /**
     * @param name                    displayed as the name of the Lambda in AWS.
     * @param pathJarFile             location of code for the lambda.
     * @param requestHandlerClassName class name with package prefixes of the Lambda code.
     */
    public LambdaBuilder(String name, String pathJarFile, String requestHandlerClassName) {
        this.displayName = name;
        this.requestHandlerClassName = requestHandlerClassName;
        this.code = Code.fromAsset(pathJarFile);

        // standard during asset building
        out.println("Successfully found lambda in Asset \"" + pathJarFile + "\".");
    }

    /**
     * Generate CDK construct for a Lambda using display name and code reference
     * provided in the constructor.
     *
     * @param scope of construct
     * @param id    as CDK identifier in the generated asset files
     * @return
     */
    public Function build(Construct scope, String id) {

        FunctionProps lambdaSpec = FunctionProps
            .builder()
            .runtime(Runtime.JAVA_21)
            .handler(requestHandlerClassName)
            .memorySize(128)
            .timeout(Duration.seconds(10))
            .architecture(Architecture.X86_64)
            .functionName(displayName)
            .environment(envVariables)
            .code(code)
            .build();

        return new Function(scope, id, lambdaSpec);
    }
}
