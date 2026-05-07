package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.SimulationState;
import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that compares the opening level of an openable device with a trigger value.
 */
public class DeviceOpenCondition implements Condition {

    private int deviceId;
    private int triggerLevel;
    private Operator operator;

    /**
     * Creates a condition with no target device, trigger level 0, and EQUALS operator.
     */
    public DeviceOpenCondition() {
        this.deviceId = -1;
        this.triggerLevel = 0;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition for the given device, trigger level, and operator.
     *
     * @param deviceId the target device identifier
     * @param triggerLevel the trigger opening level for comparison
     * @param operator the comparison operator
     */
    public DeviceOpenCondition(int deviceId, int triggerLevel, Operator operator) {
        this.deviceId = deviceId;
        this.triggerLevel = triggerLevel;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Creates a copy of another device-open condition.
     *
     * @param other the condition to copy
     */
    public DeviceOpenCondition(DeviceOpenCondition other) {
        this.deviceId = other.getDeviceId();
        this.triggerLevel = other.getTriggerLevel();
        this.operator = other.getOperator();
    }

    /**
     * Gets the target device identifier.
     *
     * @return the target device identifier
     */
    public int getDeviceId() {
        return this.deviceId;
    }

    /**
     * Sets the target device identifier.
     *
     * @param deviceId the target device identifier
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the trigger opening level.
     *
     * @return the trigger opening level
     */
    public int getTriggerLevel() {
        return this.triggerLevel;
    }

    /**
     * Sets the trigger opening level.
     *
     * @param triggerLevel the trigger opening level
     */
    public void setTriggerLevel(int triggerLevel) {
        this.triggerLevel = triggerLevel;
    }

    /**
     * Gets the comparison operator.
     *
     * @return the comparison operator
     */
    public Operator getOperator() {
        return this.operator;
    }

    /**
     * Sets the comparison operator.
     *
     * @param operator the comparison operator
     */
    public void setOperator(Operator operator) {
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Evaluates this condition against the current opening level of the target device.
     *
     * @param house the house where the target device is stored
     * @param simulation the current simulation state
     * @return true if the current opening level satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.deviceId, d -> {
                if (!(d instanceof OpenableDevice od)) {
                    return false;
                }
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
     * Creates a copy of this condition.
     *
     * @return a copied DeviceOpenCondition instance
     */
    @Override
    public Condition copy() {
        return new DeviceOpenCondition(this);
    }

    /**
     * Checks whether this condition depends on the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if device, trigger level, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DeviceOpenCondition condition = (DeviceOpenCondition) o;
        return this.getTriggerLevel() == condition.getTriggerLevel() &&
               this.getOperator() == condition.getOperator() &&
               this.getDeviceId() == condition.getDeviceId();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceId(), this.getTriggerLevel(), this.getOperator());
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied DeviceOpenCondition instance
     */
    @Override
    public DeviceOpenCondition clone() {
        return new DeviceOpenCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceOpenCondition { ")
          .append("Device ID: ").append(this.getDeviceId())
          .append(", Opening ").append(this.getOperator()).append(" ").append(this.getTriggerLevel()).append("%")
          .append(" }");
        return sb.toString();
    }
}
