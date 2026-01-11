package uk.co.frankz.hmcts.dts.model.exception;

import uk.co.frankz.hmcts.dts.model.Task;

public class TaskInvalidArgumentException extends TaskException {

    public TaskInvalidArgumentException(Task task, String s) {
        super(task, s);
    }

    public TaskInvalidArgumentException(Task task, Exception e) {

        super(task, e);
    }

    public TaskInvalidArgumentException(String s, Exception e) {
        super(s, e);
    }
}
