package uk.co.frankz.hmcts.dts;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.spring.Application;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestMethodOrder(OrderAnnotation.class)
class IdemPotencyFunctionalTest {

    private static final Logger LOG = LoggerFactory.getLogger(IdemPotencyFunctionalTest.class);

    public static final String ANSI_PURPLE = "\u001B[35m";

    private static void log(String msg) {
        LOG.info(ANSI_PURPLE + msg);
    }

    //@Value("${TEST_URL:https://api.frankz.co.uk}")
    @Value("${TEST_URL:http://localhost:4000}")
    @SuppressWarnings("unused")
    private String testUrl;

    static TaskDto testTaskInMemory;

    private static final String TEST_IDEMPOTENCY_HEADER = "Idempotency-Key";

    private static final String TEST_IDEMPOTENCY_KEY_CREATE = UUID.randomUUID().toString();

    @BeforeAll
    public static void init() {
        testTaskInMemory = null;
    }

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = testUrl + "/task";
        RestAssured.useRelaxedHTTPSValidation();

        log("Before testTask=" + testTaskInMemory);
    }

    @AfterEach
    public void testInfo() {
        // little bit of feedback
        log("After testTask=" + testTaskInMemory);
    }

    @Test
    @Order(1)
    void shouldCreateTaskWithIdemPotencyAndReturnStoredResponseOnRepeat() {
        TaskDto testTask = new TaskDto();
        testTask.setTitle("test title");

        log("create with key:" + TEST_IDEMPOTENCY_KEY_CREATE);

        RequestSpecification givenRequest = given()
            .header(TEST_IDEMPOTENCY_HEADER, TEST_IDEMPOTENCY_KEY_CREATE)
            .contentType(ContentType.JSON)
            .body(testTask);

        Response response = givenRequest
            .when()
            .post("/create")
            .then()
            .extract()
            .response();

        assertEquals(HttpStatus.CREATED.value(), response.statusCode(), "Expecting a 201 on regular create request.");
        testTaskInMemory = response.body().as(TaskDto.class);

        Response responseRepeat = givenRequest
            .when()
            .post("/create")
            .then()
            .extract()
            .response();

        // should return same response
        assertEquals(
            HttpStatus.CREATED.value(),
            responseRepeat.statusCode(),
            "Expecting a stored response status back."
        );
        TaskDto actual = responseRepeat.body().as(TaskDto.class);
        assertEquals(
            testTaskInMemory.getId(),
            actual.getId(),
            "should NOT have created a new Task but return same response."
        );
    }

    @Test
    @Order(2)
    void shouldFailCreateUsingDifferentRequestSameIdempotencyKey() {
        TaskDto testTask = new TaskDto();
        testTask.setTitle("a little different test title");

        Response response = given()
            .header(TEST_IDEMPOTENCY_HEADER, TEST_IDEMPOTENCY_KEY_CREATE)
            .contentType(ContentType.JSON)
            .body(testTask)
            .when()
            .post("/create")
            .then()
            .extract()
            .response();

        assertEquals(
            HttpStatus.CONFLICT.value(),
            response.statusCode(),
            "Expecting a 409 when repeat request with slightly different body."
        );
    }

}
