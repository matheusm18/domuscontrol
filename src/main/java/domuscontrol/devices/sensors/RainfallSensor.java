package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.WeatherCondition;

import java.util.Objects;

/**
 * A sensor device that measures rainfall intensity derived from the simulation weather.
 * When weather is {@link WeatherCondition#RAINING}, the reading is a random value in [1, 15) mm/h.
 * When weather is {@link WeatherCondition#STORMY}, the reading is a random value in [15, 50) mm/h.
 * Any other weather condition results in 0 mm/h.
 * The reading is only updated while the sensor is ON.
 */
public class RainfallSensor extends Sensor {

    private double rainfall;

    /**
     * Creates a rainfall sensor with default values.
     */
    public RainfallSensor() {
        super();
        this.rainfall = 0.0;
    }

    /**
     * Creates a rainfall sensor with the given hardware details.
     *
     * @param brand              the sensor brand
     * @param model              the sensor model
     * @param consumptionPerHour the standby power consumption in Wh
     */
    public RainfallSensor(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
        this.rainfall = 0.0;
    }

    /**
     * Copy constructor.
     *
     * @param other the sensor to copy
     */
    public RainfallSensor(RainfallSensor other) {
        super(other);
        this.rainfall = other.rainfall;
    }

    /**
     * Returns the last recorded rainfall intensity.
     *
     * @return rainfall in mm/h
     */
    public double getRainfall() {
        return this.rainfall;
    }

    /**
     * Updates the stored rainfall reading based on the current weather condition.
     * Does nothing if the sensor is OFF.
     *
     * @param state the current simulation state snapshot
     */
    @Override
    public void updateFromState(SimulationState state) {
        if (isOn()) {
            this.rainfall = switch (state.getWeather()) {
                case RAINING -> 1.0 + Math.random() * 14.0;
                case STORMY  -> 15.0 + Math.random() * 35.0;
                default      -> 0.0;
            };
        }
    }

    /**
     * Creates a deep copy of this sensor.
     *
     * @return a new RainfallSensor with the same state
     */
    @Override
    public Device clone() {
        return new RainfallSensor(this);
    }

    /**
     * Compares this sensor with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the superclass fields and rainfall reading match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RainfallSensor s = (RainfallSensor) o;
        return super.equals(s) && Double.compare(this.rainfall, s.getRainfall()) == 0;
    }

    /**
     * Returns a hash code for this sensor.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.rainfall);
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
          .append("Rainfall: ").append(String.format("%.1f", this.rainfall)).append(" mm/h\n");
        return sb.toString();
    }
}
