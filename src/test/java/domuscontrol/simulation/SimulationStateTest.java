package domuscontrol.simulation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationStateTest {

    @Test
    void constructorStoresSnapshotFields() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDateTime previous = now.minusHours(1);
        SimulationState state = new SimulationState(now, previous, 20.5, 700.0, WeatherCondition.CLOUDY);

        assertEquals(now, state.getCurrentDateTime());
        assertEquals(previous, state.getPreviousDateTime());
        assertEquals(20.5, state.getTemperature());
        assertEquals(700.0, state.getLuminosity());
        assertEquals(WeatherCondition.CLOUDY, state.getWeather());
    }

    @Test
    void fromBuildsSnapshotFromSimulation() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        Simulation simulation = new Simulation(now, 22.0, WeatherCondition.CLOUDY);

        SimulationState state = SimulationState.from(simulation);

        assertEquals(now, state.getCurrentDateTime());
        assertEquals(now, state.getPreviousDateTime());
        assertEquals(22.0, state.getTemperature());
        assertEquals(500.0, state.getLuminosity());
        assertEquals(WeatherCondition.CLOUDY, state.getWeather());
    }

    @Test
    void isRainingMatchesRainAndStorm() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        assertTrue(new SimulationState(now, now, 20.0, 0.0, WeatherCondition.RAINING).isRaining());
        assertTrue(new SimulationState(now, now, 20.0, 0.0, WeatherCondition.STORMY).isRaining());
        assertFalse(new SimulationState(now, now, 20.0, 0.0, WeatherCondition.SUNNY).isRaining());
    }
}
