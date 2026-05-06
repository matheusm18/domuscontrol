package domuscontrol.routines.conditions;

import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.Simulation;

import java.util.Objects;

/**
 * Condition that compares the current simulation luminosity with a trigger value.
 */
public class LuminosityCondition implements Condition {

    private int triggerLuminosity;
    private Operator operator;

    /**
     * Creates a condition with trigger luminosity 500 and EQUALS operator.
     */
    public LuminosityCondition() {
        this.triggerLuminosity = 500;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition with the given trigger luminosity and operator.
     *
     * @param triggerLuminosity the trigger luminosity
     * @param operator the comparison operator
     */
    public LuminosityCondition(int triggerLuminosity, Operator operator) {
        this.triggerLuminosity = triggerLuminosity;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Creates a copy of another luminosity condition.
     *
     * @param other the condition to copy
     */
    public LuminosityCondition(LuminosityCondition other) {
        this.triggerLuminosity = other.getTriggerLuminosity();
        this.operator = other.getOperator();
    }

    /**
     * Gets the trigger luminosity.
     *
     * @return the trigger luminosity
     */
    public int getTriggerLuminosity() {
        return this.triggerLuminosity;
    }

    /**
     * Sets the trigger luminosity.
     *
     * @param triggerLuminosity the trigger luminosity
     */
    public void setTriggerLuminosity(int triggerLuminosity) {
        this.triggerLuminosity = triggerLuminosity;
    }

    /**
     * Gets the comparison operator.
     *
     * @return the comparison operator
     */
    public Operator getOperator() {
        return this.operator;
    }

    /**
     * Sets the comparison operator.
     *
     * @param operator the comparison operator
     */
    public void setOperator(Operator operator) {
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Evaluates this condition against the current simulation luminosity.
     *
     * @param house the house context
     * @param simulation the current simulation state
     * @return true if the current luminosity satisfies the operator comparison; false otherwise
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        double currentLuminosity = simulation.getLuminosity();
        switch (this.operator) {
            case EQUALS:       return Math.round(currentLuminosity) == this.triggerLuminosity;
            case GREATER_THAN: return currentLuminosity > this.triggerLuminosity;
            case LESS_THAN:    return currentLuminosity < this.triggerLuminosity;
            default:           return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied LuminosityCondition instance
     */
    @Override
    public Condition copy() {
        return new LuminosityCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if trigger luminosity and operator match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LuminosityCondition condition = (LuminosityCondition) o;
        return this.triggerLuminosity == condition.getTriggerLuminosity() &&
               this.operator == condition.getOperator();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.triggerLuminosity, this.operator);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied LuminosityCondition instance
     */
    @Override
    public LuminosityCondition clone() {
        return new LuminosityCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LuminosityCondition { ")
          .append("Luminosity ").append(this.operator).append(" ").append(this.triggerLuminosity).append(" lx")
          .append(" }");
        return sb.toString();
    }
}
