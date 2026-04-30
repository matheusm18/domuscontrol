package domuscontrol.model.routines.actions;

import domuscontrol.model.routines.Action;
import domuscontrol.model.device.types.OpenableDevice;
import java.util.Objects;

/**
 * Action responsible for setting the opening percentage of an openable device.
 * Typically used for devices like motorized blinds, curtains, or gates.
 */
public class SetOpeningAction implements Action {
    private OpenableDevice device;
    private int targetPercentage; 

    /**
     * Default constructor initializing device to null and percentage to zero.
     */
    public SetOpeningAction() {
        this.device = null;
        this.targetPercentage = 0;
    }
    
    /**
     * Parameterized constructor.
     * @param device           The live reference to the openable device.
     * @param targetPercentage The percentage of opening to be set (typically 0-100).
     */
    public SetOpeningAction(OpenableDevice device, int targetPercentage) {
        this.device = device;
        this.targetPercentage = targetPercentage;
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing SetOpeningAction instance to copy.
     */
    public SetOpeningAction(SetOpeningAction other) {
        this.device = other.getDevice();
        this.targetPercentage = other.getTargetPercentage();
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
    public void execute() {
        if (this.device != null) {
            this.device.setOpening(this.targetPercentage);
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
        
        return Objects.equals(this.device, that.getDevice()) && 
               this.targetPercentage == that.getTargetPercentage();
    }

    /**
     * Generates a hash code for this action.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.targetPercentage);
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
          .append("Device: ").append(this.device != null ? this.device.getId() : "null")
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
        return this.device != null && this.device.getId() == deviceId;
    }
}