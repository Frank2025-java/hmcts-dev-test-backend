package uk.co.frankz.hmcts.dts.spring.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

class RootControllerTest {

    RootController testSubject;

    @BeforeEach
    void setup() {
        testSubject = new RootController();
    }

    @Test
    void shouldWelcome() {
        //when
        ResponseEntity<String> actual = testSubject.welcome();

        assertEquals(OK, actual.getStatusCode());
        assertEquals("Welcome to HM Courts and Tribunal Service Developer Technical Test", actual.getBody());
    }
}
