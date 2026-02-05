package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.model.exception.TaskInvalidArgumentException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

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

    protected final TaskService<TaskWithId> service = new uk.co.frankz.hmcts.dts.aws.TaskService();

    protected final Mapper json = new Mapper();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        LambdaLogger out = context.getLogger();

        String routePath = takePathFromRoutKey(event.getRouteKey());

        Action action = Action.fromPath(routePath);

        Map<String, String> pathParams = event.getPathParameters();

        var response = new APIGatewayV2HTTPResponse();

        try {
            Pair<String, Integer> result = handle(action, event.getBody(), pathParams);

            response.setStatusCode(result.getRight());
            response.setBody(result.getLeft());
            response.setHeaders(Header.JSON);

        } catch (Exception e) {
            out.log(e.getMessage());
            setErrorOnResponse(e, response);
        }
        return response;
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

        String[] parts = routeKey.split(" ", 2);

        return parts.length > 1 ? parts[1] : "";
    }

}
