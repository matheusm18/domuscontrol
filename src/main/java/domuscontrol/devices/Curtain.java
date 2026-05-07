package domuscontrol.devices;

import java.util.Objects;

import domuscontrol.devices.types.OpenableDevice;

/**
 * Represents a smart curtain device with automated opening and closing control.
 * The curtain can be adjusted to any opening level from 0% (fully closed) to 100% (fully open).
 * Key capabilities:
 * - Adjustable opening levels (0-100%)
 * - Automatic OPEN/CLOSED state management
 * - Convenience methods to fully open or close the curtain
 * - Energy consumption tracking based on opening level
 * - Useful for light control and privacy management
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Curtain extends OpenableDevice {

    /**
     * Creates a new curtain with default values.
     * The curtain is initialized as fully closed (0% opening).
     */
    public Curtain() {
        super();
    }

    /**
     * Creates a curtain with specified brand, model, power consumption, and opening level.
     *
     * @param brand the brand of the curtain.
     * @param model the model of the curtain.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param openingLevel the initial opening percentage (0-100%).
     */
    public Curtain(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour, openingLevel);
    }

    /**
     * Copy constructor for the curtain.
     *
     * @param curtain the curtain to copy.
     */
    public Curtain(Curtain curtain) {
        super(curtain);
    }

    /**
     * Opens the curtain fully by setting the opening level to 100%.
     */
    public void openFully() {
        this.setOpening(100);
    }

    /**
     * Closes the curtain fully by setting the opening level to 0%.
     */
    public void closeFully() {
        this.setOpening(0);
    }

    /**
     * Checks if this curtain is equal to another object.
     *
     * @param o the object to compare with this curtain.
     * @return true if the given object is a curtain with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Curtain curtain = (Curtain) o;
        return super.equals(curtain);
    }

    /**
     * Calculates the hash code of this curtain.
     *
     * @return the hash code of this curtain.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a deep copy of this curtain.
     *
     * @return a new curtain instance with the same properties.
     */
    @Override
    public Curtain clone() {
        return new Curtain(this);
    }

    /**
     * Creates a string representation of this curtain.
     *
     * @return a string representation of this curtain.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
