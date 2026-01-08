package uk.co.frankz.hmcts.dts.spring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.frankz.hmcts.dts.dto.TaskDto;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class TaskController {

    @Autowired
    private final TaskStore store;

    @Autowired
    private final Mapper map;

    @Autowired
    public TaskController(TaskStore taskStore, Mapper mapper) {
        this.store = taskStore;
        this.map = mapper;
    }

    @Operation(summary = "Web root content.")
    @GetMapping("/")
    public ResponseEntity<String> welcome() {

        return ok("Welcome to HM Courts and Tribunal Service Developer Technical Test");
    }

    @Operation(summary = "Create a Task with Title, Description (optional), Status, Due date/time.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Created Task and returned task with populated id.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))}),
        @ApiResponse(responseCode = "400", description = "Title, Status, and Due are required fields.",
            content = @Content)
    })
    @PostMapping(value = "/create-task", produces = "application/json")
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto newTask) {
        try {
            TaskWithId input = map.toEntity(newTask);
            TaskWithId stored = store.save(input);
            TaskDto display = map.toDto(stored);

            return ok(display);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // tiny bit more info to feedback
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
        }
    }
}
