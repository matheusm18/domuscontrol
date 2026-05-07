package domuscontrol.exceptions;

/**
 * Exception thrown when attempting to create a time-based schedule with a non-time condition.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class ScheduleWithConditionDifferentFromTimeException extends Exception {

    /**
     * Constructs a ScheduleWithConditionDifferentFromTimeException when attempting to create a time-based schedule with a non-time condition.
     *
     * @param message the error message
     */
    public ScheduleWithConditionDifferentFromTimeException(String message) {
        super(message);
    }
}
