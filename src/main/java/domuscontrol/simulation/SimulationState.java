package domuscontrol.simulation;

import java.time.LocalDateTime;

/**
 * Read-only view of the simulation environment.
 * Passed to conditions, sensors, and routines so they can observe the current
 * state without being able to mutate the real simulation.
 * Callers receive a clone of {@link Simulation} typed as this interface;
 * even a downcast to {@code Simulation} only reaches the clone.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public interface SimulationState {

    /**
     * Returns the current date and time.
     *
     * @return the current date and time
     */
    LocalDateTime getCurrentDateTime();

    /**
     * Returns the date and time at the start of the last tick.
     *
     * @return the previous date and time
     */
    LocalDateTime getPreviousDateTime();

    /**
     * Returns the current temperature in Celsius.
     *
     * @return the temperature
     */
    double getTemperature();

    /**
     * Returns the ambient luminosity in lux.
     *
     * @return the luminosity
     */
    double getLuminosity();

    /**
     * Returns the current weather condition.
     *
     * @return the weather condition
     */
    WeatherCondition getWeather();

    /**
     * Returns the number of minutes elapsed since the last tick.
     *
     * @return minutes elapsed between the previous and current date/time
     */
    long getTimeElapsed();

    /**
     * Returns whether it is currently raining or stormy.
     *
     * @return true if the weather is RAINING or STORMY
     */
    boolean isRaining();
}
