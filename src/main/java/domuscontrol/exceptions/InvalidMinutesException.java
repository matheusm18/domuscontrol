package domuscontrol.exceptions;

/**
 * Exception thrown when a simulation advance receives an invalid minute count.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class InvalidMinutesException extends IllegalArgumentException {

    /**
     * Constructs an InvalidMinutesException with the invalid minute value.
     *
     * @param minutes the invalid minute count
     */
    public InvalidMinutesException(int minutes) {
        super("" + minutes);
    }
}
