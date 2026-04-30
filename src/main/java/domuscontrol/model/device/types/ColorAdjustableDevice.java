package domuscontrol.model.device.types;

/**
 * Represents devices that support color temperature adjustment,
 * such as smart lamps with configurable light warmth.
 */
public interface ColorAdjustableDevice {

    /**
     * Sets the color temperature of the device.
     *
     * @param temperature The color temperature in Kelvin (typically 2700K to 4000K).
     */
    void setColorTemperature(int temperature);

    /**
     * Returns the current color temperature of the device.
     *
     * @return The current color temperature in Kelvin.
     */
    int getColorTemperature();

    /**
     * Returns the unique identifier of the device.
     * Required to support device cleanup in routines.
     *
     * @return The device ID.
     */
    int getId();
}