package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/task")
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
            content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "No task matching the provided id.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @GetMapping(value = "/get/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto> getTask(@PathVariable String id) {

        TaskWithId stored = service.get(id);
        TaskDto display = map.toDto(stored);

        return ok(display);
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
    @GetMapping(value = "/get-all-tasks", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto[]> getAllTasks() {

        Stream<TaskWithId> stored = service.getAll();
        TaskDto[] display = map.toDto(stored);

        return ok(display);
    }

}
