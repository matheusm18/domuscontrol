package domuscontrol.routines.actions;

import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action responsible for switching on a specific switchable device.
 * This is used for devices that support a simple power-on state, such as lights or smart plugs.
 */
public class TurnOnAction implements Action {
    private int deviceId;

    /**
     * Default constructor initializing the device to null.
     */
    public TurnOnAction() { 
        this.deviceId = -1; 
    }

    /**
     * Parameterized constructor.
     * @param device The live reference to the device to be turned on.
     */
    public TurnOnAction(int deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing TurnOnAction instance to copy.
     */
    public TurnOnAction(TurnOnAction other) { 
        this.deviceId = other.getDeviceId(); 
    }
    
    /**
     * Retrieves the target device.
     * @return The switchable device reference.
     */
    public int getDeviceId() {
        return this.deviceId;
    }

    /**
     * Sets a new target device.
     * @param device The new switchable device reference.
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Executes the action by interacting directly with the stored device reference.
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
     * @return A new instance of TurnOnAction.
     */
    @Override
    public Action copy() {
        return new TurnOnAction(this);
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
        
        TurnOnAction that = (TurnOnAction) o;
        
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
     * @return A cloned TurnOnAction.
     */
    @Override
    public TurnOnAction clone() {
        return new TurnOnAction(this);
    }

    /**
     * Returns a string representation of the action.
     * @return Formatted string containing the target device ID.
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
