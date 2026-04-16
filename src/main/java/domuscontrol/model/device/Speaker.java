package domuscontrol.model.device;

import java.util.Objects;

/**
 * Represents a speaker device.
 */
public class Speaker extends Device {

    /** The maximum allowed volume. */
    private static final int MAX_VOLUME = 100;

    /** The minimum allowed volume. */
    private static final int MIN_VOLUME = 0;

    /** The current volume level of the speaker. */
    private int volume;

    /** The current source of the speaker. */
    private String source;

    /** Creates a new speaker with default values. */
    public Speaker() {
        super();
        this.volume = 50;
        this.source = "";
    }

    /**
     * Creates a speaker with the specified values.
     * 
     * @param brand the brand of the speaker.
     * @param model the model of the speaker.
     * @param consumptionPerHour the power consumption rate in Wh/h.
     * @param source the initial source of the speaker.
     */
    public Speaker(String brand, String model, double consumptionPerHour, String source) {
        super(brand, model, consumptionPerHour);
        this.volume = 50;
        this.source = source;
    }

    /**
     * Copy constructor for the speaker.
     * 
     * @param speaker the speaker to copy.
     */
    public Speaker(Speaker speaker) {
        super(speaker);
        this.volume = speaker.getVolume();
        this.source = speaker.getSource();
    }

    /**
     * Gets the current volume of the speaker.
     * 
     * @return This speaker's volume.
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Sets the volume of the speaker. The value is fixed between MIN_VOLUME and MAX_VOLUME.
     * 
     * @param volume The new volume level.
     */
    public void setVolume(int volume) {
        if (volume > MAX_VOLUME) {
            this.volume = MAX_VOLUME;
        } else if (volume < MIN_VOLUME) {
            this.volume = MIN_VOLUME;
        } else {
            this.volume = volume;
        }

        if (this.volume > MIN_VOLUME) {
            this.turnOn();
        } else {
            this.turnOff();
        }
    }

    /**
     * Gets the current source of the speaker.
     * 
     * @return This speaker's source.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source of the speaker.
     * 
     * @param source The new source name.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Checks if this speaker is equal to another object. 
     * 
     * @param obj The object to compare with this speaker.
     * @return true if the given object is a speaker with the same properties as this speaker, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)return true;
    
        if (obj == null || this.getClass() != obj.getClass())return false;
    
        Speaker s = (Speaker) obj;

        return super.equals(s) && this.volume == s.volume &&
                Objects.equals(this.source, s.source);
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
     * Calculates the hash code of this speaker. Combines the hash code of the base device with the volume and source.
     * 
     * @return The hash code of this speaker.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), volume, source);
    }

    /**
     * Creates a string representation of this speaker.
     * Includes the base device information plus volume and source.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
            .append("Volume: ").append(this.volume).append("\n")
            .append("Source: ").append(this.source).append("\n");
        return sb.toString();
    }
}