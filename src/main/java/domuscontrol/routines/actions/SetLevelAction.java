package domuscontrol.routines.actions;

import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action responsible for setting a specific level on an adjustable device.
 * This can be used for dimming lights, setting volume on speakers, or adjusting temperature.
 */
public class SetLevelAction implements Action {
    private int deviceId;
    private int targetLevel; 

    /**
     * Default constructor initializing device to null and level to zero.
     */
    public SetLevelAction() { 
        this.deviceId = -1; 
        this.targetLevel = 0; 
    }
    
    /**
     * Parameterized constructor.
     * @param device      The live reference to the adjustable device.
     * @param targetLevel The numerical value to be set on the device.
     */
    public SetLevelAction(int deviceId, int targetLevel) {
        this.deviceId = deviceId;
        this.targetLevel = targetLevel;
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing SetLevelAction instance to copy.
     */
    public SetLevelAction(SetLevelAction other) {
        this.deviceId = other.getDeviceId();
        this.targetLevel = other.getTargetLevel();
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
     * Retrieves the target level value.
     * @return The level value.
     */
    public int getTargetLevel() { return targetLevel; }
    
    /**
     * Sets a new target level value.
     * @param targetLevel The new level to be applied.
     */
    public void setTargetLevel(int targetLevel) { this.targetLevel = targetLevel; }

    /**
     * Executes the action by interacting directly with the stored device reference.
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
     * @return A new instance of SetLevelAction.
     */
    @Override
    public Action copy() {
        return new SetLevelAction(this);
    }

    /**
     * Compares this action with another object for equality.
     * @param o The object to compare with.
     * @return true if the devices and levels match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        SetLevelAction that = (SetLevelAction) o;
        
        return this.deviceId == that.getDeviceId() && 
               this.targetLevel == that.targetLevel;
    }

    /**
     * Generates a hash code for this action.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetLevel);
    }

    /**
     * Clones the current action instance.
     * @return A cloned SetLevelAction.
     */
    @Override
    public SetLevelAction clone() {
        return new SetLevelAction(this);
    }

    /**
     * Returns a string representation of the action.
     * @return Formatted string containing device info and level.
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
     * Checks if this action is associated with a specific device ID.
     * Used by the House/RoutineManager to clean up routines when a device is deleted.
     */
    @Override 
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }
}
