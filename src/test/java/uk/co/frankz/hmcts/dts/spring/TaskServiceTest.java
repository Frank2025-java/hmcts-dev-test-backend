package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "org.eclipse.store.storage-directory=build/eclipse-store-storage/taskservicetest"
    },
    classes = Application.class)
class TaskServiceTest {

    @MockitoBean
    RepositoryHealthIndicator mockHealthBean;

    @Autowired
    TaskService testSubject;

    @Mock
    Health mockHealth;

    @BeforeEach
    void setup() {
        when(mockHealthBean.health()).thenReturn(mockHealth);
    }

    @Test
    void shouldHaveAutowired() {
        assertNotNull(testSubject);
    }

    @Test
    void shouldHealthCheckHealthy() {
        // given
        when(mockHealth.getStatus()).thenReturn(Status.UP);

        // when
        testSubject.healthCheck();

        // then
        verify(mockHealthBean).health();
    }

    @Test
    void shouldHealthCheckSick() {
        // given
        when(mockHealth.getStatus()).thenReturn(Status.DOWN);

        // when
        TaskException e = assertThrows(TaskException.class, () -> testSubject.healthCheck());

        // then
        verify(mockHealthBean).health();
        assertNotNull(e);
    }

}
