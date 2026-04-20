package domuscontrol.exceptions;

public class HouseAlreadyExistsException extends RuntimeException {

    /** Exception thrown when a requested house already exists in the system. */
    public HouseAlreadyExistsException(String exception) {
        super(exception);
    }
}