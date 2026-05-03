package domuscontrol.routines.conditions;

import domuscontrol.simulation.Simulation;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

import java.util.Objects;

/**
 * Condition that evaluates whether the current environmental temperature
 * satisfies a specific comparison against a trigger value.
 */
public class TemperatureCondition implements Condition {

    private int triggerTemperature;
    private Operator operator;

    public TemperatureCondition() {
        this.triggerTemperature = 20;
        this.operator = Operator.EQUALS;
    }

    public TemperatureCondition(int triggerTemperature, Operator operator) {
        this.triggerTemperature = triggerTemperature;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    public TemperatureCondition(TemperatureCondition other) {
        this.triggerTemperature = other.triggerTemperature;
        this.operator = other.operator;
    }

    public int getTriggerTemperature() { return triggerTemperature; }

    public void setTriggerTemperature(int triggerTemperature) { this.triggerTemperature = triggerTemperature; }

    public Operator getOperator() { return operator; }

    public void setOperator(Operator operator) { this.operator = operator != null ? operator : Operator.EQUALS; }

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

    @Override
    public Condition copy() {
        return new TemperatureCondition(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TemperatureCondition that = (TemperatureCondition) o;
        return this.triggerTemperature == that.triggerTemperature &&
               this.operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.triggerTemperature, this.operator);
    }

    @Override
    public TemperatureCondition clone() {
        return new TemperatureCondition(this);
    }

    @Override
    public String toString() {
        return "TemperatureCondition { Temperature " + this.operator + " " + this.triggerTemperature + "ºC }";
    }
}
