package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TemperatureSensorTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStartsWithZeroTemperatureAndOnState() {
        TemperatureSensor sensor = new TemperatureSensor("Aqara", "Temp", 0.1);

        assertEquals("Aqara", sensor.getBrand());
        assertEquals("Temp", sensor.getModel());
        assertEquals(0.1, sensor.getConsumptionPerHour());
        assertEquals(0.0, sensor.getTemperature());
        assertTrue(sensor.isOn());
    }

    @Test
    void updateFromStateReadsTemperatureWhenOn() {
        TemperatureSensor sensor = new TemperatureSensor();

        sensor.updateFromState(state(23.5, 500.0, WeatherCondition.SUNNY));

        assertEquals(23.5, sensor.getTemperature());
    }

    @Test
    void updateFromStateDoesNothingWhenOff() {
        TemperatureSensor sensor = new TemperatureSensor();
        sensor.updateFromState(state(23.5, 500.0, WeatherCondition.SUNNY));

        sensor.turnOff();
        sensor.updateFromState(state(10.0, 100.0, WeatherCondition.CLOUDY));

        assertEquals(23.5, sensor.getTemperature());
    }

    @Test
    void cloneKeepsCurrentReading() {
        TemperatureSensor sensor = new TemperatureSensor("Aqara", "Temp", 0.1);
        sensor.updateFromState(state(23.5, 500.0, WeatherCondition.SUNNY));

        TemperatureSensor copy = (TemperatureSensor) sensor.clone();

        assertNotSame(sensor, copy);
        assertEquals(sensor, copy);
        assertEquals(sensor.hashCode(), copy.hashCode());
        assertEquals(23.5, copy.getTemperature());
    }

    @Test
    void toStringIncludesFormattedTemperature() {
        TemperatureSensor sensor = new TemperatureSensor();
        sensor.updateFromState(state(23.54, 500.0, WeatherCondition.SUNNY));

        assertTrue(sensor.toString().contains("Temperature: 23.5"));
    }

    private SimulationState state(double temperature, double luminosity, WeatherCondition weather) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationStateStub(now, now.minusHours(1), temperature, luminosity, weather);
    }
}
