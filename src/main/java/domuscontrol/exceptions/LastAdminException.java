package domuscontrol.exceptions;

public class LastAdminException extends Exception {

    /** Exception thrown when trying to remove the last administrator from a house. */
    public LastAdminException(String message) {
        super(message);
    }
}
