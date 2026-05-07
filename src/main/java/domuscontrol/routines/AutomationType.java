package domuscontrol.routines;

/**
 * Defines the classification of an automation routine.
 * This determines the execution logic and the type of conditions allowed for the routine.
 * AUTOMATION: Standard automation triggered by device state or environmental conditions.
 * SCHEDULE: Time-based routine triggered at specific moments or within time windows.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public enum AutomationType {
    /**
     * Represents a standard automation triggered by device state changes or environmental conditions.
     */
    AUTOMATION,

    /**
     * Represents a time-based routine that triggers at specific moments or within specific time windows.
     */
    SCHEDULE
}