package domuscontrol.exceptions;

/**
 * Exception thrown when attempting to create a house that already exists.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class HouseAlreadyExistsException extends Exception {

    /**
     * Constructs a HouseAlreadyExistsException when the given house name already exists in the system.
     *
     * @param exception the error message
     */
    public HouseAlreadyExistsException(String exception) {
        super(exception);
    }
}
