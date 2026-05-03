package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.Simulation;
import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that evaluates whether the opening level of an openable device
 * (e.g., curtains, garage gate) satisfies a specific comparison against a trigger value.
 * The opening level ranges from 0 (fully closed) to 100 (fully open).
 */
public class DeviceOpenCondition implements Condition {

    private int deviceId;
    private int triggerLevel;
    private Operator operator;

    /**
     * Default constructor initializing device to null and operator to EQUALS.
     */
    public DeviceOpenCondition() {
        this.deviceId     = -1;
        this.triggerLevel = 0;
        this.operator     = Operator.EQUALS;
    }

    /**
     * Parameterized constructor.
     *
     * @param device       The live reference to the openable device to monitor.
     * @param triggerLevel The opening level threshold (0-100) for comparison.
     * @param operator     The comparison operator (EQUALS, GREATER_THAN, LESS_THAN).
     */
    public DeviceOpenCondition(int deviceId, int triggerLevel, Operator operator) {
        this.deviceId     = deviceId;
        this.triggerLevel = triggerLevel;
        this.operator     = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Copy constructor using getters to access the other instance's state.
     * The device pointer remains shared.
     *
     * @param other The existing DeviceOpenCondition instance to copy.
     */
    public DeviceOpenCondition(DeviceOpenCondition other) {
        this.deviceId     = other.getDeviceId();
        this.triggerLevel = other.getTriggerLevel();
        this.operator     = other.getOperator();
    }

    /**
     * Returns the target device.
     *
     * @return The openable device reference.
     */
    public int getDeviceId() { return deviceId; }

    /**
     * Sets a new target device.
     *
     * @param device The new openable device reference.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    /**
     * Returns the trigger level threshold.
     *
     * @return The opening level threshold (0-100).
     */
    public int getTriggerLevel() { return triggerLevel; }

    /**
     * Sets a new trigger level threshold.
     *
     * @param triggerLevel The new opening level threshold (0-100).
     */
    public void setTriggerLevel(int triggerLevel) { this.triggerLevel = triggerLevel; }

    /**
     * Returns the comparison operator.
     *
     * @return The Operator enum value.
     */
    public Operator getOperator() { return operator; }

    /**
     * Sets a new comparison operator.
     *
     * @param operator The new Operator to be used.
     */
    public void setOperator(Operator operator) { this.operator = operator != null ? operator : Operator.EQUALS; }

    /**
     * Evaluates the condition by reading the current opening level of the live device reference.
     *
     * @return true if the current opening level satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        try {
            return house.readDevice(this.deviceId, d -> {
                if (!(d instanceof OpenableDevice od)) return false;
                int currentLevel = od.getOpeningLevel();
            switch (this.getOperator()) {
                case EQUALS:       return currentLevel == this.getTriggerLevel();
                case GREATER_THAN: return currentLevel > this.getTriggerLevel();
                case LESS_THAN:    return currentLevel < this.getTriggerLevel();
                default:           return false;
            }
            });
        } catch (DeviceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a deep copy of this condition.
     *
     * @return A new instance of DeviceOpenCondition.
     */
    @Override
    public Condition copy() {
        return new DeviceOpenCondition(this);
    }

    /**
     * Checks if this condition is associated with a specific device ID.
     * Used by the RoutineManager to clean up routines when a device is deleted.
     *
     * @param deviceId The ID to check.
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }

    /**
     * Compares this condition with another object for equality using getters.
     *
     * @param o The object to compare with.
     * @return true if device, trigger level, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DeviceOpenCondition that = (DeviceOpenCondition) o;
        return this.getTriggerLevel() == that.getTriggerLevel() &&
               this.getOperator() == that.getOperator() &&
               this.getDeviceId() == that.getDeviceId();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return The hash code based on device, trigger level, and operator.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceId(), this.getTriggerLevel(), this.getOperator());
    }

    /**
     * Clones this condition instance.
     *
     * @return A cloned DeviceOpenCondition.
     */
    @Override
    public DeviceOpenCondition clone() {
        return new DeviceOpenCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return Formatted string containing device info and comparison logic.
     */
    @Override
    public String toString() {
        return "DeviceOpenCondition { Device: " +
               this.getDeviceId() +
               ", Opening " + this.getOperator() + " " + this.getTriggerLevel() + "% }";
    }
}
