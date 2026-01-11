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
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import java.util.stream.Stream;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class RetrieveTaskController {

    @Autowired
    private final TaskService service;

    @Autowired
    private final Mapper map;

    @Autowired
    public RetrieveTaskController(TaskService service, Mapper mapper) {
        this.service = service;
        this.map = mapper;
    }

    @Operation(summary = "Retrieve a task by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task matching provided id.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "No task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @PostMapping(value = "/get-task", produces = "application/json")
    public ResponseEntity<TaskDto> getTask(@RequestBody String id) {

        TaskWithId stored = service.get(id);
        TaskDto display = map.toDto(stored);

        return ok(display);
    }

    @Operation(summary = "Retrieve all tasks.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Non-empty array of Tasks.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto[].class))}),
        @ApiResponse(responseCode = "400", description = "No tasks.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @PostMapping(value = "/get-all-tasks", produces = "application/json")
    public ResponseEntity<TaskDto[]> getAllTasks() {

        Stream<TaskWithId> stored = service.getAll();
        TaskDto[] display = map.toDto(stored);

        return ok(display);
    }

}
