package uk.co.frankz.hmcts.dts.model.exception;

import uk.co.frankz.hmcts.dts.model.EntityWithId;
import uk.co.frankz.hmcts.dts.model.Task;

public class TaskInvalidArgumentException extends TaskException {

    public TaskInvalidArgumentException(EntityWithId taskWithId, String s) {

        this(task(taskWithId), s);
    }

    public TaskInvalidArgumentException(EntityWithId taskWithId, Exception e) {

        this(task(taskWithId), e);
    }

    public TaskInvalidArgumentException(Task task, String s) {

        super(task, s);
    }

    public TaskInvalidArgumentException(Task task, Exception e) {

        super(task, e);
    }

    public TaskInvalidArgumentException(String s, Exception e) {

        super(s, e);
    }

    public TaskInvalidArgumentException(String message) {

        super(message);
    }

}
