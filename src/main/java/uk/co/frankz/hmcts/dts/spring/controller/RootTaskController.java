package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.frankz.hmcts.dts.service.Action;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/task")
public class RootTaskController {

    @Operation(summary = "Web content for Task api.")
    @GetMapping(Action.PATH.ROOT)
    public ResponseEntity<String> welcome() {

        return ok("Use /v3/api-docs for REST documentation, "
                      + "or /swagger-ui.html for a more user-friendly way of inspecting the application.");
    }
}
