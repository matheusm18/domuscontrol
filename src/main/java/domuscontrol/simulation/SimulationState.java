package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable snapshot of the simulation state at a specific point in time.
 * Captures current date/time, temperature, luminosity, and weather conditions.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public final class SimulationState implements Serializable {

    /**
     * The current date and time of the simulation.
     */
    private final LocalDateTime currentDateTime;

    /**
     * The previous date and time before the last simulation tick.
     */
    private final LocalDateTime previousDateTime;

    /**
     * The current temperature in Celsius.
     */
    private final double temperature;

    /**
     * The current luminosity in lux.
     */
    private final double luminosity;

    /**
     * The current weather condition.
     */
    private final WeatherCondition weather;

    /**
     * Constructs a SimulationState with the given parameters.
     *
     * @param currentDateTime the current date and time
     * @param previousDateTime the previous date and time
     * @param temperature the temperature in Celsius
     * @param luminosity the luminosity in lux
     * @param weather the weather condition
     */
    public SimulationState(LocalDateTime currentDateTime, LocalDateTime previousDateTime,
                           double temperature, double luminosity, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.previousDateTime = previousDateTime;
        this.temperature = temperature;
        this.luminosity = luminosity;
        this.weather = weather;
    }

    /**
     * Creates a SimulationState from a Simulation object.
     *
     * @param simulation the simulation to snapshot
     * @return a new SimulationState with the current simulation values
     */
    public static SimulationState from(Simulation simulation) {
        return new SimulationState(
            simulation.getCurrentDateTime(),
            simulation.getPreviousDateTime(),
            simulation.getTemperature(),
            simulation.getLuminosity(),
            simulation.getWeather()
        );
    }

    /**
     * Returns the current date and time.
     *
     * @return the current date and time
     */
    public LocalDateTime getCurrentDateTime() { return this.currentDateTime; }

    /**
     * Returns the previous date and time.
     *
     * @return the previous date and time
     */
    public LocalDateTime getPreviousDateTime() { return this.previousDateTime; }

    /**
     * Returns the current temperature in Celsius.
     *
     * @return the temperature
     */
    public double getTemperature() { return this.temperature; }

    /**
     * Returns the current luminosity in lux.
     *
     * @return the luminosity
     */
    public double getLuminosity() { return this.luminosity; }

    /**
     * Returns the current weather condition.
     *
     * @return the weather condition
     */
    public WeatherCondition getWeather() { return this.weather; }

    /**
     * Checks if it is currently raining or stormy.
     *
     * @return true if the weather is RAINING or STORMY, false otherwise
     */
    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING || this.weather == WeatherCondition.STORMY;
    }
}
