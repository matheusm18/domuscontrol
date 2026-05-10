package domuscontrol.devices;

/**
 * Enumeration representing the possible statuses of a device.
 * Represents the state of switchable and openable devices in the system.
 * 
 * @author Afonso Barros a112178
 * @author Martim Monteiro a111013
 * @author Matheus Azevedo a111430
 * @version 1.0
 */
public enum DeviceStatus {
    
    /** Device is turned on (for switchable devices). */
    ON,

    /** Device is turned off (for switchable devices). */
    OFF,

    /** Device is in open position (for openable devices). */
    OPEN,
    
    /** Device is in closed position (for openable devices). */
    CLOSED
}