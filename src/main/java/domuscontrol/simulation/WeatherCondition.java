package domuscontrol.simulation;

/**
 * Represents the weather conditions available in the simulation,
 * each with an associated luminosity multiplier and display name.
 */
public enum WeatherCondition {
    SUNNY(1.0, "SUNNY"),
    PARTLY_CLOUDY(0.8, "PARTLY_CLOUDY"),
    CLOUDY(0.5, "CLOUDY"),
    FOGGY(0.4, "FOGGY"),
    RAINING(0.3, "RAINING"),
    STORMY(0.15, "STORMY"),
    SNOWING(0.6, "SNOWING"),
    WINTER_IS_COMING(0.2, "WINTER");

    private final double luminosityMultiplier;
    private final String displayName;

    WeatherCondition(double luminosityMultiplier, String displayName) {
        this.luminosityMultiplier = luminosityMultiplier;
        this.displayName = displayName;
    }

    /**
     * Returns the luminosity multiplier associated with this weather condition.
     * @return A value between 0.0 and 1.0 applied to the base luminosity.
     */
    public double getLuminosityMultiplier() {
        return this.luminosityMultiplier;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
