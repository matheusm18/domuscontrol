package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a fan device.
 * The adjustable level represents fan speed from 0% to 100%.
 */
public class Fan extends AdjustableDevice {

    public Fan() {
        super();
    }

    public Fan(String brand, String model, double consumptionPerHour, int speed) {
        super(brand, model, consumptionPerHour, speed);
    }

    public Fan(Fan fan) {
        super(fan);
    }

    public int getSpeed() {
        return this.getLevel();
    }

    public void setSpeed(int speed) {
        this.setLevel(speed);
    }

    /**
     * Sets fan speed and keeps the fan's ON/OFF state consistent.
     * Speed greater than 0 turns the fan on; speed 0 turns it off.
     *
     * @param level The new fan speed (0-100).
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

        Fan fan = (Fan) o;
        return super.equals(fan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public Fan clone() {
        return new Fan(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
