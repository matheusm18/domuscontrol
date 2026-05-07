package domuscontrol.exceptions;

/**
 * Exception thrown when a requested division is not found in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DivisionNotFoundException extends Exception {

    /**
     * Constructs a DivisionNotFoundException when a division is not found.
     *
     * @param exception the error message
     */
    public DivisionNotFoundException(String exception) {
        super(exception);
    }
}
