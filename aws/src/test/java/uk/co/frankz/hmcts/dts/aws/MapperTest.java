package uk.co.frankz.hmcts.dts.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;
import uk.co.frankz.hmcts.dts.dto.TaskDto;
import uk.co.frankz.hmcts.dts.model.Status;
import uk.co.frankz.hmcts.dts.model.Task;
import uk.co.frankz.hmcts.dts.model.exception.TaskJsonException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Arrays.stream;
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

    static final UUID TEST_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");

    static final String TEST_TIME_LOCAL_STR = "2026-04-05T10:11:30";

    static final LocalDateTime TEST_TIME_LOCAL = LocalDateTime.parse(TEST_TIME_LOCAL_STR);

    // the offset "+01:00" in "+01:00[Europe/London]"
    static final String ZONE_OFFSET = uk.co.frankz.hmcts.dts.dto.Mapper.isoZoneOffset(TEST_TIME_LOCAL);

    static final String TEST_TIME_ZONED_STR = TEST_TIME_LOCAL_STR + ZONE_OFFSET;

    static final ZonedDateTime TEST_TIME_BST = ZonedDateTime.parse(TEST_TIME_ZONED_STR);

    static final String TEST_DESC = "dddd";

    static final String TEST_TITLE = "tttt ";

    static final String TEST_STATUS = Status.Deleted.name();

    static final String JACKSON_NEWLINE = System.lineSeparator();

    static final String TEST_JSON = "{" + JACKSON_NEWLINE
        + "  \"description\" : \"" + TEST_DESC + "\"" + "," + JACKSON_NEWLINE
        + "  \"due\" : \"" + TEST_TIME_ZONED_STR + "\"" + "," + JACKSON_NEWLINE
        + "  \"id\" : \"" + TEST_ID + "\"" + "," + JACKSON_NEWLINE
        + "  \"status\" : \"" + TEST_STATUS + "\"" + "," + JACKSON_NEWLINE
        + "  \"title\" : \"" + TEST_TITLE + "\"" + JACKSON_NEWLINE
        + "}";

    TaskWithId testTask;

    TaskDto testDto;

    JsonMapper real = Mapper.JACKSON;

    @BeforeEach
    void setup() {
        // for convenience and testing the Jackson settings, use the real jackson json mapper
        testSubject = new Mapper(real);

        testDto = new TaskDto();
        testDto.setId(TEST_ID.toString());
        testDto.setDue(TEST_TIME_BST);
        testDto.setTitle(TEST_TITLE);
        testDto.setDescription(TEST_DESC);
        testDto.setStatus(TEST_STATUS);

        testTask = new TaskWithId();
        testTask.setUUID(TEST_ID);
        testTask.setDescription(TEST_DESC);
        testTask.setDue(TEST_TIME_LOCAL);
        testTask.setTitle(TEST_TITLE);
        testTask.setStatus(Status.valueOf(TEST_STATUS));
    }

    @Test
    void shouldCreateNewEntityWitId() {
        // given

        // when
        Task actual = testSubject.newEntityWitId(TEST_ID.toString());

        // then
        assertInstanceOf(TaskWithId.class, actual);
        assertEquals(TEST_ID, ((TaskWithId) actual).getUUID());
        assertEquals(TEST_ID.toString(), ((TaskWithId) actual).getId());
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
        assertEquals(TEST_ID.toString(), actual.getId());
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
        assertEquals(TEST_ID.toString(), actual.getId());
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
    void shouldPropagateExceptionOnCreateJsonStringFromDto() throws JacksonException {
        // given
        JsonMapper mockJackson = mock(JsonMapper.class);
        Mapper testSubjectMocked = new Mapper(mockJackson);
        JacksonException givenEx = mock(JacksonException.class);
        when(mockJackson.writeValueAsString(any())).thenThrow(givenEx);

        // when
        TaskJsonException actual = assertThrows(
            TaskJsonException.class,
            () -> testSubjectMocked.toJsonString(testDto)
        );

        // then
        assertInstanceOf(TaskJsonException.class, actual);
    }

    @Test
    void shouldThrowOnNoJson() {
        // given
        String givenJsonIn = "";

        // when
        Exception actual = assertThrows(Exception.class, () -> testSubject.toDto(givenJsonIn));

        // then
        assertInstanceOf(TaskJsonException.class, actual);
    }

    @Test
    void shouldConvertBlankJson() {
        // given
        String givenJsonIn = "{ }";
        String[] expectedOut = {"\"id\" : ", "\"title\" : ", "\"due\" : ", "\"status\" : ", "\"description\" : "};

        // when
        String actualJsonOut = testSubject.toJsonString(testSubject.toDto(givenJsonIn));

        // then
        stream(expectedOut).forEach((expectField) -> assertContains(actualJsonOut, expectField));
    }

    @Test
    void shouldDropTimeZoneRegion() {
        // given
        String givenDueIn = "2026-04-05T10:11:30+02:00[Europe/Amsterdam]";
        String givenJsonIn = "{ \"due\" : \"" + givenDueIn + "\" }";
        ZonedDateTime zonedDue = ZonedDateTime.parse("2026-04-05T10:11:30+02:00[Europe/Amsterdam]");
        LocalDateTime localDue = uk.co.frankz.hmcts.dts.dto.Mapper.local(zonedDue);
        String expectedZoneOffset = uk.co.frankz.hmcts.dts.dto.Mapper.isoZoneOffset(localDue);
        String expectedDueOut = "\"due\" : \"" + localDue + expectedZoneOffset + "\"";

        // when
        String actualJsonOut = testSubject.toJsonString(testSubject.toDto(givenJsonIn));

        // then
        assertContains(actualJsonOut, expectedDueOut);
    }

    @Test
    void shouldThrowOnWeirdTimeZoneRegion() {
        // given
        String givenJsonIn = "{ \"due\" : \"2026-04-05T10:11:30+04:50[Moon/SouthPoleBase]\" }";

        // when
        Exception actual = assertThrows(Exception.class, () -> testSubject.toDto(givenJsonIn));

        // then
        assertInstanceOf(TaskJsonException.class, actual);
    }

    static void assertContains(String text, String substring) {
        assertNotNull(text);
        assertTrue(
            text.contains(substring),
            () -> "Expected text to contain <" + substring + "> but was <" + text + ">"
        );
    }

}
