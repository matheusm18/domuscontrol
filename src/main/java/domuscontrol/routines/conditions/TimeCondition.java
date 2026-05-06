package domuscontrol.routines.conditions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import domuscontrol.simulation.Simulation;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Time-based condition that checks whether a specific time was reached since the last tick.
 */
public class TimeCondition implements TimeBasedCondition {
    private LocalTime triggerTime;

    /**
     * Creates a condition that triggers at midnight.
     */
    public TimeCondition() {
        this.triggerTime = LocalTime.MIDNIGHT;
    }

    /**
     * Creates a condition with the given trigger time.
     *
     * @param triggerTime the time that should trigger this condition
     */
    public TimeCondition(LocalTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    /**
     * Creates a copy of another time condition.
     *
     * @param other the condition to copy
     */
    public TimeCondition(TimeCondition other) {
        this.triggerTime = other.getTriggerTime();
    }

    /**
     * Gets the trigger time.
     *
     * @return the trigger time
     */
    public LocalTime getTriggerTime() {
        return this.triggerTime;
    }
    
    /**
     * Sets the trigger time.
     *
     * @param triggerTime the trigger time
     */
    public void setTriggerTime(LocalTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    /**
     * Evaluates whether the trigger time was reached between the previous and current simulation times.
     *
     * @param house the house context
     * @param simulation the current simulation state
     * @return true if the trigger time was reached or crossed since the last tick; false otherwise.
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        LocalDateTime now = simulation.getCurrentDateTime();
        LocalDateTime before = simulation.getPreviousDateTime();

        if (now == null || before == null || this.triggerTime == null) {
            return false;
        }

        if (!now.isAfter(before)) {
            return now.toLocalTime().equals(this.triggerTime);
        }

        LocalDateTime nextTrigger = before.toLocalDate().atTime(this.triggerTime);

        if (!nextTrigger.isAfter(before)) {
            nextTrigger = nextTrigger.plusDays(1);
        }

        return !nextTrigger.isAfter(now);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TimeCondition instance
     */
    @Override
    public Condition copy() {
        return new TimeCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the trigger times are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TimeCondition condition = (TimeCondition) o;
        
        return Objects.equals(this.triggerTime, condition.getTriggerTime());
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.triggerTime);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TimeCondition instance
     */
    @Override
    public TimeCondition clone() {
        return new TimeCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TimeCondition { ")
          .append("Trigger Time: ").append(this.triggerTime)
          .append(" }");
        return sb.toString();
    }
}
