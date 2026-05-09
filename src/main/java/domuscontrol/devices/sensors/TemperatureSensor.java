package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;

import java.util.Objects;

/**
 * A sensor device that reads the outside air temperature from the simulation.
 * The stored temperature is only updated while the sensor is ON.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class TemperatureSensor extends Sensor {

    /**
     * The last recorded temperature reading in degrees Celsius.
     */
    private double temperature;

    /**
     * Creates a temperature sensor with default values.
     */
    public TemperatureSensor() {
        super();
        this.temperature = 0.0;
    }

    /**
     * Creates a temperature sensor with the given hardware details.
     *
     * @param brand the sensor brand
     * @param model the sensor model
     * @param consumptionPerHour the standby power consumption in Wh
     */
    public TemperatureSensor(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
        this.temperature = 0.0;
    }

    /**
     * Copy constructor.
     *
     * @param other the sensor to copy
     */
    public TemperatureSensor(TemperatureSensor other) {
        super(other);
        this.temperature = other.temperature;
    }

    /**
     * Returns the last recorded temperature reading.
     *
     * @return temperature in degrees Celsius
     */
    public double getTemperature() {
        return this.temperature;
    }

    /**
     * Updates the stored temperature from the simulation state.
     * Does nothing if the sensor is OFF.
     *
     * @param state the current simulation state snapshot
     */
    @Override
    public void updateFromState(SimulationState state) {
        if (isOn()) {
            this.temperature = state.getTemperature();
        }
    }

    /**
     * Creates a deep copy of this sensor.
     *
     * @return a new TemperatureSensor with the same state
     */
    @Override
    public Device clone() {
        return new TemperatureSensor(this);
    }

    /**
     * Compares this sensor with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the superclass fields and temperature reading match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TemperatureSensor s = (TemperatureSensor) o;
        return super.equals(s) && Double.compare(this.temperature, s.getTemperature()) == 0;
    }

    /**
     * Returns a hash code for this sensor.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.temperature);
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
          .append("Temperature: ").append(String.format("%.1f", this.temperature)).append(" ºC\n");
        return sb.toString();
    }
}
