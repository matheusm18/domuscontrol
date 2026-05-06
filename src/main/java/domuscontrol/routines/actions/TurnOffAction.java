package domuscontrol.routines.actions;

import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action that turns off a switchable device.
 */
public class TurnOffAction implements Action {
    private int deviceId;

    /**
     * Creates an action with no target device.
     */
    public TurnOffAction() {
        this.deviceId = -1;
    }
    
    /**
     * Creates an action for the given device.
     *
     * @param deviceId the target device identifier
     */
    public TurnOffAction(int deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * Creates a copy of another turn-off action.
     *
     * @param other the action to copy
     */
    public TurnOffAction(TurnOffAction other) {
        this.deviceId = other.getDeviceId();
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
     * Executes this action in the given house.
     *
     * @param house the house where the target device should be turned off
     */
    @Override
    public void execute(House house) {
        try {
            house.interactWithDevice(this.deviceId, d -> {
                if (d instanceof SwitchableDevice sd) {
                    sd.turnOff();
                }
            });
        } catch (DeviceNotFoundException e) {
            // Routine action failed because the device no longer exists.
        }
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied TurnOffAction instance
     */
    @Override
    public Action copy() {
        return new TurnOffAction(this);
    }

    /**
     * Compares this action with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the devices match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TurnOffAction action = (TurnOffAction) o;
        
        return this.deviceId == action.getDeviceId();
    }

    /**
     * Generates a hash code for this action.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId);
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied TurnOffAction instance
     */
    @Override
    public TurnOffAction clone() {
        return new TurnOffAction(this);
    }

    /**
     * Returns a string representation of the action.
     *
     * @return a formatted string with the action information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TurnOffAction { ")
          .append("Device ID: ").append(this.deviceId)
          .append(" }");
        return sb.toString();
    }

    /**
     * Checks whether this action targets the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }
}
