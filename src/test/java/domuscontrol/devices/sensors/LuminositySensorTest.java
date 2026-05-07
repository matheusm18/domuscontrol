package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class LuminositySensorTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStartsWithZeroLuminosityAndOnState() {
        LuminositySensor sensor = new LuminositySensor("Aqara", "Light", 0.1);

        assertEquals("Aqara", sensor.getBrand());
        assertEquals("Light", sensor.getModel());
        assertEquals(0.1, sensor.getConsumptionPerHour());
        assertEquals(0.0, sensor.getLuminosity());
        assertTrue(sensor.isOn());
    }

    @Test
    void updateFromStateReadsLuminosityWhenOn() {
        LuminositySensor sensor = new LuminositySensor();

        sensor.updateFromState(state(20.0, 750.5, WeatherCondition.SUNNY));

        assertEquals(750.5, sensor.getLuminosity());
    }

    @Test
    void updateFromStateDoesNothingWhenOff() {
        LuminositySensor sensor = new LuminositySensor();
        sensor.updateFromState(state(20.0, 750.5, WeatherCondition.SUNNY));

        sensor.turnOff();
        sensor.updateFromState(state(20.0, 100.0, WeatherCondition.CLOUDY));

        assertEquals(750.5, sensor.getLuminosity());
    }

    @Test
    void cloneKeepsCurrentReading() {
        LuminositySensor sensor = new LuminositySensor("Aqara", "Light", 0.1);
        sensor.updateFromState(state(20.0, 750.5, WeatherCondition.SUNNY));

        LuminositySensor copy = (LuminositySensor) sensor.clone();

        assertNotSame(sensor, copy);
        assertEquals(sensor, copy);
        assertEquals(sensor.hashCode(), copy.hashCode());
        assertEquals(750.5, copy.getLuminosity());
    }

    @Test
    void toStringIncludesFormattedLuminosity() {
        LuminositySensor sensor = new LuminositySensor();
        sensor.updateFromState(state(20.0, 750.54, WeatherCondition.SUNNY));

        assertTrue(sensor.toString().contains("Luminosity: 750.5"));
    }

    private SimulationState state(double temperature, double luminosity, WeatherCondition weather) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationState(now, now.minusHours(1), temperature, luminosity, weather);
    }
}
