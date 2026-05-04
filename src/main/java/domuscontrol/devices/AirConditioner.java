package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents an air conditioner device.
 * The adjustable level represents cooling power from 0% to 100%.
 */
public class AirConditioner extends AdjustableDevice {

    public AirConditioner() {
        super();
    }

    public AirConditioner(String brand, String model, double consumptionPerHour, int coolingPower) {
        super(brand, model, consumptionPerHour, coolingPower);
    }

    public AirConditioner(AirConditioner airConditioner) {
        super(airConditioner);
    }

    public int getCoolingPower() {
        return this.getLevel();
    }

    public void setCoolingPower(int coolingPower) {
        this.setLevel(coolingPower);
    }

    /**
     * Sets cooling power and keeps the air conditioner's ON/OFF state consistent.
     * Cooling power greater than 0 turns it on; cooling power 0 turns it off.
     *
     * @param level The new cooling power (0-100).
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

        AirConditioner airConditioner = (AirConditioner) o;
        return super.equals(airConditioner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public AirConditioner clone() {
        return new AirConditioner(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
