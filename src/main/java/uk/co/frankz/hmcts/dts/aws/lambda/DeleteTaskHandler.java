package uk.co.frankz.hmcts.dts.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.frankz.hmcts.dts.service.Action;

import java.util.Map;

public class DeleteTaskHandler extends BaseTaskHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Operation(summary = "Delete a Task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Task has been deleted successfully, or "
            + "there was no task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Technical exceptions.", content = @Content)
    })
    @Override
    protected Pair<String, Integer> handle(Action action, String requestBody, Map<String, String> pathParams)
        throws Exception {

        String id = getId(pathParams);

        service.delete(id);

        return Pair.of("", 202);
    }
}
