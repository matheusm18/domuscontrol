package domuscontrol.routines.conditions;

/**
 * Enumeration of comparison operators used in routine conditions.
 * These operators allow for flexible evaluation of device states, sensor readings, and other numeric values.
 * Each operator defines a specific comparison method for evaluating condition thresholds.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public enum Operator {
    /**
     * Represents a strict equality comparison.
     * The current value must equal the trigger value exactly (or within tolerance for floating-point values).
     */
    EQUALS,

    /**
     * Represents a comparison where the current value must be strictly higher than the trigger value.
     */
    GREATER_THAN,

    /**
     * Represents a comparison where the current value must be strictly lower than the trigger value.
     */
    LESS_THAN
}