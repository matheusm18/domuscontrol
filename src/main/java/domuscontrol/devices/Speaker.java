package domuscontrol.devices;

import java.util.Objects;

import domuscontrol.devices.types.AdjustableDevice;

/**
 * Represents a smart speaker device with adjustable volume and selectable audio sources.
 * Supports variable volume control from 0-100% and dynamic source selection (Spotify, Radio, Bluetooth, etc).
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Speaker extends AdjustableDevice {

    /**
     * The current audio source of the speaker (e.g., "Spotify", "Radio", "Bluetooth").
     */
    private String source;

    /** Creates a new speaker with default values. */
    public Speaker() {
        super();
        this.source = "";
    }

    /**
     * Creates a speaker with the specified values.
     *
     * @param brand the brand of the speaker.
     * @param model the model of the speaker.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param volume the initial volume level (0-100).
     * @param source the initial source of the speaker.
     */
    public Speaker(String brand, String model, double consumptionPerHour, int volume, String source) {
        super(brand, model, consumptionPerHour, volume);
        this.source = source;
    }

    /**
     * Copy constructor for the speaker.
     * 
     * @param speaker the speaker to copy.
     */
    public Speaker(Speaker speaker) {
        super(speaker);
        this.source = speaker.getSource();
    }

    /**
     * Gets the current audio source of the speaker.
     * 
     * @return This speaker's source.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the audio source of the speaker.
     * 
     * @param source The new source name.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the current volume of the speaker.
     * Uses the level inherited from AdjustableDevice.
     * 
     * @return This speaker's volume.
     */
    public int getVolume() {
        return this.getLevel();
    }

    /**
     * Sets the volume of the speaker. 
     * This keeps the value between 0 and 100 without changing the ON/OFF state.
     * 
     * @param volume The new volume level.
     */
    public void setVolume(int volume) {
        this.setLevel(volume);
    }

    /**
     * Checks if this speaker is equal to another object. 
     *
     * @param o The object to compare with this speaker.
     * @return true if the given object is a speaker with the same properties as this speaker, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
    
        if (o == null || this.getClass() != o.getClass()) return false;
    
        Speaker speaker = (Speaker) o;

        return super.equals(speaker) && Objects.equals(this.source, speaker.source);
    }

    /**
     * Calculates the hash code of this speaker. 
     * 
     * @return The hash code of this speaker.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source);
    }

    /**
     * Creates a copy of this speaker.
     * 
     * @return A copy of this speaker.
     */
    @Override
    public Speaker clone() {
        return new Speaker(this);
    }

    /**
     * Creates a string representation of this speaker.
     * Includes the base device information, the level (volume), and the source.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
            .append("Source: ").append(this.source).append("\n");
        return sb.toString();
    }
}
