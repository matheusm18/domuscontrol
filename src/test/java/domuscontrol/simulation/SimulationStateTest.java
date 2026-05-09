package domuscontrol.simulation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationStateTest {

    @Test
    void simulationImplementsInterface() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        Simulation simulation = new Simulation(now, 22.0, WeatherCondition.CLOUDY);

        SimulationState state = simulation;

        assertEquals(now, state.getCurrentDateTime());
        assertEquals(now, state.getPreviousDateTime());
        assertEquals(22.0, state.getTemperature());
        assertEquals(500.0, state.getLuminosity());
        assertEquals(WeatherCondition.CLOUDY, state.getWeather());
    }

    @Test
    void cloneIsIsolatedFromOriginal() {
        Simulation simulation = new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 20.0, WeatherCondition.SUNNY);

        SimulationState snapshot = simulation.clone();
        simulation.changeWeather(WeatherCondition.STORMY);
        simulation.setTemperature(5.0);

        assertEquals(WeatherCondition.SUNNY, snapshot.getWeather());
        assertEquals(20.0, snapshot.getTemperature());
    }

    @Test
    void isRainingMatchesRainAndStorm() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        SimulationState raining = new Simulation(now, 20.0, WeatherCondition.RAINING);
        SimulationState stormy = new Simulation(now, 20.0, WeatherCondition.STORMY);
        SimulationState sunny = new Simulation(now, 20.0, WeatherCondition.SUNNY);

        assertTrue(raining.isRaining());
        assertTrue(stormy.isRaining());
        assertFalse(sunny.isRaining());
    }
}
