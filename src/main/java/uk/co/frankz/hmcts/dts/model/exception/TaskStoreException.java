package uk.co.frankz.hmcts.dts.model.exception;

import uk.co.frankz.hmcts.dts.model.Task;

public class TaskStoreException extends TaskException {

    public TaskStoreException(Task task, String s) {
        super(task, s);
    }

    public TaskStoreException(Exception e) {
        super(e);
    }

    public TaskStoreException(Task task, Exception e) {
        super(task, e);
    }
}
