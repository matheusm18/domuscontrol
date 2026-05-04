package domuscontrol.exceptions;

public class DeviceNotFoundException extends Exception {

    /** Exception thrown when a requested device is not found in the system. */
    public DeviceNotFoundException(String exception) {
        super(exception);
    }
}
