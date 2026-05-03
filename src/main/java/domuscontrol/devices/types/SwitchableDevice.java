package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;

/**
 * Represents devices that operate strictly on ON and OFF states.
 */
public abstract class SwitchableDevice extends Device {

    public SwitchableDevice() {
        super();
    }

    public SwitchableDevice(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
    }

    public SwitchableDevice(SwitchableDevice d) {
        super(d);
    }

    public void turnOn() {
        this.updateStatus(DeviceStatus.ON);
    }

    public void turnOff() {
        this.updateStatus(DeviceStatus.OFF);
    }

    public boolean isOn() {
        return this.getStatus() == DeviceStatus.ON;
    }

    @Override
    public boolean isConsuming() {
        return this.getStatus() == DeviceStatus.ON;
    }
}