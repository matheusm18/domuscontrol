package domuscontrol.exceptions;

public class HouseNotFoundException extends RuntimeException {

    /** Exception thrown when a requested house is not found in the system. */
    public HouseNotFoundException(String exception) {
        super(exception);
    }
}