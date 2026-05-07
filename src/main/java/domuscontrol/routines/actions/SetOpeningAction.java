package domuscontrol.routines.actions;

import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action that sets the opening percentage of an openable device.
 * 
 * This action sets the opening percentage (0-100) of any device implementing
 * the OpenableDevice interface, such as curtains, blinds, or gates. If the device is
 * not found in the house, the action fails silently.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class SetOpeningAction implements Action {
    /**
     * The unique identifier of the target device to open or close.
     * Value of -1 indicates no target device is set.
     */
    private int deviceId;
    /**
     * The target opening percentage (0-100) to set on the device.
     * 0 represents fully closed, 100 represents fully open.
     */
    private int targetPercentage;

    /**
     * Creates an action with no target device and opening percentage 0.
     */
    public SetOpeningAction() {
        this.deviceId = -1;
        this.targetPercentage = 0;
    }
    
    /**
     * Creates an action for the given device and target opening percentage.
     *
     * @param deviceId the target device identifier
     * @param targetPercentage the opening percentage to set on the device
     */
    public SetOpeningAction(int deviceId, int targetPercentage) {
        this.deviceId = deviceId;
        this.targetPercentage = targetPercentage;
    }
    
    /**
     * Creates a copy of another set-opening action.
     *
     * @param other the action to copy
     */
    public SetOpeningAction(SetOpeningAction other) {
        this.deviceId = other.getDeviceId();
        this.targetPercentage = other.getTargetPercentage();
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
     * Gets the target opening percentage.
     *
     * @return the target opening percentage
     */
    public int getTargetPercentage() {
        return this.targetPercentage;
    }
    
    /**
     * Sets the target opening percentage.
     *
     * @param targetPercentage the target opening percentage
     */
    public void setTargetPercentage(int targetPercentage) {
        this.targetPercentage = targetPercentage;
    }

    /**
     * Executes this action in the given house.
     *
     * @param house the house where the target device opening should be changed
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
     *
     * @return a copied SetOpeningAction instance
     */
    @Override
    public Action copy() {
        return new SetOpeningAction(this);
    }

    /**
     * Compares this action with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the devices and percentages match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        SetOpeningAction action = (SetOpeningAction) o;
        
        return this.deviceId == action.getDeviceId() &&
               this.targetPercentage == action.getTargetPercentage();
    }

    /**
     * Generates a hash code for this action.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetPercentage);
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied SetOpeningAction instance
     */
    @Override
    public SetOpeningAction clone() {
        return new SetOpeningAction(this);
    }

    /**
     * Returns a string representation of the action.
     *
     * @return a formatted string with the action information
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
