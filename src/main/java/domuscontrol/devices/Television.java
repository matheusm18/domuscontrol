package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a television device.
 * The adjustable level represents volume from 0% to 100%.
 */
public class Television extends AdjustableDevice {

    private String source;

    public Television() {
        super();
        this.source = "";
    }

    public Television(String brand, String model, double consumptionPerHour, int volume, String source) {
        super(brand, model, consumptionPerHour, volume);
        this.source = source;
    }

    public Television(Television television) {
        super(television);
        this.source = television.getSource();
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getVolume() {
        return this.getLevel();
    }

    public void setVolume(int volume) {
        this.setLevel(volume);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Television television = (Television) o;
        return super.equals(television) && Objects.equals(this.source, television.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.source);
    }

    @Override
    public Television clone() {
        return new Television(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Source: ").append(this.source).append("\n");
        return sb.toString();
    }
}
