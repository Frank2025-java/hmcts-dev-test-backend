package uk.co.frankz.hmcts.dts.spring.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;

class RootTaskControllerTest {

    RootTaskController testSubject;

    @BeforeEach
    void setup() {
        testSubject = new RootTaskController();
    }

    @Test
    void shouldGiveSwaggerLink() {
        //when
        ResponseEntity<String> actual = testSubject.welcome();

        assertEquals(OK, actual.getStatusCode());
        String body = actual.getBody();
        assertTrue(body.contains("swagger"), body);
    }
}
