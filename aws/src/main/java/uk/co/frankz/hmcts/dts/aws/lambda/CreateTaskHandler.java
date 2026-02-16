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
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.service.TaskService;

import java.util.Map;

public class CreateTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Required constructor for the Lambda getting initialised. A so-called warm container constructor.
     */
    @SuppressWarnings("unused")
    public CreateTaskHandler() {
        super();
    }

    /**
     * Constructor allowing unit test with mocks.
     *
     * @param service allows unit testing with mock TaskService
     * @param json    allows unit testing with mock Mapper
     */
    CreateTaskHandler(TaskService<TaskWithId> service, Mapper json) {
        super(service, json);
    }

    @Operation(summary = "Create a Task with Title, Description (optional), Status, Due date/time.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created Task and returned task with populated id.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "Title, Status, and Due are required fields. "
            + "In case of an unexpected error, an error message is send back as feedback, "
            + "which shows depending on the Front End framework.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @Override
    protected Pair<String, Integer> handle(Action action, String request, Map<String, String> pathParams)
        throws Exception {

        TaskWithId task = json.toEntity(request);
        TaskWithId taskWitId = service.createTask(task);
        String body = json.toJsonString(taskWitId);

        return Pair.of(body, HttpStatusCode.CREATED);
    }
}
