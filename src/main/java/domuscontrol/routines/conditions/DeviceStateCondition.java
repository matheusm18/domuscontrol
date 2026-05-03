package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.Simulation;
import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that evaluates whether a switchable device (like a lamp or plug)
 * is currently in a specific power state (ON or OFF).
 */
public class DeviceStateCondition implements Condition {
    private int deviceId;
    private boolean triggerWhenOn; 

    /**
     * Default constructor initializing device to null and trigger state to true (ON).
     */
    public DeviceStateCondition() {
        this.deviceId = -1;
        this.triggerWhenOn = true; 
    }

    /**
     * Parameterized constructor.
     * @param device        The live reference to the device to monitor.
     * @param triggerWhenOn The state to check for (true for ON, false for OFF).
     */
    public DeviceStateCondition(int deviceId, boolean triggerWhenOn) {
        this.deviceId = deviceId;
        this.triggerWhenOn = triggerWhenOn;
    }

    /**
     * Copy constructor for deep copying the condition itself.
     * Note: The device pointer remains shared.
     * @param other The existing DeviceStateCondition instance to copy.
     */
    public DeviceStateCondition(DeviceStateCondition other) {
        this.deviceId = other.getDeviceId();
        this.triggerWhenOn = other.getTriggerWhenOn();
    }

    /**
     * Retrieves the target device.
     * @return The switchable device reference.
     */
    public int getDeviceId() { return deviceId; }
    
    /**
     * Sets a new target device.
     * @param device The new switchable device reference.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

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
    public boolean evaluate(House house, Simulation simulation) {
        try {
            return house.readDevice(this.deviceId, d ->
                d instanceof SwitchableDevice sd && sd.isOn() == this.triggerWhenOn
            );
        } catch (DeviceNotFoundException e) {
            return false;
        }
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
        
        return this.deviceId == that.getDeviceId() && 
               this.triggerWhenOn == that.getTriggerWhenOn();
    }

    /**
     * Generates a hash code for this condition.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.triggerWhenOn);
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
          .append("Device ID: ").append(this.deviceId)
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
        return this.deviceId == deviceId;
    }
}
