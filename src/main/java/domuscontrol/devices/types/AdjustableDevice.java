package domuscontrol.devices.types;

import java.util.Objects;

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
     * Sets the adjustable level, clamping it to the 0-100 range.
     * Subclasses decide whether level changes affect their physical ON/OFF state.
     *
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
