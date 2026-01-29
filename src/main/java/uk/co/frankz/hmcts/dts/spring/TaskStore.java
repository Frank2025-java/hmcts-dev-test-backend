package uk.co.frankz.hmcts.dts.spring;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TaskStore represents the API of the back-end.
 * <br>
 * To use the handy Spring JPA it extends Crud repository adaptor,
 * for which there will be an Eclipse Store implementation behind
 * the spring application, which is enabled by annotation in the
 * SpringBootApplication.
 * <br>
 * The difference between the standard Spring Data JPA, is that
 * Eclipse Store allows persistence in the cloud without
 * pinning the application down to an expensive RDS database.
 */
@Repository
public interface TaskStore
    extends
    CrudRepository<TaskWithId, String>,
    uk.co.frankz.hmcts.dts.service.TaskStore<TaskWithId> {

    @Override
    <S extends TaskWithId> S save(S entity);

    @Override
    Optional<TaskWithId> findById(String s);

    @Override
    void deleteById(String s);

    @Override
    Iterable<TaskWithId> findAll();

    /**
     * The implementation of checking the
     * health is done from {@link TaskService#healthCheck()}
     * so that this Spring Beans and the {@link RepositoryHeathIndicator}
     * are dependent on construction order.
     */
    @Override
    default void healthCheck() {
    }
}
