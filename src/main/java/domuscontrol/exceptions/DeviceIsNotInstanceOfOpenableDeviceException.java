package domuscontrol.exceptions;

/**
 * Exception thrown when a device is not an instance of OpenableDevice.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceIsNotInstanceOfOpenableDeviceException extends Exception {

    /**
     * Constructs a DeviceIsNotInstanceOfOpenableDeviceException when a device is not an instance of OpenableDevice.
     *
     * @param message the error message
     */
    public DeviceIsNotInstanceOfOpenableDeviceException(String message) {
        super(message);
    }
}
