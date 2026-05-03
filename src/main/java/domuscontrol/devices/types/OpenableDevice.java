package domuscontrol.devices.types;

import java.util.Objects;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;

/**
 * Represents devices that operate by opening and closing (e.g., Gates, Blinds).
 */
public abstract class OpenableDevice extends Device {

    private int openingLevel;

    public OpenableDevice() {
        super();
        this.openingLevel = 0;
        this.updateStatus(DeviceStatus.CLOSED);
    }

    public OpenableDevice(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour);
        this.setOpening(openingLevel);
    }

    public OpenableDevice(OpenableDevice d) {
        super(d);
        this.openingLevel = d.getOpeningLevel();
    }

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

    public int getOpeningLevel() {
        return this.openingLevel;
    }

    public boolean isOpen() {
        return this.openingLevel > 0;
    }

    @Override
    public boolean isConsuming() {
        return this.getStatus() == DeviceStatus.OPEN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        
        OpenableDevice d = (OpenableDevice) o;
        return super.equals(d) && this.openingLevel == d.getOpeningLevel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.openingLevel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Opening: ").append(this.openingLevel).append("%\n");
        return sb.toString();
    }
}