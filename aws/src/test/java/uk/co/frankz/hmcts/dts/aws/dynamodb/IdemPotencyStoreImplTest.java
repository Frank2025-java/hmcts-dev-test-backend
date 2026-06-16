package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.frankz.hmcts.dts.model.IdemPotencyHash;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyAlreadyProcessedException;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyRuntimeException;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdemPotencyStoreImplTest {

    private IdemPotencyStoreImpl testSubject;

    private static final IdemPotencyScopeKey TEST_KEY =
        new IdemPotencyScopeKey("POST", "/x", "abc");

    private static final String TEST_ID = TEST_KEY.sha256().value();

    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2026, 6, 12, 17, 30, 59);

    private static final IdemPotencyRecord TEST_RECORD = new IdemPotencyRecord(
        TEST_KEY.sha256(),                                 // IdemPotencyHash key
        TEST_TIME, // createdAt
        IdemPotencyHash.sha256Hex("request body".getBytes(UTF_8)),          // requestHash
        "response-body".getBytes(UTF_8), // responseBody
        200,                                          // statusCode
        "application/json"                            // contentType
    );

    private static final IdemPotencyEntity TEST_ENTITY = IdemPotencyEntity.toEntity(TEST_RECORD);

    @Mock
    private DynamoDbTableProvider<IdemPotencyEntity> mockProvider;

    @BeforeEach
    void setup() {
        testSubject = new IdemPotencyStoreImpl(mockProvider);
    }

    @Test
    void shouldReturnEmptyOptionalWhenNotFound() {
        // given
        when(mockProvider.find(any())).thenReturn(null);

        // when
        Optional<IdemPotencyRecord> result = testSubject.findById(TEST_KEY);

        // then
        assertTrue(result.isEmpty());
        verify(mockProvider).find(TEST_ID);
    }

    @Test
    void shouldReturnRecordWhenFound() {
        // given
        when(mockProvider.find(any())).thenReturn(TEST_ENTITY);

        // when
        Optional<IdemPotencyRecord> result = testSubject.findById(TEST_KEY);

        // then
        assertTrue(result.isPresent());
        assertEquals(TEST_RECORD.key(), result.get().key());
        verify(mockProvider).find(TEST_ID);
    }

    @Test
    void shouldThrowAlreadyProcessedWhenDuplicateExists() {
        // given
        when(mockProvider.find(any())).thenReturn(TEST_ENTITY);

        // when / then
        assertThrows(
            IdemPotencyAlreadyProcessedException.class,
            () -> testSubject.saveSafe(TEST_RECORD)
        );
    }

    @Test
    void shouldSaveRecordWhenNotDuplicate() throws Exception {
        // given
        when(mockProvider.find(any())).thenReturn(null);

        // when
        assertDoesNotThrow(() -> testSubject.saveSafe(TEST_RECORD));

        // then
        verify(mockProvider, times(1)).save(any(IdemPotencyEntity.class));
        verify(mockProvider).find(TEST_ID);
    }

    @Test
    void shouldWrapExceptionsDuringSave() throws Exception {
        // given
        when(mockProvider.find(any())).thenReturn(null);
        doThrow(new RuntimeException("boom")).when(mockProvider).save(any());

        // when / then
        assertThrows(
            IdemPotencyRuntimeException.class,
            () -> testSubject.saveSafe(TEST_RECORD)
        );
        verify(mockProvider).find(TEST_ID);
    }
}
