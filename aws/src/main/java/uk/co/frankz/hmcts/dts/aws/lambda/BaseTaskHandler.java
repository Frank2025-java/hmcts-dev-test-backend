package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

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

    /**
     * Cold-start container constructor. Should run on container start, but not on each invocation.
     */
    protected BaseTaskHandler() {
        this(new uk.co.frankz.hmcts.dts.aws.TaskService(DefaultCredentialsProvider.create()), new Mapper());
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service access to the database
     * @param json    the conversion for DTOs and Jackson json mapper
     */
    protected BaseTaskHandler(TaskService<TaskWithId> service, Mapper json) {
        this.service = service;
        this.json = json;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        LambdaLogger out = context.getLogger();

        var response = new APIGatewayV2HTTPResponse();

        try {
            String routePath = takePathFromRoutKey(event.getRouteKey());

            Action action = Action.fromPath(routePath);

            Map<String, String> pathParams = event.getPathParameters();

            out.log("Request: " + event.getBody());

            Pair<String, Integer> result = handle(action, event.getBody(), pathParams);

            response.setStatusCode(result.getRight());
            response.setBody(result.getLeft());
            response.setHeaders(Header.JSON);

        } catch (Exception e) {
            out.log(stackTrace(e));
            out.log(TaskException.toString(e));
            setErrorOnResponse(e, response);
        }

        out.log("Response: " + response.getBody());

        return response;
    }

    private String stackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    protected abstract Pair<String, Integer> handle(
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

}
