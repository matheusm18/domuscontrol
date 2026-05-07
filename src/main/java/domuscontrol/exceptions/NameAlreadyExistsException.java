package domuscontrol.exceptions;

/**
 * Exception thrown when attempting to create an entity with a name that already exists.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class NameAlreadyExistsException extends Exception {

    /**
     * Constructs a NameAlreadyExistsException when attempting to create an entity with a name that already exists.
     *
     * @param message the error message
     */
    public NameAlreadyExistsException(String message) {
        super(message);
    }
}