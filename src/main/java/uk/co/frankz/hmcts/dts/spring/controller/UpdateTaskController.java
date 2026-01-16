package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.spring.Mapper;
import uk.co.frankz.hmcts.dts.spring.TaskService;
import uk.co.frankz.hmcts.dts.spring.TaskWithId;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/task")
public class UpdateTaskController {

    @Autowired
    private final TaskService service;

    @Autowired
    private final Mapper map;

    @Autowired
    public UpdateTaskController(TaskService service, Mapper mapper) {
        this.service = service;
        this.map = mapper;
    }

    @Operation(summary = "Update Status by ID.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task matching provided id.",
            content = {@Content(mediaType = APPLICATION_JSON_VALUE)}),
        @ApiResponse(
            responseCode = "400",
            description = "No task matching the provided id, or invalid status.",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Other exceptions.", content = @Content)
    })
    @PutMapping(value = "/update/{id}/status/{status}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable String id, @PathVariable String status) {

        TaskWithId stored = service.update(id, status);
        TaskDto display = map.toDto(stored);

        return ok(display);
    }

    @Operation(summary = "Update Task fields.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Updated Task.",
            content = {@Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(
            responseCode = "400",
            description = "No task matching the id in the provided Task.",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Other exceptions.",
            content = @Content)
    })
    @PostMapping(value = "/update", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto> updateTask(@RequestBody TaskDto task) {

        TaskWithId input = map.toEntity(task);
        TaskWithId stored = service.update(input);
        TaskDto display = map.toDto(stored);

        return ok(display);
    }

}
