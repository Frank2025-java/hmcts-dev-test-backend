package uk.co.frankz.hmcts.dts.model.exception;

import org.apache.commons.lang3.StringUtils;
import uk.co.frankz.hmcts.dts.model.Task;

public class TaskException extends IllegalArgumentException {

    public TaskException(Task task, String s) {
        super(toTaskString(task) + ": " + s);
    }

    public TaskException(Task task, Exception e) {
        this(task, toString(e));
    }

    public TaskException(Exception e) {
        this(toString(e));
    }

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String s, Exception e) {
        super(s + ": " + toString(e));
    }

    private static String toTaskString(Task task) {
        return task == null ? "Task null" : task.toString();
    }

    private static String toString(Exception e) {
        if (e instanceof TaskException) {
            // Do not double wrap TaskException
            return e.getMessage();
        } else if (StringUtils.isBlank(e.getMessage())) {
            // Do not rely on e.getMessage, as, for example, null pointers have a blank message
            return e.toString();
        } else {
            // Return some feedback on exception, but do not return stacktrace to reveal to much
            return e.getMessage();
        }
    }
}
