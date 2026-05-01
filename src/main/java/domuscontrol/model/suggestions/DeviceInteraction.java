package domuscontrol.model.suggestions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single manual interaction performed on a device inside a house.
 * Stores what happened, on which device, when it happened, and the value applied if applicable.
 */
public class DeviceInteraction implements Serializable {

    /** The ID of the device that was interacted with. */
    private int deviceId;

    /** The type of interaction performed. */
    private InteractionType type;

    /**
     * The value applied during the interaction, if applicable.
     * Null for TURN_ON and TURN_OFF. Holds level, opening, or temperature for the others.
     */
    private Double value;

    /** The ID of the user who performed this interaction. */
    private int userId;

    /** The simulation timestamp when this interaction occurred. */
    private LocalDateTime timestamp;

    /**
     * Default constructor initializing all fields to safe defaults.
     */
    public DeviceInteraction() {
        this.deviceId  = -1;
        this.type      = null;
        this.value     = null;
        this.userId    = -1;
        this.timestamp = null;
    }

    /**
     * Parameterized constructor for interactions without a value (TURN_ON, TURN_OFF).
     *
     * @param deviceId  The ID of the device.
     * @param type      The type of interaction.
     * @param timestamp The simulation time of the interaction.
     */
    public DeviceInteraction(int deviceId, InteractionType type, int userId, LocalDateTime timestamp) {
        this.deviceId  = deviceId;
        this.type      = type;
        this.value     = null;
        this.userId    = userId;
        this.timestamp = timestamp;
    }

    public DeviceInteraction(int deviceId, InteractionType type, Double value, int userId, LocalDateTime timestamp) {
        this.deviceId  = deviceId;
        this.type      = type;
        this.value     = value;
        this.userId    = userId;
        this.timestamp = timestamp;
    }

    /**
     * Copy constructor using getters to access the other instance's state.
     *
     * @param other The existing DeviceInteraction instance to copy.
     */
    public DeviceInteraction(DeviceInteraction other) {
        this.deviceId  = other.getDeviceId();
        this.type      = other.getType();
        this.value     = other.getValue();
        this.userId    = other.getUserId();
        this.timestamp = other.getTimestamp();
    }

    /**
     * Returns the ID of the device that was interacted with.
     *
     * @return The device ID.
     */
    public int getDeviceId() { return deviceId; }

    public int getUserId() { return userId; }

    public void setUserId(int userId) { this.userId = userId; }

    /**
     * Sets the device ID.
     *
     * @param deviceId The new device ID.
     */
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    /**
     * Returns the type of interaction performed.
     *
     * @return The InteractionType.
     */
    public InteractionType getType() { return type; }

    /**
     * Sets the interaction type.
     *
     * @param type The new InteractionType.
     */
    public void setType(InteractionType type) { this.type = type; }

    /**
     * Returns the value applied during the interaction, or null if not applicable.
     *
     * @return The value, or null.
     */
    public Double getValue() { return value; }

    /**
     * Sets the value applied during the interaction.
     *
     * @param value The new value, or null if not applicable.
     */
    public void setValue(Double value) { this.value = value; }

    /**
     * Returns the simulation timestamp of the interaction.
     *
     * @return The LocalDateTime.
     */
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Sets the simulation timestamp.
     *
     * @param timestamp The new LocalDateTime.
     */
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /**
     * Creates a deep copy of this interaction.
     *
     * @return A new DeviceInteraction instance.
     */
    @Override
    public DeviceInteraction clone() {
        return new DeviceInteraction(this);
    }

    /**
     * Compares this interaction with another object for equality using getters.
     *
     * @param o The object to compare with.
     * @return true if all fields match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DeviceInteraction that = (DeviceInteraction) o;
        return this.getDeviceId() == that.getDeviceId() &&
               this.getUserId()   == that.getUserId()   &&
               this.getType()     == that.getType()     &&
               Objects.equals(this.getValue(), that.getValue()) &&
               Objects.equals(this.getTimestamp(), that.getTimestamp());
    }

    /**
     * Generates a hash code for this interaction.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceId(), this.getUserId(), this.getType(), this.getValue(), this.getTimestamp());
    }

    /**
     * Returns a string representation of this interaction.
     *
     * @return Formatted string with all fields.
     */
    @Override
    public String toString() {
        return "DeviceInteraction { deviceId=" + this.getDeviceId() +
               ", userId=" + this.getUserId() +
               ", type=" + this.getType() +
               ", value=" + (this.getValue() != null ? this.getValue() : "N/A") +
               ", timestamp=" + this.getTimestamp() + " }";
    }
}