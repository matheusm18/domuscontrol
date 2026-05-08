package domuscontrol.simulation;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object representing a routine activation during a simulation tick.
 * This class is immutable to ensure the event record remains consistent.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public final class ActivationEvent implements Serializable {

    /** The identifier of the house where the activation occurred. */
    private final int houseId;
    /** The name of the house where the activation occurred. */
    private final String houseName;
    /** The name of the activated routine. */
    private final String routineName;

    /**
     * Constructs an ActivationEvent with default values.
     */
    public ActivationEvent() {
        this.houseId = -1;
        this.houseName = "Unknown House";
        this.routineName = "Unknown Routine";
    }

    /**
     * Constructs an ActivationEvent with the house and routine information.
     *
     * @param houseId the identifier of the house where the activation occurred
     * @param houseName the name of the house where the activation occurred
     * @param routineName the name of the activated routine
     */
    public ActivationEvent(int houseId, String houseName, String routineName) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.routineName = routineName;
    }

    /**
     * Constructs an ActivationEvent by copying another ActivationEvent.
     *
     * @param other the activation event to copy
     */
    public ActivationEvent(ActivationEvent other) {
        this.houseId = other.getHouseId();
        this.houseName = other.getHouseName();
        this.routineName = other.getRoutineName();
    }

    /**
     * Returns the house identifier.
     *
     * @return the house identifier
     */
    public int getHouseId() {
        return this.houseId;
    }

    /**
     * Returns the house name.
     *
     * @return the house name
     */
    public String getHouseName() {
        return this.houseName;
    }

    /**
     * Returns the activated routine name.
     *
     * @return the routine name
     */
    public String getRoutineName() {
        return this.routineName;
    }

    /**
     * Creates a copy of this activation event.
     *
     * @return a new ActivationEvent with the same values
     */
    @Override
    public ActivationEvent clone() {
        return new ActivationEvent(this);
    }

    /**
     * Compares this activation event with another object for equality.
     *
     * @param o the object to compare with
     * @return true if all fields match, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivationEvent event = (ActivationEvent) o;
        return this.houseId == event.getHouseId()
            && Objects.equals(this.houseName, event.getHouseName())
            && Objects.equals(this.routineName, event.getRoutineName());
    }

    /**
     * Generates a hash code for this activation event.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.houseId, this.houseName, this.routineName);
    }

    /**
     * Returns a string representation of this activation event.
     *
     * @return a formatted string with the event information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ActivationEvent { ")
          .append("House ID: ").append(this.houseId)
          .append(", House Name: '").append(this.houseName).append("'")
          .append(", Routine: '").append(this.routineName).append("'")
          .append(" }");
        return sb.toString();
    }
}