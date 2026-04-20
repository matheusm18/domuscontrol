package domuscontrol.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    /** Exception thrown when a requested user already exists in the system. */
    public UserAlreadyExistsException(String exception) {
        super(exception);
    }
}