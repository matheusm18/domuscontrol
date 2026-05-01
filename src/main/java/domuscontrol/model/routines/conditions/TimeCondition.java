package domuscontrol.model.routines.conditions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import domuscontrol.DomusControl;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.routines.TimeBasedCondition;

/**
 * Condition that evaluates whether the current simulation time has reached or passed 
 * a specific trigger time. It is designed to handle time skips and day-cycle transitions.
 */
public class TimeCondition implements TimeBasedCondition {
    private LocalTime triggerTime;

    /**
     * Default constructor initializing the trigger time to midnight.
     */
    public TimeCondition() { 
        this.triggerTime = LocalTime.MIDNIGHT; 
    }

    /**
     * Parameterized constructor.
     * * @param triggerTime The specific LocalTime that should trigger the condition.
     */
    public TimeCondition(LocalTime triggerTime) { 
        this.triggerTime = triggerTime; 
    }

    /**
     * Copy constructor for deep copying.
     * * @param other The existing TimeCondition instance to copy.
     */
    public TimeCondition(TimeCondition other) { 
        this.triggerTime = other.getTriggerTime(); 
    }

    /**
     * Retrieves the configured trigger time.
     * * @return The LocalTime set as the trigger.
     */
    public LocalTime getTriggerTime() { 
        return triggerTime; 
    }
    
    /**
     * Sets a new trigger time.
     * * @param triggerTime The new LocalTime to be used as a trigger.
     */
    public void setTriggerTime(LocalTime triggerTime) { 
        this.triggerTime = triggerTime; 
    }

    /**
     * Evaluates the condition by comparing the current house time and previous house time 
     * against the trigger time. This ensures triggers are not missed if the simulation 
     * ticks skip over the exact second.
     * @return true if the trigger time was reached or crossed since the last tick; false otherwise.
     */
    @Override
    public boolean evaluate() {
        LocalDateTime now = DomusControl.getCurrentDateTime();
        LocalDateTime before = DomusControl.getLastTickDateTime();

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
     * Creates a deep copy of this condition.
     * * @return A new instance of TimeCondition with the same trigger time.
     */
    @Override
    public Condition copy() {
        return new TimeCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     * * @param o The object to compare with.
     * @return true if the trigger times are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TimeCondition that = (TimeCondition) o;
        
        return Objects.equals(this.triggerTime, that.triggerTime);
    }

    /**
     * Generates a hash code for this condition.
     * * @return The hash code based on the trigger time.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.triggerTime);
    }

    /**
     * Clones the current condition instance.
     * * @return A cloned TimeCondition.
     */
    @Override
    public TimeCondition clone() {
        return new TimeCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     * * @return Formatted string containing the trigger time.
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