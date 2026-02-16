package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.service.Action;
import uk.co.frankz.hmcts.dts.spring.TaskService;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
@RequestMapping("/task")
public class DeleteTaskController {

    @Autowired
    private final TaskService service;

    @Autowired
    public DeleteTaskController(TaskService service) {
        this.service = service;
    }

    @Operation(summary = "Delete a Task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task has been deleted successfully, or "
            + "there was no task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Technical exceptions.", content = @Content)
    })
    @DeleteMapping(Action.PATH.DELETE)
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {

        service.delete(id);

        return noContent().build();
    }
}
