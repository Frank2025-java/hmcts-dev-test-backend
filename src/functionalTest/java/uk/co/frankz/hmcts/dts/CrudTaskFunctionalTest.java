package uk.co.frankz.hmcts.dts;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
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
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
class CrudTaskFunctionalTest {

    protected static final String CONTENT_TYPE_VALUE = "application/json";

    private static final Logger LOG = LoggerFactory.getLogger(CrudTaskFunctionalTest.class);

    public static final String ANSI_PURPLE = "\u001B[35m";

    private static void log(String msg) {
        LOG.info(ANSI_PURPLE + msg);
        ;
    }

    @Value("${TEST_URL:http://localhost:4000}")
    private String testUrl;

    static TaskDto testTaskInMemory;

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

    private TaskDto assertValidTaskResponse(Response response) {
        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        TaskDto actual = response.body().as(TaskDto.class);
        assertTrue(StringUtils.isNotBlank(actual.getId()));
        assertNotNull(actual.getStatus());
        assertNotNull(actual.getDue());
        return actual;
    }

    @Test
    @Order(1)
    void shouldCreateTask() {
        TaskDto testTask = new TaskDto();
        String testTitle = "test title";
        testTask.setTitle(testTitle);

        log("create:" + testTask);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(testTask)
            .when()
            .post("/create")
            .then()
            .extract()
            .response();

        TaskDto actual = assertValidTaskResponse(response);
        assertNotNull(actual.getId());
        assertEquals(testTitle, actual.getTitle());

        testTaskInMemory = actual;
    }

    @Test
    @Order(2)
    void shouldFailCreateTaskWithExistingId() {
        TaskDto testTask = testTaskInMemory;

        log("create:" + testTask);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(testTask)
            .when()
            .post("/create")
            .then()
            .extract()
            .response();

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(3)
    void shouldUpdateStatus() throws JSONException {
        String testTaskId = testTaskInMemory.getId();
        String testStatus = Status.Deleted.name();

        String testIdStatusURI = "/update"
            + "/" + testTaskId
            + "/status/" + testStatus;

        log("update-task-by-id:" + testIdStatusURI);


        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .put(testIdStatusURI)
            .then()
            .extract()
            .response();

        TaskDto actual = assertValidTaskResponse(response);
        assertEquals(testStatus, actual.getStatus());
        testTaskInMemory = actual;
    }

    @Test
    @Order(4)
    void shouldUpdateTask() {
        String testTitle = "change test title";
        TaskDto testTask = testTaskInMemory;
        testTask.setTitle(testTitle);

        log("update:" + testTask);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(testTask)
            .when()
            .post("/update")
            .then()
            .extract()
            .response();

        TaskDto actual = assertValidTaskResponse(response);
        assertEquals(testTitle, actual.getTitle());
        testTaskInMemory = actual;
    }

    @Test
    @Order(5)
    void shouldRetrieveTask() {

        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .get("/get/" + testTaskInMemory.getId())
            .then()
            .extract()
            .response();

        TaskDto actual = assertValidTaskResponse(response);
        assertEquals(testTaskInMemory.getId(), actual.getId());
        assertEquals(testTaskInMemory.getTitle(), actual.getTitle());
        assertEquals(testTaskInMemory.getDescription(), actual.getDescription());
        assertEquals(testTaskInMemory.getStatus(), actual.getStatus());
        assertEquals(testTaskInMemory.getDue(), actual.getDue());
    }

    @Test
    @Order(6)
    void shouldRetrieveAll() {

        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .get("/get-all-tasks")
            .then()
            .extract()
            .response();

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        TaskDto[] actualArray = response.body().as(TaskDto[].class);
        assertTrue(actualArray.length > 0);

        Arrays.stream(actualArray).forEach(dto -> log("get-all-tasks:" + dto));
    }

    @Test
    @Order(7)
    void shouldDeleteTask() {
        String testTaskId = testTaskInMemory.getId();

        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .delete("/delete/" + testTaskId)
            .then()
            .extract()
            .response();

        assertEquals(204, response.statusCode());
        testTaskInMemory = null;
    }
}
