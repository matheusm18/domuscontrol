package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class SimulationState implements Serializable {

    private final LocalDateTime currentDateTime;
    private final LocalDateTime previousDateTime;
    private final double temperature;
    private final double luminosity;
    private final WeatherCondition weather;

    public SimulationState(LocalDateTime currentDateTime, LocalDateTime previousDateTime,
                           double temperature, double luminosity, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.previousDateTime = previousDateTime;
        this.temperature = temperature;
        this.luminosity = luminosity;
        this.weather = weather;
    }

    public static SimulationState from(Simulation simulation) {
        return new SimulationState(
            simulation.getCurrentDateTime(),
            simulation.getPreviousDateTime(),
            simulation.getTemperature(),
            simulation.getLuminosity(),
            simulation.getWeather()
        );
    }

    public LocalDateTime getCurrentDateTime() { return this.currentDateTime; }
    public LocalDateTime getPreviousDateTime() { return this.previousDateTime; }
    public double getTemperature() { return this.temperature; }
    public double getLuminosity() { return this.luminosity; }
    public WeatherCondition getWeather() { return this.weather; }

    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING || this.weather == WeatherCondition.STORMY;
    }
}
