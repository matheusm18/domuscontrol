package domuscontrol.model.routines.actions;

import domuscontrol.model.routines.Action;
import domuscontrol.model.device.types.SwitchableDevice;
import java.util.Objects;

/**
 * Action responsible for switching on a specific switchable device.
 * This is used for devices that support a simple power-on state, such as lights or smart plugs.
 */
public class TurnOnAction implements Action {
    private SwitchableDevice device;

    /**
     * Default constructor initializing the device to null.
     */
    public TurnOnAction() { 
        this.device = null; 
    }

    /**
     * Parameterized constructor.
     * @param device The live reference to the device to be turned on.
     */
    public TurnOnAction(SwitchableDevice device) {
        this.device = device;
    }
    
    /**
     * Copy constructor for deep copying the action itself.
     * Note: The device pointer remains shared.
     * @param other The existing TurnOnAction instance to copy.
     */
    public TurnOnAction(TurnOnAction other) { 
        this.device = other.getDevice(); 
    }
    
    /**
     * Retrieves the target device.
     * @return The switchable device reference.
     */
    public SwitchableDevice getDevice() {
        return this.device;
    }

    /**
     * Sets a new target device.
     * @param device The new switchable device reference.
     */
    public void setDevice(SwitchableDevice device) {
        this.device = device;
    }

    /**
     * Executes the action by interacting directly with the stored device reference.
     */
    @Override
    public void execute() {
        if (this.device != null) {
            this.device.turnOn();
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