package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class DeleteTaskController {

    @Autowired
    private final TaskService service;

    @Autowired
    private final Mapper map;

    @Autowired
    public DeleteTaskController(TaskService service, Mapper mapper) {
        this.service = service;
        this.map = mapper;
    }

    @Operation(summary = "Delete a Task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Task has been deleted.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))}),
        @ApiResponse(responseCode = "400", description = "No task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @PostMapping(value = "/delete-task", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> deleteTask(@RequestBody String id) {

        service.delete(id);

        return noContent().build();
    }
}
