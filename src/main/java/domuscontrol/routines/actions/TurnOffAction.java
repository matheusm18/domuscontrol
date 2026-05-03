package domuscontrol.routines.actions;

import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action responsible for switching off a specific switchable device.
 * This is used for binary state devices such as lamps, smart plugs, or appliances.
 */
public class TurnOffAction implements Action {
    private int deviceId;

    /**
     * Default constructor initializing the device to null.
     */
    public TurnOffAction() { 
        this.deviceId = -1; 
    }
    
    /**
     * Parameterized constructor.
     * @param device The live reference to the device to be turned off.
     */
    public TurnOffAction(int deviceId) { 
        this.deviceId = deviceId; 
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing TurnOffAction instance to copy.
     */
    public TurnOffAction(TurnOffAction other) { 
        this.deviceId = other.getDeviceId(); 
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
     * Executes the action by interacting directly with the stored device reference.
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
     * @return A new instance of TurnOffAction.
     */
    @Override
    public Action copy() {
        return new TurnOffAction(this);
    }

    /**
     * Compares this action with another object for equality.
     * @param o The object to compare with.
     * @return true if the devices match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        TurnOffAction that = (TurnOffAction) o;
        
        return this.deviceId == that.getDeviceId();
    }

    /**
     * Generates a hash code for this action.
     * @return The hash code based on the device reference.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId);
    }

    /**
     * Clones the current action instance.
     * @return A cloned TurnOffAction.
     */
    @Override
    public TurnOffAction clone() {
        return new TurnOffAction(this);
    }

    /**
     * Returns a string representation of the action.
     * @return Formatted string containing the target device ID.
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
