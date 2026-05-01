package domuscontrol.model.suggestions;
 
/**
 * Represents the type of manual interaction a user performed on a device.
 */
public enum InteractionType {
 
    /** The device was turned on. */
    TURN_ON,
 
    /** The device was turned off. */
    TURN_OFF,
 
    /** The adjustable level of the device was changed (e.g. brightness). */
    SET_LEVEL,
 
    /** The opening degree of the device was changed (e.g. curtains, garage gate). */
    SET_OPENING,
 
    /** The color temperature of a lamp was changed. */
    SET_COLOR_TEMPERATURE
}
 
