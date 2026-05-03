package domuscontrol.routines.conditions;

/**
 * Enumeration of comparison operators used in routine conditions.
 * These operators allow for flexible evaluation of device states and levels.
 */
public enum Operator {
    /**
     * Represents a strict equality comparison.
     */
    EQUALS,

    /**
     * Represents a comparison where the current value must be strictly higher than the trigger.
     */
    GREATER_THAN,

    /**
     * Represents a comparison where the current value must be strictly lower than the trigger.
     */
    LESS_THAN
}