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
import uk.co.frankz.hmcts.dts.aws.Mapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RetrieveTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Required constructor for the Lambda getting initialised. A so-called warm container constructor.
     */
    public RetrieveTaskHandler() {
        super();
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service allows unit testing with mock TaskService
     * @param json    allows unit testing with mock Mapper
     */
    RetrieveTaskHandler(TaskService<TaskWithId> service, Mapper json) {
        super(service, json);
    }

    @Override
    protected Pair<String, Integer> handle(Action action, String requestBody, Map<String, String> pathParams)
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
                mediaType = APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TaskDto[].class))),
        @ApiResponse(responseCode = "400", description = "No tasks.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    Pair<String, Integer> getAll() throws Exception {

        Stream<TaskWithId> tasks = service.getAll();
        TaskDto[] dtos = json.toDto(tasks);
        String body = json.toJsonString(dtos);

        return Pair.of(body, 200);
    }

    @Operation(summary = "Retrieve a task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task matching provided id.",
            content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "No task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    Pair<String, Integer> get(
        Map<String, String> pathParams) throws Exception {

        String id = getId(pathParams);

        TaskWithId taskWitId = service.get(id);
        String body = json.toJsonString(json.toDto(taskWitId));

        return Pair.of(body, 200);
    }

}
