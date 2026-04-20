package domuscontrol.exceptions;

public class UserNotLoggedInException extends RuntimeException {

    /** Exception thrown when a user is not logged in. */
    public UserNotLoggedInException(String exception) {
        super(exception);
    }
}