package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.aws.http.IdemPotencyScopeKeyBuilder;
import uk.co.frankz.hmcts.dts.aws.http.ResponseFields;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.Header;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

public class UpdateTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Required constructor for the Lambda getting initialised. A so-called warm container constructor.
     */
    @SuppressWarnings("unused")
    public UpdateTaskHandler() {
        super();
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service allows unit testing with mock TaskService
     * @param json    allows unit testing with mock Mapper
     * @param idemPotency      the builder for IdemPotencyScopeKey
     * @param idemPotencyStore the dynamoDb table
     */
    UpdateTaskHandler(TaskService<TaskWithId> service,
                      Mapper json,
                      IdemPotencyScopeKeyBuilder idemPotency,
                      IdemPotencyStore idemPotencyStore) {
        super(service, json, idemPotency, idemPotencyStore);
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
    ResponseFields update(Map<String, String> pathParams) {

        String id = getId(pathParams);
        String status = get(pathParams, Action.PARM.STATUS);

        TaskWithId taskWitId = service.update(id, status);
        String body = json.toJsonString(taskWitId);

        return new ResponseFields(body, HttpStatusCode.OK, Header.JSON);
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
    ResponseFields update(String requestBody) {
        TaskWithId task = json.toEntity(requestBody);
        TaskWithId taskWitId = service.update(task);
        String body = json.toJsonString(taskWitId);

        return new ResponseFields(body, HttpStatusCode.OK, Header.JSON);
    }

    @Override
    protected ResponseFields handle(Action action, String requestBody, Map<String, String> pathParams)
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
