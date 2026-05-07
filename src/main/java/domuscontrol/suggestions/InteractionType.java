package domuscontrol.suggestions;
 
/**
 * Represents the type of manual interaction a user performed on a device.
 * Each type corresponds to a specific action that can be performed and tracked for automation suggestion.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
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
 
