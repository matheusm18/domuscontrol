package domuscontrol.routines.actions;

import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action responsible for setting the opening percentage of an openable device.
 * Typically used for devices like motorized blinds, curtains, or gates.
 */
public class SetOpeningAction implements Action {
    private int deviceId;
    private int targetPercentage; 

    /**
     * Default constructor initializing device to null and percentage to zero.
     */
    public SetOpeningAction() {
        this.deviceId = -1;
        this.targetPercentage = 0;
    }
    
    /**
     * Parameterized constructor.
     * @param device           The live reference to the openable device.
     * @param targetPercentage The percentage of opening to be set (typically 0-100).
     */
    public SetOpeningAction(int deviceId, int targetPercentage) {
        this.deviceId = deviceId;
        this.targetPercentage = targetPercentage;
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing SetOpeningAction instance to copy.
     */
    public SetOpeningAction(SetOpeningAction other) {
        this.deviceId = other.getDeviceId();
        this.targetPercentage = other.getTargetPercentage();
    }

    /**
     * Retrieves the target device.
     * @return The openable device reference.
     */
    public int getDeviceId() { return deviceId; }
    
    /**
     * Sets a new target device.
     * @param device The new openable device reference.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    /**
     * Retrieves the target opening percentage.
     * @return The opening percentage.
     */
    public int getTargetPercentage() { return targetPercentage; }
    
    /**
     * Sets a new target opening percentage.
     * @param targetPercentage The new percentage value.
     */
    public void setTargetPercentage(int targetPercentage) { this.targetPercentage = targetPercentage; }

    /**
     * Executes the action by interacting directly with the stored device reference.
     */
    @Override
    public void execute(House house) {
        try {
            house.interactWithDevice(this.deviceId, d -> {
                if (d instanceof OpenableDevice od) {
                    od.setOpening(this.targetPercentage);
                }
            });
        } catch (DeviceNotFoundException e) {
            // Routine action failed because the device no longer exists.
        }
    }

    /**
     * Creates a copy of this action.
     * @return A new instance of SetOpeningAction.
     */
    @Override
    public Action copy() {
        return new SetOpeningAction(this);
    }

    /**
     * Compares this action with another object for equality.
     * @param o The object to compare with.
     * @return true if the devices and percentages match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        SetOpeningAction that = (SetOpeningAction) o;
        
        return this.deviceId == that.getDeviceId() && 
               this.targetPercentage == that.getTargetPercentage();
    }

    /**
     * Generates a hash code for this action.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetPercentage);
    }

    /**
     * Clones the current action instance.
     * @return A cloned SetOpeningAction.
     */
    @Override
    public SetOpeningAction clone() {
        return new SetOpeningAction(this);
    }

    /**
     * Returns a string representation of the action.
     * @return Formatted string containing device info and percentage.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SetOpeningAction { ")
          .append("Device ID: ").append(this.deviceId)
          .append(", Target Percentage: ").append(this.targetPercentage)
          .append(" }");
        return sb.toString();
    }

    /**
     * Checks if this action is associated with a specific device ID.
     * Used by the House/RoutineManager to clean up routines when a device is deleted.
     * @param deviceId The ID to check.
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override 
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }
}
