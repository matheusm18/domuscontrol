package domuscontrol.exceptions;

/**
 * Exception thrown when login credentials are invalid (incorrect password).
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class LoginInvalidPasswordException extends Exception {

    /**
     * Constructs a LoginInvalidPasswordException when login credentials are invalid (incorrect password).
     *
     * @param exception the error message
     */
    public LoginInvalidPasswordException(String exception) {
        super(exception);
    }
}
