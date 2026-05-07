package domuscontrol.exceptions;

/**
 * Exception thrown when a requested schedule does not exist in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class ScheduleDoesntExistException extends Exception {

    /**
     * Constructs a ScheduleDoesntExistException when a requested schedule does not exist in the system.
     *
     * @param message the error message
     */
    public ScheduleDoesntExistException(String message) {
        super(message);
    }
}
