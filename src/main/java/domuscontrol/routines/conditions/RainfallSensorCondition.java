package domuscontrol.routines.conditions;

import domuscontrol.devices.sensors.RainfallSensor;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;

import java.util.Objects;

/**
 * Condition that evaluates whether a {@link RainfallSensor} reading satisfies
 * a threshold comparison. The condition is only true when the sensor is ON.
 * Compares the current rainfall intensity against a trigger level using the specified operator.
 * For EQUALS comparisons, a tolerance of ±0.5 mm/h is applied due to the continuous nature of readings.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class RainfallSensorCondition implements Condition {

    /** The identifier of the target rainfall sensor. */
    private int sensorId;
    /** The rainfall intensity threshold in millimeters per hour (mm/h) used for comparison. */
    private double triggerRainfall;
    /** The comparison operator to apply when evaluating the condition. */
    private Operator operator;

    /**
     * Creates a condition with no target sensor and a default trigger of 5.0 mm/h (GREATER_THAN).
     */
    public RainfallSensorCondition() {
        this.sensorId = -1;
        this.triggerRainfall = 5.0;
        this.operator = Operator.GREATER_THAN;
    }

    /**
     * Creates a condition for the given sensor and threshold.
     *
     * @param sensorId        the identifier of the target rainfall sensor
     * @param triggerRainfall the threshold rainfall intensity in mm/h
     * @param operator        the comparison operator to apply
     */
    public RainfallSensorCondition(int sensorId, double triggerRainfall, Operator operator) {
        this.sensorId = sensorId;
        this.triggerRainfall = triggerRainfall;
        this.operator = operator != null ? operator : Operator.GREATER_THAN;
    }

    /**
     * Copy constructor.
     *
     * @param other the condition to copy
     */
    public RainfallSensorCondition(RainfallSensorCondition other) {
        this.sensorId = other.sensorId;
        this.triggerRainfall = other.triggerRainfall;
        this.operator = other.operator;
    }

    /**
     * Returns the identifier of the target rainfall sensor.
     *
     * @return the sensor device identifier
     */
    public int getSensorId() {
        return this.sensorId;
    }

    /**
     * Returns the threshold rainfall intensity used for the comparison.
     *
     * @return threshold in mm/h
     */
    public double getTriggerRainfall() {
        return this.triggerRainfall;
    }

    /**
     * Returns the comparison operator applied to the sensor reading.
     *
     * @return the operator
     */
    public Operator getOperator() {
        return this.operator;
    }

    /**
     * Evaluates whether the sensor's current reading satisfies the threshold comparison.
     * Returns false if the sensor is OFF or not found.
     * For EQUALS, a tolerance of +-0.5 mm/h is used due to the continuous nature of the reading.
     *
     * @param house the house containing the target sensor
     * @param state the current simulation state (unused; reading comes from the sensor)
     * @return true if the condition is satisfied; false otherwise
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.sensorId, d -> {
                if (!(d instanceof RainfallSensor sensor)) return false;
                if (!sensor.isOn()) return false;
                double current = sensor.getRainfall();
                return switch (this.operator) {
                    case EQUALS       -> Math.abs(current - this.triggerRainfall) < 0.5;
                    case GREATER_THAN -> current > this.triggerRainfall;
                    case LESS_THAN    -> current < this.triggerRainfall;
                };
            });
        } catch (DeviceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a new RainfallSensorCondition with the same state
     */
    @Override
    public Condition copy() {
        return new RainfallSensorCondition(this);
    }

    /**
     * Checks whether this condition depends on the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the target sensor ID matches; false otherwise
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.sensorId == deviceId;
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if sensor ID, threshold, and operator all match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RainfallSensorCondition c = (RainfallSensorCondition) o;
        return this.sensorId == c.sensorId
            && Double.compare(this.triggerRainfall, c.triggerRainfall) == 0
            && this.operator == c.operator;
    }

    /**
     * Returns a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.sensorId, this.triggerRainfall, this.operator);
    }

    /**
     * Creates a deep copy of this condition.
     *
     * @return a new RainfallSensorCondition with the same state
     */
    @Override
    public RainfallSensorCondition clone() {
        return new RainfallSensorCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with condition details
     */
    @Override
    public String toString() {
        return "RainfallSensorCondition { Sensor #" + this.sensorId
            + " rainfall " + this.operator + " " + String.format("%.1f", this.triggerRainfall) + " mm/h }";
    }
}
