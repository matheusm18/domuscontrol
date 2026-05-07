package domuscontrol.routines.actions;

import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action that sets the level of an adjustable device.
 * 
 * This action sets the level (brightness, volume, etc.) of any device implementing
 * the AdjustableDevice interface to a target level value. If the device is not found
 * in the house, the action fails silently.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class SetLevelAction implements Action {
    /**
     * The unique identifier of the target device whose level will be adjusted.
     * Value of -1 indicates no target device is set.
     */
    private int deviceId;
    /**
     * The target level value to set on the device.
     * The interpretation of this value depends on the specific device type.
     */
    private int targetLevel;

    /**
     * Creates an action with no target device and level 0.
     */
    public SetLevelAction() {
        this.deviceId = -1;
        this.targetLevel = 0;
    }
    
    /**
     * Creates an action for the given device and target level.
     *
     * @param deviceId the target device identifier
     * @param targetLevel the level to set on the device
     */
    public SetLevelAction(int deviceId, int targetLevel) {
        this.deviceId = deviceId;
        this.targetLevel = targetLevel;
    }
    
    /**
     * Creates a copy of another set-level action.
     *
     * @param other the action to copy
     */
    public SetLevelAction(SetLevelAction other) {
        this.deviceId = other.getDeviceId();
        this.targetLevel = other.getTargetLevel();
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
     * Gets the target level.
     *
     * @return the target level
     */
    public int getTargetLevel() {
        return this.targetLevel;
    }
    
    /**
     * Sets the target level.
     *
     * @param targetLevel the target level
     */
    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    /**
     * Executes this action in the given house.
     *
     * @param house the house where the target device level should be changed
     */
    @Override
    public void execute(House house) {
        try {
            house.interactWithDevice(this.deviceId, d -> {
                if (d instanceof AdjustableDevice ad) {
                    ad.setLevel(this.targetLevel);
                }
            });
        } catch (DeviceNotFoundException e) {
            // Routine action failed because the device no longer exists.
        }
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied SetLevelAction instance
     */
    @Override
    public Action copy() {
        return new SetLevelAction(this);
    }

    /**
     * Compares this action with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the devices and levels match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        SetLevelAction action = (SetLevelAction) o;
        
        return this.deviceId == action.getDeviceId() &&
               this.targetLevel == action.getTargetLevel();
    }

    /**
     * Generates a hash code for this action.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetLevel);
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied SetLevelAction instance
     */
    @Override
    public SetLevelAction clone() {
        return new SetLevelAction(this);
    }

    /**
     * Returns a string representation of the action.
     *
     * @return a formatted string with the action information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SetLevelAction { ")
          .append("Device ID: ").append(this.deviceId)
          .append(", Target Level: ").append(this.targetLevel)
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
