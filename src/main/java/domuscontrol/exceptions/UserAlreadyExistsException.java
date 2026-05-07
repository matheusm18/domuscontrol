package domuscontrol.exceptions;

/**
 * Exception thrown when attempting to create a user with an email that already exists in the system.
 * 
 * @author Afonso Barros a112178
 * @author Martim Monteiro a111013
 * @author Matheus Azevedo a111430
 * @version 1.0
 */
public class UserAlreadyExistsException extends Exception {

    /**
     * Constructs a UserAlreadyExistsException when a user with the same email already exists in the system.
     * 
     * @param exception the error message
     */
    public UserAlreadyExistsException(String exception) {
        super(exception);
    }
}
