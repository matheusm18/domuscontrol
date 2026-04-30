package domuscontrol.model.routines;

/**
 * Defines the classification of an automation routine.
 * This determines the execution logic and the type of conditions allowed for the routine.
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