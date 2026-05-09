package domuscontrol.simulation;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Test-only implementation of {@link SimulationState} that accepts arbitrary values.
 * Use this to inject specific luminosity, temperature, or time values in unit tests.
 */
public class SimulationStateStub implements SimulationState {

    private final LocalDateTime currentDateTime;
    private final LocalDateTime previousDateTime;
    private final double temperature;
    private final double luminosity;
    private final WeatherCondition weather;

    public SimulationStateStub(LocalDateTime currentDateTime, LocalDateTime previousDateTime,
                               double temperature, double luminosity, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.previousDateTime = previousDateTime;
        this.temperature = temperature;
        this.luminosity = luminosity;
        this.weather = weather;
    }

    @Override
    public LocalDateTime getCurrentDateTime() { return this.currentDateTime; }

    @Override
    public LocalDateTime getPreviousDateTime() { return this.previousDateTime; }

    @Override
    public double getTemperature() { return this.temperature; }

    @Override
    public double getLuminosity() { return this.luminosity; }

    @Override
    public WeatherCondition getWeather() { return this.weather; }

    @Override
    public long getTimeElapsed() {
        return Duration.between(this.previousDateTime, this.currentDateTime).toMinutes();
    }

    @Override
    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING || this.weather == WeatherCondition.STORMY;
    }
}
