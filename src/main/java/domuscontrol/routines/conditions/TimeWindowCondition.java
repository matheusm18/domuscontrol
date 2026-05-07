package domuscontrol.routines.conditions;

import java.time.LocalTime;
import java.util.Objects;

import domuscontrol.simulation.SimulationState;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Time-based condition that checks whether the current simulation time is inside a time window.
 * Windows may also cross midnight. When the start time is after the end time, the window is
 * considered to wrap around midnight (e.g., 22:00 to 04:00).
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class TimeWindowCondition implements TimeBasedCondition {
    /** The start time of the time window. */
    private LocalTime startTime;
    /** The end time of the time window. */
    private LocalTime endTime;

    /**
     * Creates a one-minute window starting at midnight.
     */
    public TimeWindowCondition() {
        this.startTime = LocalTime.MIDNIGHT;
        this.endTime = LocalTime.MIDNIGHT.plusMinutes(1);
    }

    /**
     * Creates a condition with the given time window.
     *
     * @param startTime the beginning of the time window
     * @param endTime the end of the time window
     */
    public TimeWindowCondition(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Creates a copy of another time-window condition.
     *
     * @param other the condition to copy
     */
    public TimeWindowCondition(TimeWindowCondition other) {
        this.startTime = other.getStartTime();
        this.endTime = other.getEndTime();
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Evaluates whether the current simulation time is inside the configured window.
     *
     * @param house the house context
     * @param state the current simulation state
     * @return true if current time is within the window (inclusive); false otherwise.
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        if (state.getCurrentDateTime() == null || this.startTime == null || this.endTime == null) {
            return false;
        }

        LocalTime current = state.getCurrentDateTime().toLocalTime();

        if (this.startTime.isBefore(this.endTime)) {
            // Standard window:  10:00 to 18:00
            return !current.isBefore(this.startTime) && !current.isAfter(this.endTime);
        } else {
            // Crossover window: 22:00 to 04:00
            return !current.isBefore(this.startTime) || !current.isAfter(this.endTime);
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TimeWindowCondition instance
     */
    @Override
    public Condition copy() {
        return new TimeWindowCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if both start and end times match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TimeWindowCondition condition = (TimeWindowCondition) o;
        
        return Objects.equals(this.startTime, condition.getStartTime()) &&
               Objects.equals(this.endTime, condition.getEndTime());
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.startTime, this.endTime);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied TimeWindowCondition instance
     */
    @Override
    public TimeWindowCondition clone() {
        return new TimeWindowCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
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
