package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class SimulationState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime currentDateTime;
    private final int temperature;
    private final Simulation.WeatherCondition weather;

    public SimulationState(LocalDateTime currentDateTime, int temperature, Simulation.WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.temperature = temperature;
        this.weather = weather;
    }

    public LocalDateTime getCurrentDateTime() {
        return this.currentDateTime;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public Simulation.WeatherCondition getWeather() {
        return this.weather;
    }
}
