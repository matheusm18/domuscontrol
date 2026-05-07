package domuscontrol.devices.types;

import java.util.Objects;

/**
 * Represents switchable devices that also have an adjustable level (e.g., volume, brightness).
 * These devices extend SwitchableDevice with the ability to control power levels from 0% to 100%.
 * Subclasses can define how level changes affect the ON/OFF state (e.g., turning off at 0%).
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public abstract class AdjustableDevice extends SwitchableDevice {

    /**
     * The adjustable level (0-100).
     */
    private int level;

    /**
     * Creates a new adjustable device with default values.
     * The device is initialized as OFF with level 0.
     */
    public AdjustableDevice() {
        super();
        this.level = 0;
    }

    /**
     * Creates an adjustable device with specified brand, model, consumption, and level.
     *
     * @param brand the brand of the device.
     * @param model the model of the device.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param level the initial adjustable level (0-100%).
     */
    public AdjustableDevice(String brand, String model, double consumptionPerHour, int level) {
        super(brand, model, consumptionPerHour);
        this.setLevel(level);
    }

    /**
     * Copy constructor for the adjustable device.
     *
     * @param d the adjustable device to copy.
     */
    public AdjustableDevice(AdjustableDevice d) {
        super(d);
        this.level = d.getLevel();
    }

    /**
     * Gets the current adjustable level.
     *
     * @return the current level (0-100%).
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Sets the adjustable level, clamping it to the 0-100 range.
     * Subclasses decide whether level changes affect their physical ON/OFF state.
     *
     * @param level the new level (0-100).
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

    /**
     * Checks if this adjustable device is equal to another object.
     *
     * @param o the object to compare with this device.
     * @return true if the given object is an adjustable device with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        AdjustableDevice d = (AdjustableDevice) o;
        return super.equals(d) && this.level == d.getLevel();
    }

    /**
     * Calculates the hash code of this adjustable device.
     *
     * @return the hash code of this device.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.level);
    }

    /**
     * Creates a string representation of this adjustable device.
     *
     * @return a string representation including the level percentage.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Level: ").append(this.level).append("%\n");
        return sb.toString();
    }
}
