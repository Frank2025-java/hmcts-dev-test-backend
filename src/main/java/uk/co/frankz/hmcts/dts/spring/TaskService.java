package uk.co.frankz.hmcts.dts.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;
import uk.co.frankz.hmcts.dts.model.exception.TaskStoreException;

import static uk.co.frankz.hmcts.dts.spring.RepositoryHeathIndicator.print;

@Service
public class TaskService extends uk.co.frankz.hmcts.dts.service.TaskService<TaskWithId> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final HealthIndicator healthChecker;

    @Autowired
    public TaskService(TaskStore taskStore, RepositoryHeathIndicator health) {
        super(taskStore);
        this.healthChecker = health;
    }

    @Override
    public void healthCheck() {

        Health health = healthChecker.health();
        LOG.info(print(health));

        Status status = health.getStatus();

        if (Status.UP != status) {
            throw new TaskStoreException("Health " + status);
        }
        // else we are healthy, and return silently
    }
}
