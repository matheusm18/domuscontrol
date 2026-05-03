package domuscontrol.model.device.types;

import java.util.Objects;
//import domuscontrol.model.device.DeviceStatus;

/**
 * Represents switchable devices that also have an adjustable level (e.g., volume, brightness).
 */
public abstract class AdjustableDevice extends SwitchableDevice {

    private int level;

    public AdjustableDevice() {
        super();
        this.level = 0;
    }

    public AdjustableDevice(String brand, String model, double consumptionPerHour, int level) {
        super(brand, model, consumptionPerHour);
        this.setLevel(level);
    }

    public AdjustableDevice(AdjustableDevice d) {
        super(d);
        this.level = d.getLevel();
    }

    public int getLevel() {
        return this.level;
    }

    /**
     * Sets the level and ensures physical status consistency (Auto-ON / Auto-OFF).
     * @param level The new level (0-100).
     */
    public void setLevel(int level) {
        if (level < 0) {
            this.level = 0;
        } else if (level > 100) {
            this.level = 100;
        } else {
            this.level = level;
        }

        if (this.level > 0 && !this.isOn()) {
            this.turnOn();
        } else if (this.level == 0 && this.isOn()) {
            this.turnOff();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        AdjustableDevice d = (AdjustableDevice) o;
        return super.equals(d) && this.level == d.getLevel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Level: ").append(this.level).append("%\n");
        return sb.toString();
    }
}