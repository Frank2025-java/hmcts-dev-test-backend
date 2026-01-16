package uk.co.frankz.hmcts.dts.model;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Enumeration Status represents the various states of the Task domain entity,
 * and currently we only have a requirement for an initial state and a deleted state.
 */
public enum Status {
    Initial, Deleted;

    public static String names() {
        return stream(values()).map(Status::name).collect(joining(", "));
    }

    /**
     * Same as valueOf, except that in case of an illegal argument,
     * the error message is more informative in what the legal
     * arguments can be.
     * @param status name of the Status enum
     * @return Status instance
     */
    public static Status parse(String status) {
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            String invalid = "Invalid status: " + status;
            String valid = "Valid status: " + Status.names();
            throw new IllegalArgumentException(invalid + ". " + valid);
        }
    }
}
