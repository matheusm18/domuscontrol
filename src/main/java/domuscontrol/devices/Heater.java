package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a smart heater device with adjustable heating power.
 * The heating power ranges from 0% (off) to 100% (maximum heating).
 * Key capabilities:
 * - Adjustable heating power levels (0-100%)
 * - Automatic ON/OFF state management (turns off at 0% heating power)
 * - Power-dependent energy consumption
 * - Ideal for temperature control and space heating in residential environments
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Heater extends AdjustableDevice {

    /**
     * Creates a new heater with default values.
     * The device is initialized as OFF with 0% heating power.
     */
    public Heater() {
        super();
    }

    /**
     * Creates a heater with specified brand, model, power consumption, and heating power.
     *
     * @param brand the brand of the heater.
     * @param model the model of the heater.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param power the initial heating power (0-100%).
     */
    public Heater(String brand, String model, double consumptionPerHour, int power) {
        super(brand, model, consumptionPerHour, power);
    }

    /**
     * Copy constructor for the heater.
     *
     * @param heater the heater to copy.
     */
    public Heater(Heater heater) {
        super(heater);
    }

    /**
     * Gets the current heating power level.
     *
     * @return the heating power level (0-100%).
     */
    public int getPower() {
        return this.getLevel();
    }

    /**
     * Sets the heating power level.
     *
     * @param power the new heating power level (0-100%).
     */
    public void setPower(int power) {
        this.setLevel(power);
    }

    /**
     * Sets heating power and keeps the heater's ON/OFF state consistent.
     * Power greater than 0 turns the heater on; power 0 turns it off.
     *
     * @param level the new heating power (0-100).
     */
    @Override
    public void setLevel(int level) {
        super.setLevel(level);

        if (this.getLevel() > 0 && !this.isOn()) {
            this.turnOn();
        } else if (this.getLevel() == 0 && this.isOn()) {
            this.turnOff();
        }
    }

    /**
     * Checks if this heater is equal to another object.
     *
     * @param o the object to compare with this heater.
     * @return true if the given object is a heater with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Heater heater = (Heater) o;
        return super.equals(heater);
    }

    /**
     * Calculates the hash code of this heater.
     *
     * @return the hash code of this heater.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a deep copy of this heater.
     *
     * @return a new heater instance with the same properties.
     */
    @Override
    public Heater clone() {
        return new Heater(this);
    }

    /**
     * Creates a string representation of this heater.
     *
     * @return a string representation of this heater.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
