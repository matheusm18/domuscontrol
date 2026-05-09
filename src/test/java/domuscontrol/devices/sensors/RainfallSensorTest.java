package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RainfallSensorTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStartsWithZeroRainfallAndOnState() {
        RainfallSensor sensor = new RainfallSensor("Aqara", "Rain", 0.1);

        assertEquals("Aqara", sensor.getBrand());
        assertEquals("Rain", sensor.getModel());
        assertEquals(0.1, sensor.getConsumptionPerHour());
        assertEquals(0.0, sensor.getRainfall());
        assertTrue(sensor.isOn());
    }

    @Test
    void clearWeatherSetsRainfallToZero() {
        RainfallSensor sensor = new RainfallSensor();

        sensor.updateFromState(state(WeatherCondition.SUNNY));

        assertEquals(0.0, sensor.getRainfall());
    }

    @Test
    void rainingWeatherSetsRainfallInExpectedRange() {
        RainfallSensor sensor = new RainfallSensor();

        sensor.updateFromState(state(WeatherCondition.RAINING));

        assertTrue(sensor.getRainfall() >= 1.0);
        assertTrue(sensor.getRainfall() < 15.0);
    }

    @Test
    void stormyWeatherSetsRainfallInExpectedRange() {
        RainfallSensor sensor = new RainfallSensor();

        sensor.updateFromState(state(WeatherCondition.STORMY));

        assertTrue(sensor.getRainfall() >= 15.0);
        assertTrue(sensor.getRainfall() < 50.0);
    }

    @Test
    void updateFromStateDoesNothingWhenOff() {
        RainfallSensor sensor = new RainfallSensor();
        sensor.updateFromState(state(WeatherCondition.RAINING));
        double previousRainfall = sensor.getRainfall();

        sensor.turnOff();
        sensor.updateFromState(state(WeatherCondition.SUNNY));

        assertEquals(previousRainfall, sensor.getRainfall());
    }

    @Test
    void cloneKeepsCurrentReading() {
        RainfallSensor sensor = new RainfallSensor("Aqara", "Rain", 0.1);
        sensor.updateFromState(state(WeatherCondition.STORMY));

        RainfallSensor copy = (RainfallSensor) sensor.clone();

        assertNotSame(sensor, copy);
        assertEquals(sensor, copy);
        assertEquals(sensor.hashCode(), copy.hashCode());
        assertEquals(sensor.getRainfall(), copy.getRainfall());
    }

    @Test
    void toStringIncludesRainfall() {
        RainfallSensor sensor = new RainfallSensor();

        assertTrue(sensor.toString().contains("Rainfall: 0.0 mm/h"));
    }

    private SimulationState state(WeatherCondition weather) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationStateStub(now, now.minusHours(1), 20.0, 500.0, weather);
    }
}
