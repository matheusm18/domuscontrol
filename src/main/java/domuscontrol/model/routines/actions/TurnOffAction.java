package domuscontrol.model.routines.actions;

import domuscontrol.model.routines.Action;
import domuscontrol.model.device.types.SwitchableDevice;
import java.util.Objects;

/**
 * Action responsible for switching off a specific switchable device.
 * This is used for binary state devices such as lamps, smart plugs, or appliances.
 */
public class TurnOffAction implements Action {
    private SwitchableDevice device;

    /**
     * Default constructor initializing the device to null.
     */
    public TurnOffAction() { 
        this.device = null; 
    }
    
    /**
     * Parameterized constructor.
     * @param device The live reference to the device to be turned off.
     */
    public TurnOffAction(SwitchableDevice device) { 
        this.device = device; 
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing TurnOffAction instance to copy.
     */
    public TurnOffAction(TurnOffAction other) { 
        this.device = other.getDevice(); 
    }

    /**
     * Retrieves the target device.
     * @return The switchable device reference.
     */
    public SwitchableDevice getDevice() { return device; }
    
    /**
     * Sets a new target device.
     * @param device The new switchable device reference.
     */
    public void setDevice(SwitchableDevice device) { this.device = device; }

    /**
     * Executes the action by interacting directly with the stored device reference.
     */
    @Override
    public void execute() {
        if (this.device != null) {
            this.device.turnOff();
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
        
        return Objects.equals(this.device, that.getDevice());
    }

    /**
     * Generates a hash code for this action.
     * @return The hash code based on the device reference.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device);
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
          .append("Device: ").append(this.device != null ? this.device.getId() : "null")
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