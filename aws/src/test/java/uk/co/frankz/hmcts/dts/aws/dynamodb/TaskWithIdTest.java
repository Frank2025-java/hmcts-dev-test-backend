package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import uk.co.frankz.hmcts.dts.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskWithIdTest {

    private TaskWithId testSubject;

    private TableSchema<TaskWithId> dynamoIntrospectTestSubject;

    private static final UUID TEST_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");

    private static final LocalDateTime TEST_TIME = LocalDateTime.parse("2026-02-28T16:45:30");

    private static final BiConsumer<TaskWithId, String> SETTER_TITLE = TaskWithId::setTitle;
    private static final BiConsumer<TaskWithId, String> SETTER_DESC = TaskWithId::setDescription;
    private static final BiConsumer<TaskWithId, Status> SETTER_STATUS = TaskWithId::setStatus;
    private static final BiConsumer<TaskWithId, LocalDateTime> SETTER_DUE = TaskWithId::setDue;
    private static final BiConsumer<TaskWithId, UUID> SETTER_UUID = TaskWithId::setUUID;

    @BeforeEach
    void setup() {
        testSubject = new TaskWithId();
        dynamoIntrospectTestSubject = TableSchema.fromBean(TaskWithId.class);
    }

    @Test
    void shouldHaveDefaultNullInitialId() {
        assertNull(testSubject.getId());
    }

    @Test
    void shouldGeneratedUuidOnTheFly() {
        // given
        testSubject.setUUID(null);

        // when
        UUID actual = testSubject.getUUID();

        // then
        assertNotNull(actual);
        assertEquals(actual.toString(), testSubject.getId());
        assertNotEquals(TEST_ID, actual);
    }

    @Test
    void shouldBeAbleToSetPropertyUUID() {
        // given
        UUID given = TEST_ID;

        // when
        testSubject.setUUID(given);

        // then
        assertEquals(given, testSubject.getUUID());
    }

    @Test
    void shouldBeAbleToSetPropertyTitle() {
        // given
        String given = "test title";

        // when
        testSubject.setTitle(given);

        // then
        assertEquals(given, testSubject.getTitle());
    }

    @Test
    void shouldBeAbleToSetPropertyStatus() {
        // given
        Status given = Status.Deleted;

        // when
        testSubject.setStatus(given);

        // then
        assertEquals(given, testSubject.getStatus());
    }

    @Test
    void shouldBeAbleToSetPropertyDescription() {
        // given
        String given = "test xxxx";

        // when
        testSubject.setDescription(given);

        // then
        assertEquals(given, testSubject.getDescription());
    }

    @Test
    void shouldBeAbleToSetPropertyDue() {
        // given
        LocalDateTime given = TEST_TIME;

        // when
        testSubject.setDue(given);

        // then
        assertEquals(given, testSubject.getDue());
    }

    @Test
    void shouldIntrospectBeanProperties() {
        // given

        // when
        List<String> actual = dynamoIntrospectTestSubject.attributeNames();

        // then
        assertNotNull(actual);
        assertEquals("desc,due,id,status,title", actual.stream().sorted().collect(joining(",")));
    }

    static Stream<Arguments> propertyProvider() {
        return Stream.of(
            Arguments.of("title", SETTER_TITLE, "test Tilte"),
            Arguments.of("desc", SETTER_DESC, null),
            Arguments.of("desc", SETTER_DESC, "des cript test"),
            Arguments.of("status", SETTER_STATUS, Status.Deleted),
            Arguments.of("due", SETTER_DUE, TEST_TIME),
            Arguments.of("id", SETTER_UUID, TEST_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("propertyProvider")
    <T> void shouldIntrospectBeanProperty(
        String givenName,
        BiConsumer<TaskWithId, T> givenSetter,
        T givenValue) {

        // given
        givenSetter.accept(testSubject, givenValue);
        AttributeValue givenMappedValue = dynamoIntrospectTestSubject.attributeValue(testSubject, givenName);

        // when
        var actualConvertor = (AttributeConverter<T>) dynamoIntrospectTestSubject.converterForAttribute(givenName);
        T actual = givenValue == null ? null : actualConvertor.transformTo(givenMappedValue);

        // then
        assertEquals(givenValue, actual);
    }

}
