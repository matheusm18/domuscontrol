package domuscontrol.suggestions;

import domuscontrol.simulation.WeatherCondition;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single manual interaction performed on a device inside a house.
 * Stores what happened, who performed it, when it happened, and the simulation context.
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
     * Creates an empty interaction.
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
     * Creates an interaction without an associated value.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
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

    /**
     * Creates an interaction without an associated value and with environmental context.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
     * @param weather the weather at the interaction time
     * @param outsideTemperature the outside temperature at the interaction time
     */
    public DeviceInteraction(int deviceId, InteractionType type, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature) {
        this(deviceId, type, userId, timestamp, weather, outsideTemperature, null);
    }

    /**
     * Creates an interaction without an associated value and with full environmental context.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
     * @param weather the weather at the interaction time
     * @param outsideTemperature the outside temperature at the interaction time
     * @param luminosity the luminosity at the interaction time
     */
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

    /**
     * Creates an interaction with an associated value.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param value the value applied by the interaction
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
     */
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

    /**
     * Creates an interaction with an associated value and environmental context.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param value the value applied by the interaction
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
     * @param weather the weather at the interaction time
     * @param outsideTemperature the outside temperature at the interaction time
     */
    public DeviceInteraction(int deviceId, InteractionType type, Double value, int userId, LocalDateTime timestamp,
                             WeatherCondition weather, Double outsideTemperature) {
        this(deviceId, type, value, userId, timestamp, weather, outsideTemperature, null);
    }

    /**
     * Creates an interaction with an associated value and full environmental context.
     *
     * @param deviceId the device identifier
     * @param type the interaction type
     * @param value the value applied by the interaction
     * @param userId the user identifier
     * @param timestamp the simulation timestamp
     * @param weather the weather at the interaction time
     * @param outsideTemperature the outside temperature at the interaction time
     * @param luminosity the luminosity at the interaction time
     */
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
     * Creates a copy of another device interaction.
     *
     * @param other the interaction to copy
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
     * Gets the interacted device identifier.
     *
     * @return the device identifier
     */
    public int getDeviceId() {
        return this.deviceId;
    }

    /**
     * Gets the user identifier.
     *
     * @return the user identifier
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Sets the user identifier.
     *
     * @param userId the user identifier
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Sets the device ID.
     *
     * @param deviceId the device identifier
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the type of interaction performed.
     *
     * @return the interaction type
     */
    public InteractionType getType() {
        return this.type;
    }

    /**
     * Sets the interaction type.
     *
     * @param type the interaction type
     */
    public void setType(InteractionType type) {
        this.type = type;
    }

    /**
     * Gets the value applied during the interaction.
     *
     * @return the value, or null if not applicable
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Sets the value applied during the interaction.
     *
     * @param value the interaction value, or null if not applicable
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Gets the simulation timestamp of the interaction.
     *
     * @return the simulation timestamp
     */
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the simulation timestamp.
     *
     * @param timestamp the simulation timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the outside weather recorded when the interaction happened.
     *
     * @return the weather condition, or null if unavailable
     */
    public WeatherCondition getWeather() {
        return this.weather;
    }

    /**
     * Sets the outside weather associated with this interaction.
     *
     * @param weather the weather condition
     */
    public void setWeather(WeatherCondition weather) {
        this.weather = weather;
    }

    /**
     * Gets the outside temperature recorded when the interaction happened.
     *
     * @return the outside temperature, or null if unavailable
     */
    public Double getOutsideTemperature() {
        return this.outsideTemperature;
    }

    /**
     * Sets the outside temperature associated with this interaction.
     *
     * @param outsideTemperature the outside temperature
     */
    public void setOutsideTemperature(Double outsideTemperature) {
        this.outsideTemperature = outsideTemperature;
    }

    /**
     * Gets the outside luminosity recorded when the interaction happened.
     *
     * @return the luminosity, or null if unavailable
     */
    public Double getLuminosity() {
        return this.luminosity;
    }

    /**
     * Sets the outside luminosity associated with this interaction.
     *
     * @param luminosity the luminosity
     */
    public void setLuminosity(Double luminosity) {
        this.luminosity = luminosity;
    }

    /**
     * Creates a copy of this interaction.
     *
     * @return a copied DeviceInteraction instance
     */
    @Override
    public DeviceInteraction clone() {
        return new DeviceInteraction(this);
    }

    /**
     * Compares this interaction with another object for equality.
     *
     * @param o the object to compare with
     * @return true if all fields match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DeviceInteraction interaction = (DeviceInteraction) o;
        return this.getDeviceId() == interaction.getDeviceId() &&
               this.getUserId() == interaction.getUserId() &&
               this.getType() == interaction.getType() &&
               Objects.equals(this.getValue(), interaction.getValue()) &&
               Objects.equals(this.getTimestamp(), interaction.getTimestamp()) &&
               this.getWeather() == interaction.getWeather() &&
               Objects.equals(this.getOutsideTemperature(), interaction.getOutsideTemperature()) &&
               Objects.equals(this.getLuminosity(), interaction.getLuminosity());
    }

    /**
     * Generates a hash code for this interaction.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceId(), this.getUserId(), this.getType(), this.getValue(), this.getTimestamp(),
                this.getWeather(), this.getOutsideTemperature(), this.getLuminosity());
    }

    /**
     * Returns a string representation of this interaction.
     *
     * @return a formatted string with the interaction information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceInteraction { ")
          .append("Device ID: ").append(this.getDeviceId())
          .append(", User ID: ").append(this.getUserId())
          .append(", Type: ").append(this.getType())
          .append(", Value: ").append(this.getValue() != null ? this.getValue() : "N/A")
          .append(", Timestamp: ").append(this.getTimestamp())
          .append(", Weather: ").append(this.getWeather() != null ? this.getWeather() : "N/A")
          .append(", Outside Temperature: ").append(this.getOutsideTemperature() != null ? this.getOutsideTemperature() : "N/A")
          .append(", Luminosity: ").append(this.getLuminosity() != null ? this.getLuminosity() : "N/A")
          .append(" }");
        return sb.toString();
    }
}
