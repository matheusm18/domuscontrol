package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a heater device.
 * The adjustable level represents heating power from 0% to 100%.
 */
public class Heater extends AdjustableDevice {

    public Heater() {
        super();
    }

    public Heater(String brand, String model, double consumptionPerHour, int power) {
        super(brand, model, consumptionPerHour, power);
    }

    public Heater(Heater heater) {
        super(heater);
    }

    public int getPower() {
        return this.getLevel();
    }

    public void setPower(int power) {
        this.setLevel(power);
    }

    /**
     * Sets heating power and keeps the heater's ON/OFF state consistent.
     * Power greater than 0 turns the heater on; power 0 turns it off.
     *
     * @param level The new heating power (0-100).
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Heater heater = (Heater) o;
        return super.equals(heater);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public Heater clone() {
        return new Heater(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
