package domuscontrol.exceptions;

/**
 * Exception thrown when a user does not have any scenarios.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class UserDoesntHaveScenarios extends Exception {

    /**
     * Constructs a UserDoesntHaveScenarios when a user does not have any scenarios.
     *
     * @param exception the error message
     */
    public UserDoesntHaveScenarios(String exception) {
        super(exception);
    }
}
