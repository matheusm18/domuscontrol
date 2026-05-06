package domuscontrol.simulation;

import java.time.LocalDateTime;

public final class SimulationState {

    private final LocalDateTime currentDateTime;
    private final int temperature;
    private final int luminosity;
    private final WeatherCondition weather;

    public SimulationState(LocalDateTime currentDateTime, int temperature, int luminosity, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.temperature = temperature;
        this.luminosity = luminosity;
        this.weather = weather;
    }

    public LocalDateTime getCurrentDateTime() {
        return this.currentDateTime;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getLuminosity() {
        return this.luminosity;
    }

    public WeatherCondition getWeather() {
        return this.weather;
    }
}
