package domuscontrol.model.routines.conditions;

import java.time.LocalTime;
import java.util.Objects;

import domuscontrol.DomusControl;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.routines.TimeBasedCondition;

/**
 * Condition that evaluates whether the current simulation time falls within a specific
 * time window. It supports both standard windows (e.g., 09:00 to 17:00) and windows
 * that cross over midnight (e.g., 22:00 to 06:00).
 */
public class TimeWindowCondition implements TimeBasedCondition {
    private LocalTime startTime;
    private LocalTime endTime;

    /**
     * Default constructor initializing a one-minute window starting at midnight.
     */
    public TimeWindowCondition() {
        this.startTime = LocalTime.MIDNIGHT;
        this.endTime = LocalTime.MIDNIGHT.plusMinutes(1);
    }

    /**
     * Parameterized constructor.
     * * @param startTime The beginning of the time window.
     * @param endTime   The end of the time window.
     */
    public TimeWindowCondition(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Copy constructor for deep copying.
     * * @param other The existing TimeWindowCondition instance to copy.
     */
    public TimeWindowCondition(TimeWindowCondition other) {
        this.startTime = other.getStartTime();
        this.endTime = other.getEndTime();
    }

    /**
     * Retrieves the configured start time.
     * * @return The start LocalTime.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets a new start time for the window.
     * * @param startTime The new beginning LocalTime.
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Retrieves the configured end time.
     * * @return The end LocalTime.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets a new end time for the window.
     * * @param endTime The new ending LocalTime.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Evaluates if the current house time is within the defined window.
     * If the start time is after the end time, the window is assumed to span across midnight.
     * * @param house The house context providing the current simulation time.
     * @return true if current time is within the window (inclusive); false otherwise.
     */
    @Override
    public boolean evaluate() {
        if (DomusControl.getCurrentTime() == null || this.startTime == null || this.endTime == null) {
            return false;
        }

        LocalTime current = DomusControl.getCurrentTime();

        if (this.startTime.isBefore(this.endTime)) {
            // Standard window:  10:00 to 18:00
            return !current.isBefore(this.startTime) && !current.isAfter(this.endTime);
        } else {
            // Crossover window: 22:00 to 04:00
            return !current.isBefore(this.startTime) || !current.isAfter(this.endTime);
        }
    }

    /**
     * Creates a deep copy of this condition.
     * * @return A new instance of TimeWindowCondition.
     */
    @Override
    public Condition copy() {
        return new TimeWindowCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     * * @param o The object to compare with.
     * @return true if both start and end times match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TimeWindowCondition that = (TimeWindowCondition) o;
        
        return Objects.equals(this.startTime, that.startTime) &&
               Objects.equals(this.endTime, that.endTime);
    }

    /**
     * Generates a hash code for this condition.
     * * @return The hash code based on start and end times.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.startTime, this.endTime);
    }

    /**
     * Clones the current condition instance.
     * * @return A cloned TimeWindowCondition.
     */
    @Override
    public TimeWindowCondition clone() {
        return new TimeWindowCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     * * @return Formatted string containing the start and end times.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TimeWindowCondition { ")
          .append("Start Time: ").append(this.startTime)
          .append(", End Time: ").append(this.endTime)
          .append(" }");
        return sb.toString();
    }
}