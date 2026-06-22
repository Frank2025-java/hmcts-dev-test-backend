package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.IdemPotencyStoreImpl;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.aws.http.IdemPotencyScopeKeyBuilder;
import uk.co.frankz.hmcts.dts.aws.http.ResponseFields;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyMismatchException;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getDecoder;
import static uk.co.frankz.hmcts.dts.aws.TaskExceptionHandler.setErrorOnResponse;

/**
 * BaseTaskHandler is the base class for the Lambdas that do the processing for
 * create, retrieve, update and delete Tasks.
 * <br>
 * This base class has the code to handle a API Gateway request and response, and
 * has a handle function for the actual Lambda to do their processing.
 */
abstract class BaseTaskHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    protected final TaskService<TaskWithId> service;
    protected final Mapper json;
    private final IdemPotencyScopeKeyBuilder idemPotency;
    private final IdemPotencyStore idemPotencyStore;

    /**
     * Cold-start container constructor. Should run on container start, but not on each invocation.
     */
    protected BaseTaskHandler() {
        this(
            new uk.co.frankz.hmcts.dts.aws.TaskService(),
            new Mapper(),
            new IdemPotencyScopeKeyBuilder(),
            new IdemPotencyStoreImpl()
        );
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service          access to the database
     * @param json             the conversion for DTOs and Jackson json mapper
     * @param idemPotency      the builder for IdemPotencyScopeKey
     * @param idemPotencyStore the dynamoDb table
     */
    protected BaseTaskHandler(
        TaskService<TaskWithId> service,
        Mapper json,
        IdemPotencyScopeKeyBuilder idemPotency,
        IdemPotencyStore idemPotencyStore) {

        this.service = service;
        this.json = json;
        this.idemPotency = idemPotency;
        this.idemPotencyStore = idemPotencyStore;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        LambdaLogger out = context.getLogger();

        var response = new APIGatewayV2HTTPResponse();

        try {
            Optional<IdemPotencyScopeKey> idemPotencyKey = idemPotency.build(event);

            ResponseFields responseResult = null;

            if (idemPotencyKey.isPresent()) {
                Optional<IdemPotencyRecord> idemPotencyRecord = idemPotencyStore.findById(idemPotencyKey.get());

                if (idemPotencyRecord.isPresent()) {
                    var record = idemPotencyRecord.get();

                    // make sure the idempotency key has been used for matching request body
                    verifyRequestHash(event, record);

                    responseResult = new ResponseFields(
                        record.responseBody(),
                        record.statusCode(),
                        Header.contentOfType(record.contentType())
                    );
                    out.log("Returning stored Idem Potency result " + idemPotencyKey.get());
                }
            }

            if (responseResult == null) {

                String routePath = takePathFromRoutKey(event.getRouteKey());

                Action action = Action.fromPath(routePath);

                Map<String, String> pathParams = event.getPathParameters();

                out.log("Request body: " + event.getBody());
                out.log("Request parms: " + (pathParams == null ? "none" : pathParams.toString()));

                responseResult = handle(action, event.getBody(), pathParams);

                out.log("Result code: " + responseResult.status());
                out.log("Response: " + response.getBody());

                if (idemPotencyKey.isPresent()) {

                    IdemPotencyRecord record = new IdemPotencyRecord(
                        idemPotencyKey.get().sha256(),
                        LocalDateTime.now(),
                        getRequestBodyHash(event),
                        responseResult.body().getBytes(UTF_8),
                        responseResult.status(),
                        responseResult.header().get(Header.CONTENT_TYPE)
                    );

                    idemPotencyStore.saveSafe(record);
                }
            }

            response.setStatusCode(responseResult.status());
            response.setBody(responseResult.body());
            response.setHeaders(responseResult.header());

        } catch (Exception e) {
            out.log(stackTrace(e));
            out.log(TaskException.toString(e));
            setErrorOnResponse(e, response);
        }

        return response;
    }

    private String stackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    protected abstract ResponseFields handle(
        Action action,
        String requestBody,
        Map<String, String> pathParams) throws Exception;

    public String get(Map<String, String> pathParams, String field) {
        if (pathParams == null || !pathParams.containsKey(field)) {
            return null;
        }
        return pathParams.get(field);
    }

    public @NotNull String getId(Map<String, String> pathParams) {
        String id = get(pathParams, Action.PARM.ID);

        if (id == null) {
            throw new TaskInvalidArgumentException("Missing id");
        }

        return id;
    }

    private @NotNull String takePathFromRoutKey(String routeKey) {
        if (StringUtils.isBlank(routeKey)) {
            return "";
        }

        if ("$Default".equals(routeKey)) {
            return Action.PATH.ROOT;
        }

        String[] parts = routeKey.split(" ", 2);

        return parts.length > 1 ? parts[1] : "";
    }

    // utility method, also available method for unit testing
    IdemPotencyHash getRequestBodyHash(APIGatewayV2HTTPEvent event) {

        byte[] bodyBytes;

        if (event.getIsBase64Encoded()) {
            bodyBytes = getDecoder().decode(event.getBody());
        } else {
            bodyBytes = event.getBody() == null ? new byte[0] : event.getBody().getBytes(UTF_8);

        }

        return IdemPotencyHash.sha256Hex(bodyBytes);
    }

    private void verifyRequestHash(APIGatewayV2HTTPEvent event, IdemPotencyRecord record)
        throws IdemPotencyMismatchException {

        if (!getRequestBodyHash(event).equals(record.requestHash())) {
            throw new IdemPotencyMismatchException();
        }
    }

}
