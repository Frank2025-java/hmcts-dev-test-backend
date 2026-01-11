package uk.co.frankz.hmcts.dts.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class RootController {

    @Operation(summary = "Web root content.")
    @GetMapping("/")
    public ResponseEntity<String> welcome() {

        return ok("Welcome to HM Courts and Tribunal Service Developer Technical Test");
    }
}
