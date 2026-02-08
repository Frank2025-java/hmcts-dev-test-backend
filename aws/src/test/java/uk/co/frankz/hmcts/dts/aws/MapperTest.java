package uk.co.frankz.hmcts.dts.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskJsonException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MapperTest {

    Mapper testSubject;

    static final String TEST_ID = "12344";

    static final String TEST_TIME_STR = "2026-02-05T10:11:30";

    static final LocalDateTime TEST_TIME = LocalDateTime.parse(TEST_TIME_STR);

    static final String TEST_DESC = "dddd";

    static final String TEST_TITLE = "tttt ";

    static final String TEST_STATUS = Status.Deleted.name();

    static final String JACKSON_NEWLINE = System.lineSeparator();

    static final String TEST_JSON = "{" + JACKSON_NEWLINE
        + "  \"id\" : \"" + TEST_ID + "\"," + JACKSON_NEWLINE
        + "  \"title\" : \"" + TEST_TITLE + "\"," + JACKSON_NEWLINE
        + "  \"description\" : \"" + TEST_DESC + "\"," + JACKSON_NEWLINE
        + "  \"status\" : \"" + TEST_STATUS + "\"," + JACKSON_NEWLINE
        + "  \"due\" : \"" + TEST_TIME_STR + "\"" + JACKSON_NEWLINE
        + "}";

    TaskWithId testTask;

    TaskDto testDto;

    ObjectMapper real = new ObjectMapper();

    @BeforeEach
    void setup() {
        // for convenience and testing the Jackson settings, use the real jackson json mapper
        testSubject = new Mapper();

        testDto = new TaskDto();
        testDto.setId(TEST_ID);
        testDto.setDue(TEST_TIME);
        testDto.setTitle(TEST_TITLE);
        testDto.setDescription(TEST_DESC);
        testDto.setStatus(TEST_STATUS);

        testTask = new TaskWithId();
        testTask.setId(TEST_ID);
        testTask.setDescription(TEST_DESC);
        testTask.setDue(TEST_TIME);
        testTask.setTitle(TEST_TITLE);
        testTask.setStatus(Status.valueOf(TEST_STATUS));
    }

    @Test
    void shouldCreateNewEntityWitId() {
        // given

        // when
        Task actual = testSubject.newEntityWitId(TEST_ID);

        // then
        assertInstanceOf(TaskWithId.class, actual);
        assertEquals(TEST_ID, ((TaskWithId) actual).getId());
    }

    @Test
    void shouldPropagateExceptionOnCreateDto() {
        // given
        String given = "not a json string";

        // when
        TaskJsonException actual = assertThrows(TaskJsonException.class, () -> testSubject.toDto(given));

        // then
        assertTrue(actual.getMessage().contains(given));
    }

    @Test
    void shouldCreateDto() {
        // given

        // when
        TaskDto actual = testSubject.toDto(TEST_JSON);

        // then
        assertNotNull(actual);
        assertEquals(TEST_ID, actual.getId());
    }

    @Test
    void shouldPropagateExceptionOnToEntity() {
        // given
        String given = "not a json string";

        // when
        TaskJsonException actual = assertThrows(TaskJsonException.class, () -> testSubject.toEntity(given));

        // then
        assertTrue(actual.getMessage().contains(given));
    }

    @Test
    void toEntity() {
        // given

        // when
        TaskWithId actual = testSubject.toEntity(TEST_JSON);

        // then
        assertNotNull(actual);
        assertEquals(TEST_ID, actual.getId());
    }

    @Test
    void shouldCreateJsonStringFromDto() {
        // given

        // when
        String actual = testSubject.toJsonString(testDto);

        // then
        assertEquals(TEST_JSON, actual);
    }

    @Test
    void shouldCreateJsonStringFromEntity() {
        // given

        // when
        String actual = testSubject.toJsonString(testTask);

        // then
        assertEquals(TEST_JSON, actual);
    }

    @Test
    void shouldCreateJsonStringFromDtos() {
        // given
        TaskDto[] given = new TaskDto[]{testDto, testDto};
        String expected = "[" + TEST_JSON + "," + TEST_JSON + "]";

        // when
        String actual = testSubject.toJsonString(given);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldPropagateExceptionOnCreateJsonStringFromDto() throws JsonProcessingException {
        // given
        ObjectMapper mockJackson = mock(ObjectMapper.class);
        Mapper testSubjectMocked = new Mapper(mockJackson);
        JsonProcessingException givenEx = mock(JsonEOFException.class);
        when(mockJackson.writeValueAsString(any())).thenThrow(givenEx);

        // when
        TaskJsonException actual = assertThrows(
            TaskJsonException.class,
            () -> testSubjectMocked.toJsonString(testDto)
        );

        // then
        assertInstanceOf(TaskJsonException.class, actual);
    }

}
