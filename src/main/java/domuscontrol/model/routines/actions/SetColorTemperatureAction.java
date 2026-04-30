package domuscontrol.model.routines.actions;

import domuscontrol.model.device.types.ColorAdjustableDevice;
import domuscontrol.model.routines.Action;
import java.util.Objects;

/**
 * Action responsible for setting the color temperature of a color-adjustable device.
 * Typically used for smart lamps that support light warmth configuration.
 */
public class SetColorTemperatureAction implements Action {

    private ColorAdjustableDevice device;
    private int targetTemperature;

    /**
     * Default constructor initializing device to null and temperature to 2700K.
     */
    public SetColorTemperatureAction() {
        this.device = null;
        this.targetTemperature = 2700;
    }

    /**
     * Parameterized constructor.
     *
     * @param device            The live reference to the color-adjustable device.
     * @param targetTemperature The color temperature in Kelvin to be set.
     */
    public SetColorTemperatureAction(ColorAdjustableDevice device, int targetTemperature) {
        this.device = device;
        this.targetTemperature = targetTemperature;
    }

    /**
     * Copy constructor. The device pointer remains shared.
     *
     * @param other The existing SetColorTemperatureAction instance to copy.
     */
    public SetColorTemperatureAction(SetColorTemperatureAction other) {
        this.device = other.device;
        this.targetTemperature = other.targetTemperature;
    }

    /**
     * Returns the target device.
     *
     * @return The color-adjustable device reference.
     */
    public ColorAdjustableDevice getDevice() { return device; }

    /**
     * Sets a new target device.
     *
     * @param device The new color-adjustable device reference.
     */
    public void setDevice(ColorAdjustableDevice device) { this.device = device; }

    /**
     * Returns the target color temperature.
     *
     * @return The target temperature in Kelvin.
     */
    public int getTargetTemperature() { return targetTemperature; }

    /**
     * Sets a new target color temperature.
     *
     * @param targetTemperature The new temperature value in Kelvin.
     */
    public void setTargetTemperature(int targetTemperature) { this.targetTemperature = targetTemperature; }

    /**
     * Executes the action by setting the color temperature on the stored device reference.
     */
    @Override
    public void execute() {
        if (this.device != null)
            this.device.setColorTemperature(this.targetTemperature);
    }

    /**
     * Creates a copy of this action.
     *
     * @return A new instance of SetColorTemperatureAction.
     */
    @Override
    public Action copy() {
        return new SetColorTemperatureAction(this);
    }

    /**
     * Checks if this action is associated with a specific device ID.
     *
     * @param deviceId The ID to check.
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.device != null && this.device.getId() == deviceId;
    }

    /**
     * Compares this action with another object for equality.
     *
     * @param o The object to compare with.
     * @return true if the devices and target temperatures match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SetColorTemperatureAction that = (SetColorTemperatureAction) o;
        return this.targetTemperature == that.targetTemperature &&
               Objects.equals(this.device, that.device);
    }

    /**
     * Generates a hash code for this action.
     *
     * @return The hash code based on the device and target temperature.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.targetTemperature);
    }

    /**
     * Clones this action instance.
     *
     * @return A cloned SetColorTemperatureAction.
     */
    @Override
    public SetColorTemperatureAction clone() {
        return new SetColorTemperatureAction(this);
    }

    /**
     * Returns a string representation of this action.
     *
     * @return Formatted string containing the device ID and target temperature.
     */
    @Override
    public String toString() {
        return "SetColorTemperatureAction { Device: " +
               (this.device != null ? this.device.getId() : "null") +
               ", Target Temperature: " + this.targetTemperature + "K }";
    }
}