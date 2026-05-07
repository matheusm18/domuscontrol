package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a smart fan device with adjustable speed control.
 * The fan speed ranges from 0% (off) to 100% (maximum speed).
 * Key capabilities:
 * - Adjustable speed levels (0-100%)
 * - Automatic ON/OFF state management (turns off at 0% speed)
 * - Speed-dependent energy consumption
 * - Suitable for air circulation in residential and commercial spaces
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Fan extends AdjustableDevice {

    /**
     * Creates a new fan with default values.
     * The device is initialized as OFF with 0% speed.
     */
    public Fan() {
        super();
    }

    /**
     * Creates a fan with specified brand, model, power consumption, and speed.
     *
     * @param brand the brand of the fan.
     * @param model the model of the fan.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param speed the initial fan speed (0-100%).
     */
    public Fan(String brand, String model, double consumptionPerHour, int speed) {
        super(brand, model, consumptionPerHour, speed);
    }

    /**
     * Copy constructor for the fan.
     *
     * @param fan the fan to copy.
     */
    public Fan(Fan fan) {
        super(fan);
    }

    /**
     * Gets the current fan speed.
     *
     * @return the fan speed (0-100%).
     */
    public int getSpeed() {
        return this.getLevel();
    }

    /**
     * Sets the fan speed.
     *
     * @param speed the new fan speed (0-100%).
     */
    public void setSpeed(int speed) {
        this.setLevel(speed);
    }

    /**
     * Sets fan speed and keeps the fan's ON/OFF state consistent.
     * Speed greater than 0 turns the fan on; speed 0 turns it off.
     *
     * @param level the new fan speed (0-100).
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
     * Checks if this fan is equal to another object.
     *
     * @param o the object to compare with this fan.
     * @return true if the given object is a fan with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Fan fan = (Fan) o;
        return super.equals(fan);
    }

    /**
     * Calculates the hash code of this fan.
     *
     * @return the hash code of this fan.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a deep copy of this fan.
     *
     * @return a new fan instance with the same properties.
     */
    @Override
    public Fan clone() {
        return new Fan(this);
    }

    /**
     * Creates a string representation of this fan.
     *
     * @return a string representation of this fan.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
