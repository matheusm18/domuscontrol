package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.SimulationState;
import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that compares the level of an adjustable device with a trigger value.
 */
public class DeviceLevelCondition implements Condition {
    private int deviceId;
    private int triggerLevel;
    private Operator operator;

    /**
     * Creates a condition with no target device, trigger level 0, and EQUALS operator.
     */
    public DeviceLevelCondition() {
        this.deviceId = -1;
        this.triggerLevel = 0;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition for the given device, trigger level, and operator.
     *
     * @param deviceId the target device identifier
     * @param triggerLevel the trigger level for comparison
     * @param operator the comparison operator
     */
    public DeviceLevelCondition(int deviceId, int triggerLevel, Operator operator) {
        this.deviceId = deviceId;
        this.triggerLevel = triggerLevel;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Creates a copy of another device-level condition.
     *
     * @param other the condition to copy
     */
    public DeviceLevelCondition(DeviceLevelCondition other) {
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
     * Gets the trigger level.
     *
     * @return the trigger level
     */
    public int getTriggerLevel() {
        return this.triggerLevel;
    }
    
    /**
     * Sets the trigger level.
     *
     * @param triggerLevel the trigger level
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
     * Evaluates this condition against the current level of the target device.
     *
     * @param house the house where the target device is stored
     * @param simulation the current simulation state
     * @return true if the current level satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.deviceId, d -> {
                if (!(d instanceof AdjustableDevice ad)) {
                    return false;
                }
                int currentLevel = ad.getLevel();
                switch (this.operator) {
                    case EQUALS:       return currentLevel == this.triggerLevel;
                    case GREATER_THAN: return currentLevel > this.triggerLevel;
                    case LESS_THAN:    return currentLevel < this.triggerLevel;
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
     * @return a copied DeviceLevelCondition instance
     */
    @Override
    public Condition copy() {
        return new DeviceLevelCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if device reference, trigger level, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        DeviceLevelCondition condition = (DeviceLevelCondition) o;
        
        return this.deviceId == condition.getDeviceId() &&
               this.triggerLevel == condition.getTriggerLevel() &&
               this.operator == condition.getOperator();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.triggerLevel, this.operator);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied DeviceLevelCondition instance
     */
    @Override
    public DeviceLevelCondition clone() {
        return new DeviceLevelCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceLevelCondition { ")
          .append("Device ID: ").append(this.deviceId)
          .append(", Level ").append(this.operator).append(" ").append(this.triggerLevel)
          .append(" }");
        return sb.toString();
    }

    /**
     * Checks whether this condition depends on the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the target device ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }
}
