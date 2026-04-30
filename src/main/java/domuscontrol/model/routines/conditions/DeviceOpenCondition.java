package domuscontrol.model.routines.conditions;

import java.util.Objects;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.device.types.OpenableDevice;

/**
 * Condition that evaluates whether an openable device (like a door, window, or gate) 
 * matches a specific open or closed state.
 */
public class DeviceOpenCondition implements Condition {
    private OpenableDevice device;
    private boolean triggerWhenOpen;

    /**
     * Default constructor initializing device to null and trigger state to true (open).
     */
    public DeviceOpenCondition() {
        this.device = null;
        this.triggerWhenOpen = true;
    }

    /**
     * Parameterized constructor.
     * @param device          The live reference to the openable device to monitor.
     * @param triggerWhenOpen The state to check for (true for open, false for closed).
     */
    public DeviceOpenCondition(OpenableDevice device, boolean triggerWhenOpen) {
        this.device = device;
        this.triggerWhenOpen = triggerWhenOpen;
    }

    /**
     * Copy constructor for deep copying the condition itself.
     * Note: The device pointer remains shared.
     * @param other The existing DeviceOpenCondition instance to copy.
     */
    public DeviceOpenCondition(DeviceOpenCondition other) {
        this.device = other.getDevice();
        this.triggerWhenOpen = other.getTriggerWhenOpen();
    }

    /**
     * Retrieves the target device.
     * @return The openable device reference.
     */
    public OpenableDevice getDevice() { return device; }
    
    /**
     * Sets a new target device.
     * @param device The new openable device reference.
     */
    public void setDevice(OpenableDevice device) { this.device = device; }

    /**
     * Checks the trigger state setting.
     * @return true if the condition triggers when the device is open; false if when closed.
     */
    public boolean getTriggerWhenOpen() { return triggerWhenOpen; }
    
    /**
     * Sets the trigger state requirement.
     * @param triggerWhenOpen true to trigger on open, false to trigger on closed.
     */
    public void setTriggerWhenOpen(boolean triggerWhenOpen) { this.triggerWhenOpen = triggerWhenOpen; }

    /**
     * Evaluates the condition by checking the current open/closed state of the live device reference.
     * @return true if the current state matches the triggerWhenOpen requirement; false otherwise.
     */
    @Override
    public boolean evaluate() {
        if (this.device != null) {
            return this.device.isOpen() == this.triggerWhenOpen;
        }
        return false; // If the device doesn't exist, the condition fails
    }

    /**
     * Creates a deep copy of this condition.
     * @return A new instance of DeviceOpenCondition.
     */
    @Override
    public Condition copy() {
        return new DeviceOpenCondition(this);
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
        
        DeviceOpenCondition that = (DeviceOpenCondition) o;
        
        return Objects.equals(this.device, that.getDevice()) && 
               this.triggerWhenOpen == that.getTriggerWhenOpen();
    }

    /**
     * Generates a hash code for this condition.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.triggerWhenOpen);
    }

    /**
     * Clones the current condition instance.
     * @return A cloned DeviceOpenCondition.
     */
    @Override
    public DeviceOpenCondition clone() {
        return new DeviceOpenCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     * @return Formatted string containing device info and the required state.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceOpenCondition { ")
          .append("Device: ").append(this.device != null ? this.device.getId() : "null")
          .append(", Trigger when OPEN: ").append(this.triggerWhenOpen)
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