package domuscontrol.simulation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {

    @Test
    void defaultConstructorStartsWithDefaults() {
        Simulation simulation = new Simulation();

        assertEquals(simulation.getCurrentDateTime(), simulation.getPreviousDateTime());
        assertEquals(20.0, simulation.getTemperature());
        assertEquals(WeatherCondition.SUNNY, simulation.getWeather());
    }

    @Test
    void constructorAndSettersStoreState() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        Simulation simulation = new Simulation(now, 15.0, WeatherCondition.RAINING);
        LocalDateTime later = now.plusHours(1);

        simulation.setCurrentDateTime(later);
        simulation.setPreviousDateTime(now);
        simulation.setTemperature(18.0);
        simulation.changeWeather(WeatherCondition.STORMY);

        assertEquals(later, simulation.getCurrentDateTime());
        assertEquals(now, simulation.getPreviousDateTime());
        assertEquals(60, simulation.getTimeElapsed());
        assertEquals(18.0, simulation.getTemperature());
        assertEquals(WeatherCondition.STORMY, simulation.getWeather());
    }

    @Test
    void luminosityDependsOnTimeAndWeather() {
        assertEquals(1000.0, new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 20.0, WeatherCondition.SUNNY).getLuminosity());
        assertEquals(100.0, new Simulation(LocalDateTime.of(2026, 1, 1, 2, 0), 20.0, WeatherCondition.SUNNY).getLuminosity());
        assertEquals(500.0, new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 20.0, WeatherCondition.CLOUDY).getLuminosity());
    }

    @Test
    void weatherHelpersMatchCurrentWeather() {
        Simulation simulation = new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 20.0, WeatherCondition.RAINING);

        assertTrue(simulation.isRaining());

        simulation.changeWeather(WeatherCondition.SUNNY);

        assertFalse(simulation.isRaining());
    }

    @Test
    void advanceSimulationMovesTimeAndKeepsTemperatureInBounds() {
        Simulation simulation = new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 44.9, WeatherCondition.SUNNY);

        simulation.advanceSimulation(30);

        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), simulation.getPreviousDateTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 30), simulation.getCurrentDateTime());
        assertTrue(simulation.getTemperature() <= 45.0);
        assertTrue(simulation.getTemperature() >= -15.0);
    }

    @Test
    void cloneEqualsAndHashCodeCopyState() {
        Simulation simulation = new Simulation(LocalDateTime.of(2026, 1, 1, 12, 0), 20.0, WeatherCondition.SUNNY);

        Simulation copy = simulation.clone();

        assertNotSame(simulation, copy);
        assertEquals(simulation, copy);
        assertEquals(simulation.hashCode(), copy.hashCode());
    }
}
