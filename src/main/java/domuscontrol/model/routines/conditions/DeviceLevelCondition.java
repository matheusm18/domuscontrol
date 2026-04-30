package domuscontrol.model.routines.conditions;

import java.util.Objects;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.device.types.AdjustableDevice;

/**
 * Condition that evaluates whether a device's numerical level (e.g., volume, brightness)
 * satisfies a specific comparison against a trigger value.
 */
public class DeviceLevelCondition implements Condition {
    private AdjustableDevice device;
    private int triggerLevel;
    private Operator operator;

    /**
     * Default constructor initializing device to null and operator to EQUALS.
     */
    public DeviceLevelCondition() {
        this.device = null; 
        this.triggerLevel = 0;
        this.operator = Operator.EQUALS;
    }

    /**
     * Parameterized constructor.
     * @param device       The live reference to the adjustable device to monitor.
     * @param triggerLevel The threshold value for comparison.
     * @param operator     The comparison operator (EQUALS, GREATER_THAN, LESS_THAN).
     */
    public DeviceLevelCondition(AdjustableDevice device, int triggerLevel, Operator operator) {
        this.device = device;
        this.triggerLevel = triggerLevel;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Copy constructor for deep copying the condition itself.
     * Note: The device pointer remains shared.
     * @param other The existing DeviceLevelCondition instance to copy.
     */
    public DeviceLevelCondition(DeviceLevelCondition other) {
        this.device = other.getDevice();
        this.triggerLevel = other.getTriggerLevel();
        this.operator = other.getOperator();
    }

    /**
     * Retrieves the target device.
     * @return The adjustable device reference.
     */
    public AdjustableDevice getDevice() { return device; }
    
    /**
     * Sets a new target device.
     * @param device The new adjustable device reference.
     */
    public void setDevice(AdjustableDevice device) { this.device = device; }

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
    public boolean evaluate() {
        if (this.device != null) {
            int currentLevel = this.device.getLevel();
            switch (this.operator) {
                case EQUALS:       return currentLevel == this.triggerLevel;
                case GREATER_THAN: return currentLevel > this.triggerLevel;
                case LESS_THAN:    return currentLevel < this.triggerLevel;
                default:           return false;
            }
        }
        return false; // If the device doesn't exist, the condition fails
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
        
        return Objects.equals(this.device, that.getDevice()) && 
               this.triggerLevel == that.getTriggerLevel() &&
               this.operator == that.getOperator();
    }

    /**
     * Generates a hash code for this condition.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.triggerLevel, this.operator);
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
          .append("Device: ").append(this.device != null ? this.device.getId() : "null")
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
        return this.device != null && this.device.getId() == deviceId;
    }
}