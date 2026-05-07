package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;

import java.util.Objects;

/**
 * A sensor device that reads the outside luminosity from the simulation.
 * The stored luminosity is only updated while the sensor is ON.
 */
public class LuminositySensor extends Sensor {

    private double luminosity;

    /**
     * Creates a luminosity sensor with default values.
     */
    public LuminositySensor() {
        super();
        this.luminosity = 0.0;
    }

    /**
     * Creates a luminosity sensor with the given hardware details.
     *
     * @param brand              the sensor brand
     * @param model              the sensor model
     * @param consumptionPerHour the standby power consumption in Wh
     */
    public LuminositySensor(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
        this.luminosity = 0.0;
    }

    /**
     * Copy constructor.
     *
     * @param other the sensor to copy
     */
    public LuminositySensor(LuminositySensor other) {
        super(other);
        this.luminosity = other.luminosity;
    }

    /**
     * Returns the last recorded luminosity reading.
     *
     * @return luminosity in lux
     */
    public double getLuminosity() {
        return this.luminosity;
    }

    /**
     * Updates the stored luminosity from the simulation state.
     * Does nothing if the sensor is OFF.
     *
     * @param state the current simulation state snapshot
     */
    @Override
    public void updateFromState(SimulationState state) {
        if (isOn()) {
            this.luminosity = state.getLuminosity();
        }
    }

    /**
     * Creates a deep copy of this sensor.
     *
     * @return a new LuminositySensor with the same state
     */
    @Override
    public Device clone() {
        return new LuminositySensor(this);
    }

    /**
     * Compares this sensor with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the superclass fields and luminosity reading match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LuminositySensor s = (LuminositySensor) o;
        return super.equals(s) && Double.compare(this.luminosity, s.getLuminosity()) == 0;
    }

    /**
     * Returns a hash code for this sensor.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.luminosity);
    }

    /**
     * Returns a string representation of this sensor including its current reading.
     *
     * @return a formatted string with sensor details
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Luminosity: ").append(String.format("%.1f", this.luminosity)).append(" lx\n");
        return sb.toString();
    }
}
