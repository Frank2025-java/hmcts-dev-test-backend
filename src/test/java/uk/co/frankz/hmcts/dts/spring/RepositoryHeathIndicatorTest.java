package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryHeathIndicatorTest {

    RepositoryHeathIndicator testSubject;

    @Mock
    TaskStore mockStore;

    @BeforeEach
    void setup() {
        testSubject = new RepositoryHeathIndicator(mockStore);
    }

    @Test
    void shouldShowHealthy() {
        // given
        when(mockStore.count()).thenReturn(10L);

        // when
        Health actual = testSubject.health();

        // then
        verify(mockStore).count();
        assertNotNull(actual);
        assertSame(Status.UP, actual.getStatus());
    }

    @Test
    void shouldShowSick() {
        // given
        String givenMessage = " t e st";
        when(mockStore.count()).thenThrow(new RuntimeException(givenMessage));

        // when
        Health actual = testSubject.health();

        // then
        verify(mockStore).count();
        assertNotNull(actual);
        assertSame(Status.DOWN, actual.getStatus());
        String actualAsString = RepositoryHeathIndicator.print(actual);
        assertTrue(actualAsString.contains(givenMessage), actualAsString);
    }

}
