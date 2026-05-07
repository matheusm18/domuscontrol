package domuscontrol.devices;

import domuscontrol.devices.types.AdjustableDevice;

import java.util.Objects;

/**
 * Represents a smart television device with adjustable volume and selectable video sources.
 * Provides variable volume control from 0-100% and dynamic video source selection (HDMI, Cable, Streaming, etc).
 * The device maintains synchronization between volume level and ON/OFF state.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Television extends AdjustableDevice {

    /**
     * The current video source of the television (e.g., "HDMI", "Cable", "Streaming").
     */
    private String source;

    /**
     * Default constructor for the Television class.
     * Initializes a new television with default values and empty source.
     */
    public Television() {
        super();
        this.source = "";
    }

    /**
     * Parameterized constructor for the Television class.
     *
     * @param brand The brand of the television.
     * @param model The model of the television.
     * @param consumptionPerHour The power consumption of the television in Wh/h.
     * @param volume The initial volume level (0-100).
     * @param source The initial video source name.
     */
    public Television(String brand, String model, double consumptionPerHour, int volume, String source) {
        super(brand, model, consumptionPerHour, volume);
        this.source = source;
    }

    /**
     * Copy constructor for the Television class.
     *
     * @param television The Television instance to copy.
     */
    public Television(Television television) {
        super(television);
        this.source = television.getSource();
    }

    /**
     * Gets the current video source of the television.
     *
     * @return This television's video source.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Sets the video source of the television.
     * Common sources include HDMI, Cable, Streaming, etc.
     *
     * @param source The new source name.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the current volume of the television.
     * Uses the level inherited from AdjustableDevice.
     *
     * @return This television's volume level (0-100).
     */
    public int getVolume() {
        return this.getLevel();
    }

    /**
     * Sets the volume of the television.
     * The value is constrained between 0 and 100.
     *
     * @param volume The new volume level (0-100).
     */
    public void setVolume(int volume) {
        this.setLevel(volume);
    }

    /**
     * Checks if this television is equal to another object.
     *
     * @param o The object to compare with this television.
     * @return true if the given object is a television with the same properties as this television, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Television television = (Television) o;
        return super.equals(television) && Objects.equals(this.source, television.source);
    }

    /**
     * Calculates the hash code of this television.
     *
     * @return The hash code of this television.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.source);
    }

    /**
     * Creates a copy of this television.
     *
     * @return A copy of this television.
     */
    @Override
    public Television clone() {
        return new Television(this);
    }

    /**
     * Creates a string representation of this television.
     * Includes the base device information, volume level, and current video source.
     *
     * @return A string representing this television.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append("Source: ").append(this.source).append("\n");
        return sb.toString();
    }
}
