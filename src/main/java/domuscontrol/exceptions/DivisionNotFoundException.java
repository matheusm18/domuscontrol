package domuscontrol.exceptions;

public class DivisionNotFoundException extends RuntimeException {

    /** Exception thrown when a requested division is not found in the system. */
    public DivisionNotFoundException(String exception) {
        super(exception);
    }
}