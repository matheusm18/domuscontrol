package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a smart air conditioner device with adjustable cooling power.
 * Cooling power ranges from 0% (off) to 100% (maximum cooling).
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class AirConditioner extends AdjustableDevice {

    /**
     * Creates a new air conditioner with default values.
     * The device is initialized as OFF with 0% cooling power.
     */
    public AirConditioner() {
        super();
    }

    /**
     * Creates an air conditioner with specified brand, model, power consumption, and cooling power.
     *
     * @param brand the brand of the air conditioner.
     * @param model the model of the air conditioner.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param coolingPower the initial cooling power level (0-100%).
     */
    public AirConditioner(String brand, String model, double consumptionPerHour, int coolingPower) {
        super(brand, model, consumptionPerHour, coolingPower);
    }

    /**
     * Copy constructor for the air conditioner.
     *
     * @param airConditioner the air conditioner to copy.
     */
    public AirConditioner(AirConditioner airConditioner) {
        super(airConditioner);
    }

    /**
     * Gets the current cooling power level.
     *
     * @return the cooling power level (0-100%).
     */
    public int getCoolingPower() {
        return this.getLevel();
    }

    /**
     * Sets the cooling power level.
     *
     * @param coolingPower the new cooling power level (0-100%).
     */
    public void setCoolingPower(int coolingPower) {
        this.setLevel(coolingPower);
    }

    /**
     * Sets cooling power and keeps the air conditioner's ON/OFF state consistent.
     * Cooling power greater than 0 turns it on; cooling power 0 turns it off.
     *
     * @param level the new cooling power (0-100).
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
     * Checks if this air conditioner is equal to another object.
     *
     * @param o the object to compare with this air conditioner.
     * @return true if the given object is an air conditioner with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        AirConditioner airConditioner = (AirConditioner) o;
        return super.equals(airConditioner);
    }

    /**
     * Calculates the hash code of this air conditioner.
     *
     * @return the hash code of this air conditioner.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a deep copy of this air conditioner.
     *
     * @return a new air conditioner instance with the same properties.
     */
    @Override
    public AirConditioner clone() {
        return new AirConditioner(this);
    }

    /**
     * Creates a string representation of this air conditioner.
     *
     * @return a string representation of this air conditioner.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
