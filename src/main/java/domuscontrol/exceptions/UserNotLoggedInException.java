package domuscontrol.exceptions;

/**
 * Exception thrown when an operation requires a user to be logged in but they are not.
 * 
 * @author Afonso Barros a112178
 * @author Martim Monteiro a111013
 * @author Matheus Azevedo a111430
 * @version 1.0
 */
public class UserNotLoggedInException extends Exception {

    /** 
     * Constructs a UserNotLoggedInException when an operation requires a user to be logged in but they are not.
     *
     * @param exception the error message
     */
    public UserNotLoggedInException(String exception) {
        super(exception);
    }
}
