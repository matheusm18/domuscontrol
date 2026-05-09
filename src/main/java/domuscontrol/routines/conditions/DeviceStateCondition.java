package domuscontrol.routines.conditions;

import java.util.Objects;
import domuscontrol.simulation.SimulationState;
import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

/**
 * Condition that checks whether a switchable device is on or off.
 * Evaluates to true when the device's current state matches the expected triggerWhenOn state.
 * The condition is false if the target device is not found.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DeviceStateCondition implements Condition {
    
    /** The identifier of the target switchable device. */
    private int deviceId;
    /** Whether this condition should trigger when the device is on (true) or off (false). */
    private boolean triggerWhenOn;

    /**
     * Creates a condition with no target device that triggers when the device is on.
     */
    public DeviceStateCondition() {
        this.deviceId = -1;
        this.triggerWhenOn = true;
    }

    /**
     * Creates a condition for the given device and target state.
     *
     * @param deviceId the target device identifier
     * @param triggerWhenOn true to trigger when the device is on, false to trigger when it is off
     */
    public DeviceStateCondition(int deviceId, boolean triggerWhenOn) {
        this.deviceId = deviceId;
        this.triggerWhenOn = triggerWhenOn;
    }

    /**
     * Creates a copy of another device-state condition.
     *
     * @param other the condition to copy
     */
    public DeviceStateCondition(DeviceStateCondition other) {
        this.deviceId = other.getDeviceId();
        this.triggerWhenOn = other.getTriggerWhenOn();
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
     * Checks the state required by this condition.
     *
     * @return true if this condition triggers when the device is on, false otherwise
     */
    public boolean getTriggerWhenOn() {
        return this.triggerWhenOn;
    }
    
    /**
     * Sets the state required by this condition.
     *
     * @param triggerWhenOn true to trigger when the device is on, false otherwise
     */
    public void setTriggerWhenOn(boolean triggerWhenOn) {
        this.triggerWhenOn = triggerWhenOn;
    }

    /**
     * Evaluates this condition against the current state of the target device.
     *
     * @param house the house where the target device is stored
     * @param state the current simulation state
     * @return true if the current state matches the triggerWhenOn requirement; false otherwise.
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.deviceId, d ->
                d instanceof SwitchableDevice sd && sd.isOn() == this.triggerWhenOn
            );
        } catch (DeviceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied DeviceStateCondition instance
     */
    @Override
    public Condition copy() {
        return new DeviceStateCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if device reference and trigger state match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        DeviceStateCondition condition = (DeviceStateCondition) o;
        
        return this.deviceId == condition.getDeviceId() &&
               this.triggerWhenOn == condition.getTriggerWhenOn();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.triggerWhenOn);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied DeviceStateCondition instance
     */
    @Override
    public DeviceStateCondition clone() {
        return new DeviceStateCondition(this);
    }

    /**
     * Returns a string representation of the condition.
     *
     * @return a formatted string with the condition information
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
