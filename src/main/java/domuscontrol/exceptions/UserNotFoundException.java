package domuscontrol.exceptions;

public class UserNotFoundException extends Exception {

    /** Exception thrown when a requested user is not found in the system. */
    public UserNotFoundException(String exception) {
        super(exception);
    }
}
