package domuscontrol.devices;

import java.util.Objects;

import domuscontrol.devices.types.SwitchableDevice;

/**
 * Represents a smart relay device.
 * A relay is the simplest type of connected device, functioning as a basic
 * switch that allows or prevents electrical current from passing (on/off state).
 */
public class Relay extends SwitchableDevice {
    
    /**
     * Default constructor.
     * Initializes a new relay with default device settings.
     */
    public Relay() {
        super(); 
    }

    /**
     * Parameterized constructor.
     * Initializes a new relay with specific brand, model, and power consumption.
     *
     * @param brand The brand of the relay.
     * @param model The model of the relay.
     * @param consumptionPerHour The power consumption of the relay in Wh/h.
     */
    public Relay(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
    }

    /**
     * Copy constructor.
     * Creates a new Relay instance by copying the state of an existing Relay.
     *
     * @param r The Relay object to copy.
     */
    public Relay(Relay r) {
        super(r); 
    }

    /**
     * Checks if this relay is equal to another object.
     *
     * @param o The object to compare with this relay.
     * @return true if the given object is a relay with the same properties as this relay, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Relay relay = (Relay) o;
        return super.equals(relay);
    }

    /**
     * Calculates the hash code of this relay.
     *
     * @return The hash code of this relay.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Creates a copy of this relay.
     *
     * @return A copy of this relay.
     */
    @Override
    public Relay clone() {
        return new Relay(this);
    }

    /**
     * Creates a string representation of this relay.
     *
     * @return A string representing this relay.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
