package domuscontrol.routines.actions;

import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action that turns on a switchable device.
 */
public class TurnOnAction implements Action {
    private int deviceId;

    /**
     * Creates an action with no target device.
     */
    public TurnOnAction() {
        this.deviceId = -1;
    }

    /**
     * Creates an action for the given device.
     *
     * @param deviceId the target device identifier
     */
    public TurnOnAction(int deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * Creates a copy of another turn-on action.
     *
     * @param other the action to copy
     */
    public TurnOnAction(TurnOnAction other) {
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
     * @param house the house where the target device should be turned on
     */
    @Override
    public void execute(House house) {
        try {
            house.interactWithDevice(this.deviceId, d -> {
                if (d instanceof SwitchableDevice sd) {
                    sd.turnOn();
                }
            });
        } catch (DeviceNotFoundException e) {
            // Routine action failed because the device no longer exists.
        }
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied TurnOnAction instance
     */
    @Override
    public Action copy() {
        return new TurnOnAction(this);
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
        
        TurnOnAction action = (TurnOnAction) o;
        
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
     * @return a copied TurnOnAction instance
     */
    @Override
    public TurnOnAction clone() {
        return new TurnOnAction(this);
    }

    /**
     * Returns a string representation of the action.
     *
     * @return a formatted string with the action information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TurnOnAction { ")
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
