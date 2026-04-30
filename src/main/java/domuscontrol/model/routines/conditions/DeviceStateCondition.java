package domuscontrol.model.routines.conditions;

import java.util.Objects;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.device.types.SwitchableDevice;

/**
 * Condition that evaluates whether a switchable device (like a lamp or plug)
 * is currently in a specific power state (ON or OFF).
 */
public class DeviceStateCondition implements Condition {
    private SwitchableDevice device;
    private boolean triggerWhenOn; 

    /**
     * Default constructor initializing device to null and trigger state to true (ON).
     */
    public DeviceStateCondition() {
        this.device = null;
        this.triggerWhenOn = true; 
    }

    /**
     * Parameterized constructor.
     * @param device        The live reference to the device to monitor.
     * @param triggerWhenOn The state to check for (true for ON, false for OFF).
     */
    public DeviceStateCondition(SwitchableDevice device, boolean triggerWhenOn) {
        this.device = device;
        this.triggerWhenOn = triggerWhenOn;
    }

    /**
     * Copy constructor for deep copying the condition itself.
     * Note: The device pointer remains shared.
     * @param other The existing DeviceStateCondition instance to copy.
     */
    public DeviceStateCondition(DeviceStateCondition other) {
        this.device = other.getDevice();
        this.triggerWhenOn = other.getTriggerWhenOn();
    }

    /**
     * Retrieves the target device.
     * @return The switchable device reference.
     */
    public SwitchableDevice getDevice() { return device; }
    
    /**
     * Sets a new target device.
     * @param device The new switchable device reference.
     */
    public void setDevice(SwitchableDevice device) { this.device = device; }

    /**
     * Checks the trigger state setting.
     * @return true if the condition triggers when the device is ON; false if when OFF.
     */
    public boolean getTriggerWhenOn() { return triggerWhenOn; }
    
    /**
     * Sets the trigger state requirement.
     * @param triggerWhenOn true to trigger on ON, false to trigger on OFF.
     */
    public void setTriggerWhenOn(boolean triggerWhenOn) { this.triggerWhenOn = triggerWhenOn; }

    /**
     * Evaluates the condition by checking the current power state of the live device reference.
     * @return true if the current state matches the triggerWhenOn requirement; false otherwise.
     */
    @Override
    public boolean evaluate() {
        if (this.device != null) {
            return this.device.isOn() == this.triggerWhenOn;
        }
        return false; // If the device doesn't exist, the condition fails
    }

    /**
     * Creates a deep copy of this condition.
     * @return A new instance of DeviceStateCondition.
     */
    @Override
    public Condition copy() {
        return new DeviceStateCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     * @param o The object to compare with.
     * @return true if device reference and trigger state match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        DeviceStateCondition that = (DeviceStateCondition) o;
        
        return Objects.equals(this.device, that.getDevice()) && 
               this.triggerWhenOn == that.getTriggerWhenOn();
    }

    /**
     * Generates a hash code for this condition.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.triggerWhenOn);
    }

    /**
     * Clones the current condition instance.
     * @return A cloned DeviceStateCondition.
     */
    @Override
    public DeviceStateCondition clone() {
        return new DeviceStateCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     * @return Formatted string containing device info and the required state.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceStateCondition { ")
          .append("Device: ").append(this.device != null ? this.device.getId() : "null")
          .append(", Trigger when ON: ").append(this.triggerWhenOn)
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