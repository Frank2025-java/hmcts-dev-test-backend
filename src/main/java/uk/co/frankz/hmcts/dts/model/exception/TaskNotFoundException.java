package uk.co.frankz.hmcts.dts.model.exception;

public class TaskNotFoundException extends TaskException {

    public TaskNotFoundException(String id) {
        super(id);
    }
}
