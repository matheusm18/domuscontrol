package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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
     * Constructs a SimulationState with default simulation values.
     */
    public SimulationState() {
        LocalDateTime now = LocalDateTime.now();
        this.currentDateTime = now;
        this.previousDateTime = now;
        this.temperature = 20.0;
        this.luminosity = 1000.0;
        this.weather = WeatherCondition.SUNNY;
    }

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
     * Constructs a SimulationState by copying another SimulationState.
     *
     * @param state the simulation state to copy
     */
    public SimulationState(SimulationState state) {
        this.currentDateTime = state.getCurrentDateTime();
        this.previousDateTime = state.getPreviousDateTime();
        this.temperature = state.getTemperature();
        this.luminosity = state.getLuminosity();
        this.weather = state.getWeather();
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
    public LocalDateTime getCurrentDateTime() {
        return this.currentDateTime;
    }

    /**
     * Returns the previous date and time.
     *
     * @return the previous date and time
     */
    public LocalDateTime getPreviousDateTime() {
        return this.previousDateTime;
    }

    /**
     * Returns the current temperature in Celsius.
     *
     * @return the temperature
     */
    public double getTemperature() {
        return this.temperature;
    }

    /**
     * Returns the current luminosity in lux.
     *
     * @return the luminosity
     */
    public double getLuminosity() {
        return this.luminosity;
    }

    /**
     * Returns the current weather condition.
     *
     * @return the weather condition
     */
    public WeatherCondition getWeather() {
        return this.weather;
    }

    /**
     * Checks if it is currently raining or stormy.
     *
     * @return true if the weather is RAINING or STORMY, false otherwise
     */
    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING || this.weather == WeatherCondition.STORMY;
    }

    /**
     * Creates a copy of this simulation state.
     *
     * @return a new SimulationState with the same values
     */
    @Override
    public SimulationState clone() {
        return new SimulationState(this);
    }

    /**
     * Compares this simulation state with another object for equality.
     *
     * @param o the object to compare with
     * @return true if all simulation state values match, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        SimulationState state = (SimulationState) o;
        return Double.compare(this.temperature, state.temperature) == 0
            && Double.compare(this.luminosity, state.luminosity) == 0
            && Objects.equals(this.currentDateTime, state.currentDateTime)
            && Objects.equals(this.previousDateTime, state.previousDateTime)
            && this.weather == state.weather;
    }

    /**
     * Generates a hash code for this simulation state.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.currentDateTime, this.previousDateTime, this.temperature, this.luminosity, this.weather);
    }

    /**
     * Returns a string representation of this simulation state.
     *
     * @return a formatted string with the simulation state values
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimulationState { ")
          .append("Current: ").append(this.currentDateTime)
          .append(", Previous: ").append(this.previousDateTime)
          .append(", Temperature: ").append(this.temperature)
          .append(", Luminosity: ").append(this.luminosity)
          .append(", Weather: ").append(this.weather)
          .append(" }");
        return sb.toString();
    }
}
