package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.Simulation;
import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that evaluates whether a device's numerical level (e.g., volume, brightness)
 * satisfies a specific comparison against a trigger value.
 */
public class DeviceLevelCondition implements Condition {
    private int deviceId;
    private int triggerLevel;
    private Operator operator;

    /**
     * Default constructor initializing device to null and operator to EQUALS.
     */
    public DeviceLevelCondition() {
        this.deviceId = -1; 
        this.triggerLevel = 0;
        this.operator = Operator.EQUALS;
    }

    /**
     * Parameterized constructor.
     * @param device       The live reference to the adjustable device to monitor.
     * @param triggerLevel The threshold value for comparison.
     * @param operator     The comparison operator (EQUALS, GREATER_THAN, LESS_THAN).
     */
    public DeviceLevelCondition(int deviceId, int triggerLevel, Operator operator) {
        this.deviceId = deviceId;
        this.triggerLevel = triggerLevel;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Copy constructor for deep copying the condition itself.
     * Note: The device pointer remains shared.
     * @param other The existing DeviceLevelCondition instance to copy.
     */
    public DeviceLevelCondition(DeviceLevelCondition other) {
        this.deviceId = other.getDeviceId();
        this.triggerLevel = other.getTriggerLevel();
        this.operator = other.getOperator();
    }

    /**
     * Retrieves the target device.
     * @return The adjustable device reference.
     */
    public int getDeviceId() { return deviceId; }
    
    /**
     * Sets a new target device.
     * @param device The new adjustable device reference.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    /**
     * Retrieves the trigger level value.
     * @return The threshold level.
     */
    public int getTriggerLevel() { return triggerLevel; }
    
    /**
     * Sets a new trigger level value.
     * @param triggerLevel The new threshold level.
     */
    public void setTriggerLevel(int triggerLevel) { this.triggerLevel = triggerLevel; }

    /**
     * Retrieves the comparison operator.
     * @return The Operator enum value.
     */
    public Operator getOperator() { return operator; }
    
    /**
     * Sets a new comparison operator.
     * @param operator The new Operator to be used.
     */
    public void setOperator(Operator operator) { this.operator = operator != null ? operator : Operator.EQUALS; }

    /**
     * Evaluates the condition by reading the current level of the live device reference.
     * @return true if the current level satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        try {
            return house.readDevice(this.deviceId, d -> {
                if (!(d instanceof AdjustableDevice ad)) return false;
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
     * Creates a deep copy of this condition.
     * @return A new instance of DeviceLevelCondition.
     */
    @Override
    public Condition copy() {
        return new DeviceLevelCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     * @param o The object to compare with.
     * @return true if device reference, trigger level, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        DeviceLevelCondition that = (DeviceLevelCondition) o;
        
        return this.deviceId == that.getDeviceId() && 
               this.triggerLevel == that.getTriggerLevel() &&
               this.operator == that.getOperator();
    }

    /**
     * Generates a hash code for this condition.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.triggerLevel, this.operator);
    }

    /**
     * Clones the current condition instance.
     * @return A cloned DeviceLevelCondition.
     */
    @Override
    public DeviceLevelCondition clone() {
        return new DeviceLevelCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     * @return Formatted string containing device info and comparison logic.
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
     * Checks if this condition is associated with a specific device ID.
     * Used by the House/RoutineManager to clean up routines when a device is deleted.
     * @param deviceId The ID to check.
     * @return true if the target device ID matches; false otherwise.
     */
    @Override 
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }
}
