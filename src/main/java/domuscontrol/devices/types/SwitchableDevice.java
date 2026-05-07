package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;

/**
 * Represents devices that operate strictly in ON and OFF states (binary control).
 * Examples include plugs, relays, and other simple on/off devices.
 * These devices consume energy only when turned ON.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public abstract class SwitchableDevice extends Device {

    /**
     * Creates a new switchable device with default values.
     * The device is initialized as OFF.
     */
    public SwitchableDevice() {
        super();
    }

    /**
     * Creates a switchable device with specified brand, model, and power consumption.
     *
     * @param brand the brand of the device.
     * @param model the model of the device.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     */
    public SwitchableDevice(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
    }

    /**
     * Copy constructor for the switchable device.
     *
     * @param d the switchable device to copy.
     */
    public SwitchableDevice(SwitchableDevice d) {
        super(d);
    }

    /**
     * Turns the device ON.
     * Sets the device status to ON and increments activations if transitioning from OFF.
     */
    public void turnOn() {
        this.updateStatus(DeviceStatus.ON);
    }

    /**
     * Turns the device OFF.
     * Sets the device status to OFF.
     */
    public void turnOff() {
        this.updateStatus(DeviceStatus.OFF);
    }

    /**
     * Checks if the device is currently ON.
     *
     * @return true if the device status is ON, false otherwise.
     */
    public boolean isOn() {
        return this.getStatus() == DeviceStatus.ON;
    }

    /**
     * Determines if this device is currently consuming energy.
     * Switchable devices consume energy when in the ON state.
     *
     * @return true if the device status is ON, false otherwise.
     */
    @Override
    public boolean isConsuming() {
        return this.getStatus() == DeviceStatus.ON;
    }
}