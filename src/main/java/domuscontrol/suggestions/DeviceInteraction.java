package domuscontrol.suggestions;

import domuscontrol.simulation.Simulation.WeatherCondition;

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

    /** The outside weather when this interaction occurred. */
    private WeatherCondition weather;

    /** The outside temperature when this interaction occurred. */
    private Double outsideTemperature;

    /** The outside luminosity when this interaction occurred. */
    private Double luminosity;

    /**
     * Default constructor initializing all fields to safe defaults.
     */
    public DeviceInteraction() {
        this.deviceId  = -1;
        this.type      = null;
        this.value     = null;
        this.userId    = -1;
        this.timestamp = null;
        this.weather   = null;
        this.outsideTemperature = null;
        this.luminosity = null;
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
        this.weather   = null;
        this.outsideTemperature = null;
        this.luminosity = null;
    }

    public DeviceInteraction(int deviceId, InteractionType type, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature) {
        this(deviceId, type, userId, timestamp, weather, outsideTemperature, null);
    }

    public DeviceInteraction(int deviceId, InteractionType type, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature, Double luminosity) {
        this.deviceId  = deviceId;
        this.type      = type;
        this.value     = null;
        this.userId    = userId;
        this.timestamp = timestamp;
        this.weather   = weather;
        this.outsideTemperature = outsideTemperature;
        this.luminosity = luminosity;
    }

    public DeviceInteraction(int deviceId, InteractionType type, Double value, int userId, LocalDateTime timestamp) {
        this.deviceId  = deviceId;
        this.type      = type;
        this.value     = value;
        this.userId    = userId;
        this.timestamp = timestamp;
        this.weather   = null;
        this.outsideTemperature = null;
        this.luminosity = null;
    }

    public DeviceInteraction(int deviceId, InteractionType type, Double value, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature) {
        this(deviceId, type, value, userId, timestamp, weather, outsideTemperature, null);
    }

    public DeviceInteraction(int deviceId, InteractionType type, Double value, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature, Double luminosity) {
        this.deviceId  = deviceId;
        this.type      = type;
        this.value     = value;
        this.userId    = userId;
        this.timestamp = timestamp;
        this.weather   = weather;
        this.outsideTemperature = outsideTemperature;
        this.luminosity = luminosity;
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
        this.weather   = other.getWeather();
        this.outsideTemperature = other.getOutsideTemperature();
        this.luminosity = other.getLuminosity();
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
     * Returns the outside weather recorded when the interaction happened.
     *
     * @return The weather condition, or null for legacy interactions.
     */
    public WeatherCondition getWeather() { return weather; }

    /**
     * Sets the outside weather associated with this interaction.
     *
     * @param weather The weather condition.
     */
    public void setWeather(WeatherCondition weather) { this.weather = weather; }

    /**
     * Returns the outside temperature recorded when the interaction happened.
     *
     * @return The temperature in Celsius, or null for legacy interactions.
     */
    public Double getOutsideTemperature() { return outsideTemperature; }

    /**
     * Sets the outside temperature associated with this interaction.
     *
     * @param outsideTemperature The temperature in Celsius.
     */
    public void setOutsideTemperature(Double outsideTemperature) { this.outsideTemperature = outsideTemperature; }

    /**
     * Returns the outside luminosity recorded when the interaction happened.
     *
     * @return The luminosity in lux, or null for legacy interactions.
     */
    public Double getLuminosity() { return luminosity; }

    /**
     * Sets the outside luminosity associated with this interaction.
     *
     * @param luminosity The luminosity in lux.
     */
    public void setLuminosity(Double luminosity) { this.luminosity = luminosity; }

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
               Objects.equals(this.getTimestamp(), that.getTimestamp()) &&
               this.getWeather() == that.getWeather() &&
               Objects.equals(this.getOutsideTemperature(), that.getOutsideTemperature()) &&
               Objects.equals(this.getLuminosity(), that.getLuminosity());
    }

    /**
     * Generates a hash code for this interaction.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceId(), this.getUserId(), this.getType(), this.getValue(), this.getTimestamp(),
                this.getWeather(), this.getOutsideTemperature(), this.getLuminosity());
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
               ", timestamp=" + this.getTimestamp() +
               ", weather=" + (this.getWeather() != null ? this.getWeather() : "N/A") +
               ", outsideTemperature=" + (this.getOutsideTemperature() != null ? this.getOutsideTemperature() : "N/A") +
               ", luminosity=" + (this.getLuminosity() != null ? this.getLuminosity() : "N/A") + " }";
    }
}
