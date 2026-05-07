package domuscontrol.devices.types;

import java.util.Objects;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;

/**
 * Represents devices that operate by opening and closing (e.g., Gates, Curtains, Blinds).
 * These devices have an opening level from 0% (fully closed) to 100% (fully open).
 * Opening level changes automatically update the device status (OPEN/CLOSED).
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public abstract class OpenableDevice extends Device {

    /**
     * The opening level (0-100).
     */
    private int openingLevel;

    /**
     * Creates a new openable device with default values.
     * The device is initialized as fully closed (opening level 0%).
     */
    public OpenableDevice() {
        super();
        this.openingLevel = 0;
        this.updateStatus(DeviceStatus.CLOSED);
    }

    /**
     * Creates an openable device with specified brand, model, consumption, and opening level.
     *
     * @param brand the brand of the device.
     * @param model the model of the device.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param openingLevel the initial opening percentage (0-100%).
     */
    public OpenableDevice(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour);
        this.setOpening(openingLevel);
    }

    /**
     * Copy constructor for the openable device.
     *
     * @param d the openable device to copy.
     */
    public OpenableDevice(OpenableDevice d) {
        super(d);
        this.openingLevel = d.getOpeningLevel();
    }

    /**
     * Sets the opening level of the device.
     * The value is clamped to the 0-100 range.
     * Setting opening > 0 sets status to OPEN; setting opening = 0 sets status to CLOSED.
     *
     * @param percentage the new opening percentage (0-100%).
     */
    public void setOpening(int percentage) {
        if (percentage < 0) {
            this.openingLevel = 0;
        } else if (percentage > 100) {
            this.openingLevel = 100;
        } else {
            this.openingLevel = percentage;
        }

        if (this.openingLevel > 0) {
            this.updateStatus(DeviceStatus.OPEN);
        } else {
            this.updateStatus(DeviceStatus.CLOSED);
        }
    }

    /**
     * Gets the current opening level of the device.
     *
     * @return the opening level (0-100%).
     */
    public int getOpeningLevel() {
        return this.openingLevel;
    }

    /**
     * Checks if the device is currently open.
     *
     * @return true if the opening level is greater than 0, false otherwise.
     */
    public boolean isOpen() {
        return this.openingLevel > 0;
    }

    /**
     * Determines if this device is currently consuming energy.
     * Openable devices consume energy when in the OPEN state.
     *
     * @return true if the device status is OPEN, false otherwise.
     */
    @Override
    public boolean isConsuming() {
        return this.getStatus() == DeviceStatus.OPEN;
    }

    /**
     * Checks if this openable device is equal to another object.
     *
     * @param o the object to compare with this device.
     * @return true if the given object is an openable device with the same properties, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        OpenableDevice d = (OpenableDevice) o;
        return super.equals(d) && this.openingLevel == d.getOpeningLevel();
    }

    /**
     * Calculates the hash code of this openable device.
     *
     * @return the hash code of this device.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.openingLevel);
    }

    /**
     * Creates a string representation of this openable device.
     *
     * @return a string representation including the opening percentage.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Opening: ").append(this.openingLevel).append("%\n");
        return sb.toString();
    }
}