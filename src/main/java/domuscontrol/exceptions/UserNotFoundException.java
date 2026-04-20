package domuscontrol.exceptions;

public class UserNotFoundException extends RuntimeException {

    /** Exception thrown when a requested user is not found in the system. */
    public UserNotFoundException(String exception) {
        super(exception);
    }
}