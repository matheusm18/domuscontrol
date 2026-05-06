package domuscontrol.routines.actions;

import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;

import java.util.Objects;

/**
 * Action that sets the color temperature of a color-adjustable device.
 */
public class SetColorTemperatureAction implements Action {

    private int deviceId;
    private int targetTemperature;

    /**
     * Creates an action with no target device and color temperature 2700K.
     */
    public SetColorTemperatureAction() {
        this.deviceId = -1;
        this.targetTemperature = 2700;
    }

    /**
     * Creates an action for the given device and target color temperature.
     *
     * @param deviceId the target device identifier
     * @param targetTemperature the color temperature in Kelvin to set on the device
     */
    public SetColorTemperatureAction(int deviceId, int targetTemperature) {
        this.deviceId = deviceId;
        this.targetTemperature = targetTemperature;
    }

    /**
     * Creates a copy of another set-color-temperature action.
     *
     * @param other the action to copy
     */
    public SetColorTemperatureAction(SetColorTemperatureAction other) {
        this.deviceId = other.getDeviceId();
        this.targetTemperature = other.getTargetTemperature();
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
     * Gets the target color temperature.
     *
     * @return the target temperature in Kelvin
     */
    public int getTargetTemperature() {
        return this.targetTemperature;
    }

    /**
     * Sets the target color temperature.
     *
     * @param targetTemperature the target temperature in Kelvin
     */
    public void setTargetTemperature(int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    /**
     * Executes this action in the given house.
     *
     * @param house the house where the target device color temperature should be changed
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
     * @return a copied SetColorTemperatureAction instance
     */
    @Override
    public Action copy() {
        return new SetColorTemperatureAction(this);
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

    /**
     * Compares this action with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the devices and target temperatures match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SetColorTemperatureAction action = (SetColorTemperatureAction) o;
        return this.targetTemperature == action.getTargetTemperature() &&
               this.deviceId == action.getDeviceId();
    }

    /**
     * Generates a hash code for this action.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.targetTemperature);
    }

    /**
     * Creates a copy of this action.
     *
     * @return a copied SetColorTemperatureAction instance
     */
    @Override
    public SetColorTemperatureAction clone() {
        return new SetColorTemperatureAction(this);
    }

    /**
     * Returns a string representation of this action.
     *
     * @return a formatted string with the action information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SetColorTemperatureAction { ")
          .append("Device ID: ").append(this.deviceId)
          .append(", Target Temperature: ").append(this.targetTemperature).append("K")
          .append(" }");
        return sb.toString();
    }
}
