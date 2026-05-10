package domuscontrol.simulation;

/**
 * Represents the weather conditions available in the simulation,
 * each with an associated luminosity multiplier and display name.
 * Weather affects environmental simulation and can trigger condition-based automations.
 * Each condition has a luminosity multiplier that affects outdoor light levels.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public enum WeatherCondition {
    
    /** Sunny weather with high luminosity (100% base level). */
    SUNNY(1.0, "SUNNY"),

    /** Partly cloudy weather (80% luminosity). */
    PARTLY_CLOUDY(0.8, "PARTLY_CLOUDY"),

    /** Cloudy weather (50% luminosity). */
    CLOUDY(0.5, "CLOUDY"),

    /** Foggy weather with reduced visibility (40% luminosity). */
    FOGGY(0.4, "FOGGY"),
    
    /** Rainy weather (30% luminosity). Triggers rainfall sensor. */
    RAINING(0.3, "RAINING"),

    /** Stormy weather with heavy rain (15% luminosity). Triggers rainfall sensor with high intensity. */
    STORMY(0.15, "STORMY"),

    /** Snowing weather (60% luminosity). */
    SNOWING(0.6, "SNOWING");

    /** The luminosity multiplier for this weather condition. */
    private final double luminosityMultiplier;

    /** The display name for this weather condition. */
    private final String displayName;

    /**
     * Constructs a WeatherCondition with the specified luminosity multiplier and display name.
     * @param luminosityMultiplier the multiplier applied to base luminosity for this weather condition
     * @param displayName the display name for this weather condition
     */
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

    /**
     * Returns the display name of this weather condition.
     * @return A string representing this weather condition.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}
