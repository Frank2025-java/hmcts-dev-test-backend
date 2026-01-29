package uk.co.frankz.hmcts.dts.model.exception;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class TaskNoMatchException extends TaskException {

    public TaskNoMatchException(String invalid, Stream<String> valid) {
        super(invalid + " is not matching one of " + valid.collect(joining(", ")));
    }

    public TaskNoMatchException(String invalid, String[] valid) {
        this(invalid, Arrays.stream(valid));
    }
}
