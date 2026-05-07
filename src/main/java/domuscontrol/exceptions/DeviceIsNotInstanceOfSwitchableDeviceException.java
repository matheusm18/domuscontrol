package domuscontrol.exceptions;

/**
 * Exception thrown when a device is not an instance of SwitchableDevice.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceIsNotInstanceOfSwitchableDeviceException extends Exception {

    /**
     * Constructs a DeviceIsNotInstanceOfSwitchableDeviceException when a device is not an instance of SwitchableDevice.
     *
     * @param message the error message
     */
    public DeviceIsNotInstanceOfSwitchableDeviceException(String message) {
        super(message);
    }
}
