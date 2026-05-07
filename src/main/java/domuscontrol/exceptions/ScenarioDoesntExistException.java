package domuscontrol.exceptions;

/**
 * Exception thrown when a requested scenario does not exist in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class ScenarioDoesntExistException extends Exception {

    /**
     * Constructs a ScenarioDoesntExistException when a requested scenario does not exist in the system.
     *
     * @param message the error message
     */
    public ScenarioDoesntExistException(String message) {
        super(message);
    }
}
