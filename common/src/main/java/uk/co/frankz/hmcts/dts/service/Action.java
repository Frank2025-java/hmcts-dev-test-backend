package uk.co.frankz.hmcts.dts.service;

import uk.co.frankz.hmcts.dts.model.exception.TaskNoMatchException;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Action enum intension is to keep the REST API frameworks in sync for the services that need to
 * be offered.
 */
public enum Action {
    CREATE(PATH.CREATE),
    DELETE(PATH.DELETE),
    GET(PATH.GET),
    GET_ALL(PATH.GET_ALL),
    UPDATE_STATUS(PATH.UPDATE_STATUS),
    UPDATE(PATH.UPDATE),
    ROOT(PATH.ROOT);

    public interface PARM {
        String ID = "id";
        String STATUS = "status";
    }

    public interface PATH {
        String CREATE = "/create";
        String DELETE = "/delete/{" + PARM.ID + "}";
        String GET = "/get/{" + PARM.ID + "}";
        String GET_ALL = "/get-all-tasks";
        String UPDATE_STATUS = "/update/{" + PARM.ID + "}/status/{" + PARM.STATUS + "}";
        String UPDATE = "/update";
        String ROOT = "/";
    }

    private final String path;

    Action(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static Stream<Action> stream() {
        return Arrays.stream(Action.values());
    }

    public static Action fromPath(String matchArgument) {

        Optional<Action> found = stream().filter(action -> action.path.equals(matchArgument)).findFirst();

        if (found.isEmpty()) {

            throw new TaskNoMatchException(matchArgument, stream().map(Action::getPath));
        } else {
            return found.get();
        }

    }

    public static Stream<String> names(Action... actions) {
        return Arrays.stream(actions).map(Action::name);
    }

}
