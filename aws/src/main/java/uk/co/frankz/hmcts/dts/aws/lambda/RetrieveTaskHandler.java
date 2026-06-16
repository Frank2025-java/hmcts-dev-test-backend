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
import java.util.stream.Stream;

public class RetrieveTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Required constructor for the Lambda getting initialised. A so-called warm container constructor.
     */
    @SuppressWarnings("unused")
    public RetrieveTaskHandler() {
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
    RetrieveTaskHandler(TaskService<TaskWithId> service,
                        Mapper json,
                        IdemPotencyScopeKeyBuilder idemPotency,
                        IdemPotencyStore idemPotencyStore) {
        super(service, json, idemPotency, idemPotencyStore);
    }

    @Override
    protected ResponseFields handle(Action action, String requestBody, Map<String, String> pathParams)
        throws Exception {

        if (action == Action.GET_ALL) {
            return getAll();
        }

        if (action == Action.GET) {

            return get(pathParams);
        }

        throw new TaskNoMatchException(String.valueOf(action), Action.names(Action.GET_ALL, Action.GET));
    }

    @Operation(summary = "Retrieve all tasks.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Non-empty array of Tasks.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskDto[].class))),
        @ApiResponse(responseCode = "400", description = "No tasks.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    ResponseFields getAll() {

        Stream<TaskWithId> tasks = service.getAll();
        TaskDto[] dtos = json.toDto(tasks);
        String body = json.toJsonString(dtos);

        return new ResponseFields(body, HttpStatusCode.OK, Header.JSON);
    }

    @Operation(summary = "Retrieve a task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task matching provided id.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "No task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    ResponseFields get(
        Map<String, String> pathParams) {

        String id = getId(pathParams);

        TaskWithId taskWitId = service.get(id);
        String body = json.toJsonString(json.toDto(taskWitId));

        return new ResponseFields(body, HttpStatusCode.OK, Header.JSON);
    }

}
