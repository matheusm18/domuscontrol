package domuscontrol.exceptions;

/**
 * Exception thrown when a requested automation does not exist in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class AutomationDoesntExistException extends Exception {

    /**
     * Constructs an AutomationDoesntExistException when a requested automation is not found in the system.
     *
     * @param message the error message
     */
    public AutomationDoesntExistException(String message) {
        super(message);
    }
}
