package domuscontrol.exceptions;

public class LoginInvalidPasswordException extends RuntimeException {

    /** Exception thrown when the provided password is invalid. */
    public LoginInvalidPasswordException(String exception) {
        super(exception);
    }
}