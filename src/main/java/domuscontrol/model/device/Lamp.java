package domuscontrol.model.device;

import domuscontrol.model.device.types.AdjustableDevice;
import domuscontrol.model.device.types.ColorAdjustableDevice;

import java.util.Objects;

/**
 * Represents a smart lamp device.
 */
public class Lamp extends AdjustableDevice implements ColorAdjustableDevice {

    /** The color temperature of the lamp in Kelvin (2700K to 4000K). */
    private int colorTemperature;

    /** Default constructor for the Lamp class. */
    public Lamp() {
        super();
        this.colorTemperature = 2700;
    }

    /**
     * Constructor for the Lamp class with specified values.
     *
     * @param brand the brand of the lamp.
     * @param model the model of the lamp.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param brightness the initial brightness level (0-100).
     * @param colorTemperature the initial color temperature in Kelvin (2700-4000).
     */
    public Lamp(String brand, String model, double consumptionPerHour, int brightness, int colorTemperature) {
        super(brand, model, consumptionPerHour, brightness);
        this.setColorTemperature(colorTemperature);
    }

    /**
     * Copy constructor for the Lamp class.
     * 
     * @param lamp the lamp to copy.
     */
    public Lamp(Lamp lamp) {
        super(lamp);
        this.colorTemperature = lamp.getColorTemperature();
    }
    
    /**
     * Gets the current color temperature of the lamp.
     * 
     * @return This lamp's color temperature in Kelvin.
     */
    public int getColorTemperature() {
        return colorTemperature;
    }

    /**
     * Sets the color temperature of the lamp. 
     * The value is fixed between 2700K and 4000K as per typical scale.
     * 
     * @param colorTemperature The new color temperature in Kelvin.
     */
    public void setColorTemperature(int colorTemperature) {
        if (colorTemperature < 2700) {
            this.colorTemperature = 2700;
        } else if (colorTemperature > 4000) {
            this.colorTemperature = 4000;
        } else {
            this.colorTemperature = colorTemperature;
        }
    }

    /**
     * Gets the current brightness level of the lamp.
     * Uses the level inherited from AdjustableDevice.
     * 
     * @return This lamp's brightness level.
     */
    public int getBrightness() {
        return this.getLevel();
    }

    /**
     * Sets the brightness level of the lamp.
     * This uses the inherited level logic, which automatically keeps the value 
     * between 0 and 100, and manages the Auto-ON / Auto-OFF physical state.
     * 
     * @param brightness The new brightness level.
     */
    public void setBrightness(int brightness) {
        this.setLevel(brightness);
    }
    
    /**
     * Checks if this lamp is equal to another object.
     * 
     * @param o The object to compare with this lamp.
     * @return true if the given object is a lamp with the same properties as this lamp, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        
        if (o == null || this.getClass() != o.getClass()) return false;
        
        Lamp lamp = (Lamp) o;
        
        return super.equals(lamp) && this.colorTemperature == lamp.colorTemperature;
    }

    /**
     * Calculates the hash code of this lamp.
     * 
     * @return The hash code of this lamp.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), colorTemperature);
    }

    /**
     * Creates a copy of this lamp.
     * 
     * @return A copy of this lamp.
     */
    @Override
    public Lamp clone() {
        return new Lamp(this);
    }

    /**
     * Creates a string representation of this lamp.
     * Includes the base device information, the level (brightness), and the color temperature.
     * 
     * @return A string representing the lamp.
     */
    @Override
    public String toString() {  
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Color Temp: ").append(this.colorTemperature).append("K\n");
        return sb.toString();  
    } 
}