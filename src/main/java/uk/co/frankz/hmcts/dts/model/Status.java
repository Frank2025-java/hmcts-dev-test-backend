package uk.co.frankz.hmcts.dts.model;

import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;

import static java.util.Arrays.stream;

/**
 * Enumeration Status represents the various states of the Task domain entity,
 * and currently we only have a requirement for an initial state and a deleted state.
 */
public enum Status {
    Initial, Deleted;

    /**
     * Same as valueOf, except that in case of an illegal argument,
     * the error message is more informative in what the legal
     * arguments can be.
     *
     * @param status name of the Status enum
     * @return Status instance
     */
    public static Status parse(String status) {
        try {
            return Status.valueOf(status);
        } catch (Exception e) {
            throw new TaskNoMatchException(status, stream(Status.values()).map(Status::name));
        }
    }
}
