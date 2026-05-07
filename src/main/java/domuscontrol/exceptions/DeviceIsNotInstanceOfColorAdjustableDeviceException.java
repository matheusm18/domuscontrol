package domuscontrol.exceptions;

/**
 * Exception thrown when a device is not an instance of ColorAdjustableDevice.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceIsNotInstanceOfColorAdjustableDeviceException extends Exception {

    /**
     * Constructs a DeviceIsNotInstanceOfColorAdjustableDeviceException when a device is not an instance of ColorAdjustableDevice.
     *
     * @param message the error message
     */
    public DeviceIsNotInstanceOfColorAdjustableDeviceException(String message) {
        super(message);
    }
}
