package domuscontrol.routines.actions;

import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action responsible for setting the color temperature of a color-adjustable device.
 * Typically used for smart lamps that support light warmth configuration.
 */
public class SetColorTemperatureAction implements Action {

    private int deviceId;
    private int targetTemperature;

    /**
     * Default constructor initializing device to null and temperature to 2700K.
     */
    public SetColorTemperatureAction() {
        this.deviceId = -1;
        this.targetTemperature = 2700;
    }

    /**
     * Parameterized constructor.
     *
     * @param device            The live reference to the color-adjustable device.
     * @param targetTemperature The color temperature in Kelvin to be set.
     */
    public SetColorTemperatureAction(int deviceId, int targetTemperature) {
        this.deviceId = deviceId;
        this.targetTemperature = targetTemperature;
    }

    /**
     * Copy constructor. The device pointer remains shared.
     *
     * @param other The existing SetColorTemperatureAction instance to copy.
     */
    public SetColorTemperatureAction(SetColorTemperatureAction other) {
        this.deviceId = other.deviceId;
        this.targetTemperature = other.targetTemperature;
    }

    /**
     * Returns the target device.
     *
     * @return The color-adjustable device reference.
     */
    public int getDeviceId() { return deviceId; }

    /**
     * Sets a new target device.
     *
     * @param device The new color-adjustable device reference.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

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
    public void execute(House house) {
        try {
            house.interactWithDevice(this.deviceId, d -> {
                if (d instanceof ColorAdjustableDevice cad) {
                    cad.setColorTemperature(this.targetTemperature);
                }
            });
        } catch (DeviceNotFoundException e) {
            // Routine action failed because the device no longer exists.
        }
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
        return this.deviceId == deviceId;
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
               this.deviceId == that.deviceId;
    }

    /**
     * Generates a hash code for this action.
     *
     * @return The hash code based on the device and target temperature.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetTemperature);
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
               this.deviceId +
               ", Target Temperature: " + this.targetTemperature + "K }";
    }
}
