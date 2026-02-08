package uk.co.frankz.hmcts.dts.service;

import uk.co.frankz.hmcts.dts.model.EntityWithId;

import java.util.Optional;

/**
 * TaskStore represents the API of the back-end persistence database to store the Task entity.
 * As an interface we allow different implementations.
 * <br>
 * The entity fields are not needed to know for the API, only that there
 * is a unique identifier, which is to be assumed a string.
 */
public interface TaskStore<T extends EntityWithId> {

    <S extends T> S save(S entity);

    Optional<T> findById(String s);

    void deleteById(String s);

    Iterable<T> findAll();

    void healthCheck();
}
