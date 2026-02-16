package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

public class UpdateTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Required constructor for the Lambda getting initialised. A so-called warm container constructor.
     */
    public UpdateTaskHandler() {
        super();
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service allows unit testing with mock TaskService
     * @param json    allows unit testing with mock Mapper
     */
    UpdateTaskHandler(TaskService<TaskWithId> service, Mapper json) {
        super(service, json);
    }

    @Operation(summary = "Update Status by ID.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task matching provided id.",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "No task matching the provided id, or invalid status.",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    Pair<String, Integer> update(Map<String, String> pathParams) {

        String id = getId(pathParams);
        String status = get(pathParams, Action.PARM.STATUS);

        TaskWithId taskWitId = service.update(id, status);
        String body = json.toJsonString(json.toDto(taskWitId));

        return Pair.of(json.toJsonString(taskWitId), HttpStatusCode.OK);
    }

    @Operation(summary = "Update Task fields.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Updated Task.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(
            responseCode = "400",
            description = "No task matching the id in the provided Task.",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Other exceptions.",
            content = @Content)
    })
    Pair<String, Integer> update(String requestBody) {
        TaskWithId task = json.toEntity(requestBody);
        TaskWithId taskWitId = service.update(task);
        String body = json.toJsonString(json.toDto(taskWitId));

        return Pair.of(json.toJsonString(taskWitId), HttpStatusCode.OK);
    }

    @Override
    protected Pair<String, Integer> handle(Action action, String requestBody, Map<String, String> pathParams)
        throws Exception {

        if (action == Action.UPDATE) {
            return update(requestBody);
        }

        if (action == Action.UPDATE_STATUS) {

            return update(pathParams);
        }

        throw new TaskNoMatchException(String.valueOf(action), Action.names(Action.UPDATE, Action.UPDATE_STATUS));
    }

}
