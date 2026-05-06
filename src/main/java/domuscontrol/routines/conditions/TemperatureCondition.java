package domuscontrol.routines.conditions;

import domuscontrol.simulation.Simulation;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

import java.util.Objects;

/**
 * Condition that compares the current simulation temperature with a trigger value.
 */
public class TemperatureCondition implements Condition {

    private int triggerTemperature;
    private Operator operator;

    /**
     * Creates a condition with trigger temperature 20 and EQUALS operator.
     */
    public TemperatureCondition() {
        this.triggerTemperature = 20;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition with the given trigger temperature and operator.
     *
     * @param triggerTemperature the trigger temperature
     * @param operator the comparison operator
     */
    public TemperatureCondition(int triggerTemperature, Operator operator) {
        this.triggerTemperature = triggerTemperature;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Creates a copy of another temperature condition.
     *
     * @param other the condition to copy
     */
    public TemperatureCondition(TemperatureCondition other) {
        this.triggerTemperature = other.getTriggerTemperature();
        this.operator = other.getOperator();
    }

    /**
     * Gets the trigger temperature.
     *
     * @return the trigger temperature
     */
    public int getTriggerTemperature() {
        return this.triggerTemperature;
    }

    /**
     * Sets the trigger temperature.
     *
     * @param triggerTemperature the trigger temperature
     */
    public void setTriggerTemperature(int triggerTemperature) {
        this.triggerTemperature = triggerTemperature;
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
     * Evaluates this condition against the current simulation temperature.
     *
     * @param house the house context
     * @param simulation the current simulation state
     * @return true if the current temperature satisfies the operator comparison; false otherwise
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        double currentTemperature = simulation.getTemperature();
        switch (this.operator) {
            case EQUALS:       return Math.round(currentTemperature) == this.triggerTemperature;
            case GREATER_THAN: return currentTemperature > this.triggerTemperature;
            case LESS_THAN:    return currentTemperature < this.triggerTemperature;
            default:           return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TemperatureCondition instance
     */
    @Override
    public Condition copy() {
        return new TemperatureCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if trigger temperature and operator match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TemperatureCondition condition = (TemperatureCondition) o;
        return this.triggerTemperature == condition.getTriggerTemperature() &&
               this.operator == condition.getOperator();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.triggerTemperature, this.operator);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TemperatureCondition instance
     */
    @Override
    public TemperatureCondition clone() {
        return new TemperatureCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TemperatureCondition { ")
          .append("Temperature ").append(this.operator).append(" ").append(this.triggerTemperature).append("ºC")
          .append(" }");
        return sb.toString();
    }
}
