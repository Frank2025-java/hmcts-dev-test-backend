package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.frankz.hmcts.dts.spring.IdemPotencyEntity.toEntity;

@SpringBootTest(
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "org.eclipse.store.storage-directory=build/eclipse-store-storage/idempotencytest"
    },
    classes = Application.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdemPotencyRepositoryTest {

    @Autowired
    IdemPotencyRepository testSubject;

    private final static IdemPotencyHash TEST_KEY = IdemPotencyHash.sha256Hex(randomUUID().toString().getBytes(UTF_8));

    private final static String TEST_ID = TEST_KEY.value();

    private final static byte[] TEST_BODY = "body".getBytes(UTF_8);

    private final static IdemPotencyHash request = IdemPotencyHash.sha256Hex(TEST_BODY);

    @Test
    @Order(1)
    void shouldNotFindByIdNotExisting() {
        // given

        // when
        Optional<IdemPotencyEntity> actual = testSubject.findById(TEST_ID);

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    @Order(2)
    void storeTest() {
        LocalDateTime givenTime = LocalDateTime.parse("2026-05-11T16:45:30");

        IdemPotencyEntity testRecord = toEntity(new IdemPotencyRecord(TEST_KEY, givenTime, request, TEST_BODY, 0, ""));

        IdemPotencyEntity actual = testSubject.save(testRecord);

        assertNotNull(actual);
    }

    @Test
    @Order(3)
    void shouldFindById() {
        // given

        // when
        Optional<IdemPotencyEntity> actual = testSubject.findById(TEST_ID);

        // then
        assertTrue(actual.isPresent());
    }

    @Test
    @Order(4)
    void shouldNotFindByIdInvalidId() {
        // given
        String givenInvalid = IdemPotencyHash.sha256Hex("not existing".getBytes(UTF_8)).value();

        // when
        boolean actualExist = testSubject.existsById(givenInvalid);
        Optional<IdemPotencyEntity> actualFound = testSubject.findById(givenInvalid);

        // then
        assertFalse(actualExist);
        assertNotNull(actualFound);
        assertFalse(actualFound.isPresent());
    }

    @Test
    @Order(5)
    void shouldStoreIdemPotencyOnlyOnceWithSaveSafe() {
        // given
        IdemPotencyRecord given = testSubject.findById(TEST_ID).map(IdemPotencyEntity::toDomain).orElse(null);
        assertNotNull(given,"Should have be added in earlier test");
        LocalDateTime givenTime = LocalDateTime.parse("2026-05-11T19:00:55");

        IdemPotencyRecord givenClone = new IdemPotencyRecord(
            given.key(),
            givenTime,
            given.requestHash(),
            given.responseBody(),
            given.statusCode(),
            given.contentType()
        );

        // when
        var actual = assertThrows(
            IdemPotencyAlreadyProcessedException.class,
            () -> testSubject.saveSafe(givenClone)
        );

        // then
        assertNotNull(actual);
    }

    @Test
    @Order(5)
    void shouldUpdateIdemPotencyWithBasicSave() {
        // given
        Optional<IdemPotencyEntity> given = testSubject.findById(TEST_ID);
        assertTrue(given.isPresent());
        String givenTime = "2026-05-11T19:00:55";

        IdemPotencyEntity givenEntityClone = IdemPotencyEntity.toEntity(given.get().toDomain());
        givenEntityClone.setCreatedAt(givenTime);

        // when
        testSubject.save(givenEntityClone);
        Optional<IdemPotencyEntity> actual = testSubject.findById(TEST_ID);

        // then
        assertNotNull(actual);
        assertTrue(actual.isPresent());
        assertEquals(givenTime, actual.get().getCreatedAt());
    }

    @Test
    @Order(6)
    void shouldRemove() {
        // given

        // when
        testSubject.deleteById(TEST_ID);
        boolean actualExist = testSubject.existsById(TEST_ID);

        // then
        assertFalse(actualExist);
    }

}
