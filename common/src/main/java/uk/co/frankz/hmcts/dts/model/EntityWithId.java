package uk.co.frankz.hmcts.dts.model;

/**
 * Interface EntityWithId allows
 * TaskWithId implementations where the identity is generated
 * by a framework. Example is Spring, which works with an annotation on a field.
 */
public interface EntityWithId {

    boolean isNew();

    String getId();
}
