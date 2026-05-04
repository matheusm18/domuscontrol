package domuscontrol.routines.conditions;

import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.Simulation;

import java.util.Objects;

/**
 * Condition that evaluates whether current outside luminosity satisfies a comparison.
 */
public class LuminosityCondition implements Condition {

    private int triggerLuminosity;
    private Operator operator;

    public LuminosityCondition() {
        this.triggerLuminosity = 500;
        this.operator = Operator.EQUALS;
    }

    public LuminosityCondition(int triggerLuminosity, Operator operator) {
        this.triggerLuminosity = triggerLuminosity;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    public LuminosityCondition(LuminosityCondition other) {
        this.triggerLuminosity = other.triggerLuminosity;
        this.operator = other.operator;
    }

    public int getTriggerLuminosity() { return triggerLuminosity; }

    public void setTriggerLuminosity(int triggerLuminosity) { this.triggerLuminosity = triggerLuminosity; }

    public Operator getOperator() { return operator; }

    public void setOperator(Operator operator) { this.operator = operator != null ? operator : Operator.EQUALS; }

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

    @Override
    public Condition copy() {
        return new LuminosityCondition(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LuminosityCondition that = (LuminosityCondition) o;
        return this.triggerLuminosity == that.triggerLuminosity &&
               this.operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.triggerLuminosity, this.operator);
    }

    @Override
    public LuminosityCondition clone() {
        return new LuminosityCondition(this);
    }

    @Override
    public String toString() {
        return "LuminosityCondition { Luminosity " + this.operator + " " + this.triggerLuminosity + " lx }";
    }
}
