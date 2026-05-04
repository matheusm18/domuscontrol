package domuscontrol.exceptions;

public class UserNotLoggedInException extends Exception {

    /** Exception thrown when a user is not logged in. */
    public UserNotLoggedInException(String exception) {
        super(exception);
    }
}
