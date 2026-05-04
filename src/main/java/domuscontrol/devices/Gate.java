package domuscontrol.devices;

import java.util.Objects;

import domuscontrol.devices.types.OpenableDevice;

/**
 * Represents a smart gate device in the home automation system.
 */
public class Gate extends OpenableDevice {

    /**
     * Default constructor for the Gate class.
     * Initializes a new gate in a fully closed state.
     */
    public Gate() {
        super(); 
    }

    /**
     * Parameterized constructor.
     *
     * @param brand              The brand of the gate.
     * @param model              The model of the gate.
     * @param consumptionPerHour The power consumption of the gate in Wh/h.
     * @param openingLevel       The initial opening percentage (0-100).
     */
    public Gate(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour, openingLevel); 
    }

    /**
     * Copy constructor.
     *
     * @param gate The Gate object to copy.
     */
    public Gate(Gate gate) {
        super(gate); 
    }

    /**
     * Opens the gate fully (sets opening to 100%).
     */
    public void openFully() {
        this.setOpening(100);
    }

    /**
     * Closes the gate fully (sets opening to 0%).
     */
    public void closeFully() {
        this.setOpening(0);
    }

    /**
     * Checks if this gate is equal to another object.
     *
     * @param o The object to compare with this gate.
     * @return true if the given object is a gate with the same properties as this gate, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Gate gate = (Gate) o;
        return super.equals(gate);
    }

    /**
     * Calculates the hash code of this gate.
     *
     * @return The hash code of this gate.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a copy of this gate.
     *
     * @return A copy of this gate.
     */
    @Override
    public Gate clone() {
        return new Gate(this);
    }

    /**
     * Creates a string representation of this gate.
     *
     * @return A string representing this gate.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
