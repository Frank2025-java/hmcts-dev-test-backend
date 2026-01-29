package uk.co.frankz.hmcts.dts.spring;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Spring bean to check the health of the Repository, using
 * Spring Boot actuator framework.
 * It is used in {@link TaskService#healthCheck()}.
 */
@Component
public class RepositoryHeathIndicator implements HealthIndicator {

    private final CrudRepository<TaskWithId, String> repository;

    @Autowired
    public RepositoryHeathIndicator(CrudRepository<TaskWithId, String> repository) {
        this.repository = repository;
    }

    @Override
    public @NotNull Health health() {
        Health.Builder upOrDown;
        try {
            repository.count();
            upOrDown = Health.up();
        } catch (Exception e) {
            upOrDown = Health.down(e);
        }
        return upOrDown.build();
    }

    public static @NotNull String print(Health health) {

        StringBuilder b = new StringBuilder();
        if (health != null) {
            append(b, "Status", health.getStatus());
            if (health.getDetails() != null) {
                health.getDetails().forEach((k, v) -> append(b, k, v));
            }
        }
        return b.toString();
    }

    private static void append(StringBuilder b, String name, Object obj) {
        b.append(name);
        b.append(':');
        b.append(obj);
        b.append('\n');
    }

}
