package domuscontrol.exceptions;

public class HouseNotFoundException extends Exception {

    /** Exception thrown when a requested house is not found in the system. */
    public HouseNotFoundException(String exception) {
        super(exception);
    }
}
