package domuscontrol.exceptions;

/**
 * Exception thrown when a requested user is not found in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class UserNotFoundException extends Exception {

    /**
     * Constructs a UserNotFoundException when a requested user is not found in the system.
     *
     * @param exception the error message
     */
    public UserNotFoundException(String exception) {
        super(exception);
    }
}
