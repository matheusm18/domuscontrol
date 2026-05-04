package domuscontrol.devices;

import java.util.Objects;

import domuscontrol.devices.types.SwitchableDevice;

/**
 * Represents a smart plug device.
 */
public class Plug extends SwitchableDevice {

    /** Default constructor for the Plug class. */
    public Plug() {
        super();
    }

    /**
     * Constructor for the Plug class with specified values.
     * 
     * @param brand The brand of the plug.
     * @param model The model of the plug.
     * @param consumptionPerHour The power consumption rate in Wh/h.
     */
    public Plug(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
    }

    /**
     * Copy constructor for the Plug class.
     * 
     * @param plug The Plug instance to copy.
     */
    public Plug(Plug plug) {
        super(plug);
    }

    /**
     * Checks if this plug is equal to another object.
     *
     * @param o The object to compare with this plug.
     * @return true if the given object is a plug with the same properties as this plug, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Plug plug = (Plug) o;
        return super.equals(plug);
    }

    /**
     * Calculates the hash code of this plug.
     *
     * @return The hash code of this plug.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
    
    /**
     * Creates a copy of the Plug instance.
     * 
     * @return A new Plug instance with the same properties.
     */
    @Override
    public Plug clone() {
        return new Plug(this);
    }

    /**
     * Creates a string representation of this plug.
     *
     * @return A string representing this plug.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
