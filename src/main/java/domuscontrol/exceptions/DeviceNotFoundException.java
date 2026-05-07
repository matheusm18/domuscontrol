package domuscontrol.exceptions;

/**
 * Exception thrown when a requested device is not found in the system.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceNotFoundException extends Exception {

    /**
     * Constructs a DeviceNotFoundException when a requested device is not found in the system.
     *
     * @param exception the error message
     */
    public DeviceNotFoundException(String exception) {
        super(exception);
    }
}
