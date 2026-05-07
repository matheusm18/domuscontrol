package domuscontrol.exceptions;

/**
 * Exception thrown when a device is not an instance of AdjustableDevice.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceIsNotInstanceOfAdjustableDeviceException extends Exception {

    /**
     * Constructs a DeviceIsNotInstanceOfAdjustableDeviceException when a device is not an instance of AdjustableDevice.
     *
     * @param message the error message
     */
    public DeviceIsNotInstanceOfAdjustableDeviceException(String message) {
        super(message);
    }
}
