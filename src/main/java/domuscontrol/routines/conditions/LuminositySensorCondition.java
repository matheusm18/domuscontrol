package domuscontrol.routines.conditions;

import domuscontrol.devices.sensors.LuminositySensor;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;

import java.util.Objects;

/**
 * Condition that evaluates whether a {@link LuminositySensor} reading satisfies
 * a threshold comparison. The condition is only true when the sensor is ON.
 * Compares the current luminosity reading against a trigger level using the specified operator.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class LuminositySensorCondition implements Condition {

    /** The identifier of the target luminosity sensor. */
    private int sensorId;
    /** The luminosity threshold value in lux used for comparison. */
    private int triggerLuminosity;
    /** The comparison operator to apply when evaluating the condition. */
    private Operator operator;

    /**
     * Creates a condition with no target sensor and a default trigger of 500 lx (EQUALS).
     */
    public LuminositySensorCondition() {
        this.sensorId = -1;
        this.triggerLuminosity = 500;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition for the given sensor and threshold.
     *
     * @param sensorId the identifier of the target luminosity sensor
     * @param triggerLuminosity the threshold luminosity in lux
     * @param operator the comparison operator to apply
     */
    public LuminositySensorCondition(int sensorId, int triggerLuminosity, Operator operator) {
        this.sensorId = sensorId;
        this.triggerLuminosity = triggerLuminosity;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Copy constructor.
     *
     * @param other the condition to copy
     */
    public LuminositySensorCondition(LuminositySensorCondition other) {
        this.sensorId = other.sensorId;
        this.triggerLuminosity = other.triggerLuminosity;
        this.operator = other.operator;
    }

    /**
     * Returns the identifier of the target luminosity sensor.
     *
     * @return the sensor device identifier
     */
    public int getSensorId() {
        return this.sensorId;
    }

    /**
     * Returns the threshold luminosity used for the comparison.
     *
     * @return threshold in lux
     */
    public int getTriggerLuminosity() {
        return this.triggerLuminosity;
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
     *
     * @param house the house containing the target sensor
     * @param state the current simulation state (unused; reading comes from the sensor)
     * @return true if the condition is satisfied; false otherwise
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.sensorId, d -> {
                if (!(d instanceof LuminositySensor sensor)) return false;
                if (!sensor.isOn()) return false;
                double current = sensor.getLuminosity();
                return switch (this.operator) {
                    case EQUALS       -> Math.round(current) == this.triggerLuminosity;
                    case GREATER_THAN -> current > this.triggerLuminosity;
                    case LESS_THAN    -> current < this.triggerLuminosity;
                };
            });
        } catch (DeviceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a new LuminositySensorCondition with the same state
     */
    @Override
    public Condition copy() {
        return new LuminositySensorCondition(this);
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
        LuminositySensorCondition c = (LuminositySensorCondition) o;
        return this.sensorId == c.sensorId
            && this.triggerLuminosity == c.triggerLuminosity
            && this.operator == c.operator;
    }

    /**
     * Returns a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.sensorId, this.triggerLuminosity, this.operator);
    }

    /**
     * Creates a deep copy of this condition.
     *
     * @return a new LuminositySensorCondition with the same state
     */
    @Override
    public LuminositySensorCondition clone() {
        return new LuminositySensorCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with condition details
     */
    @Override
    public String toString() {
        return "LuminositySensorCondition { Sensor #" + this.sensorId
            + " luminosity " + this.operator + " " + this.triggerLuminosity + " lx }";
    }
}
