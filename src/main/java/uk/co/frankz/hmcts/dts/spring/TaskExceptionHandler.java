package uk.co.frankz.hmcts.dts.spring;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.co.frankz.hmcts.dts.model.exception.TaskException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class TaskExceptionHandler {

    @ExceptionHandler(TaskException.class)
    public ResponseEntity<Object> handleTaskException(TaskException exception) {
        return new ResponseEntity<>(exception.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), INTERNAL_SERVER_ERROR);
    }
}
