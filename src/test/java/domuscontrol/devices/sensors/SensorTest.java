package domuscontrol.devices.sensors;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.simulation.SimulationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SensorTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void defaultConstructorStartsOnButDoesNotConsume() {
        TestSensor sensor = new TestSensor();

        assertEquals(DeviceStatus.ON, sensor.getStatus());
        assertTrue(sensor.isOn());
        assertFalse(sensor.isConsuming());
        assertEquals(1, sensor.getTotalActivations());
    }

    @Test
    void constructorStoresDetailsAndStartsOn() {
        TestSensor sensor = new TestSensor("Aqara", "T1", 0.2);

        assertEquals("Aqara", sensor.getBrand());
        assertEquals("T1", sensor.getModel());
        assertEquals(0.2, sensor.getConsumptionPerHour());
        assertTrue(sensor.isOn());
    }

    @Test
    void tickNeverCountsEnergyConsumption() {
        TestSensor sensor = new TestSensor("Aqara", "T1", 10.0);

        sensor.tick(60);

        assertEquals(0, sensor.getTotalMinutesOn());
        assertEquals(0.0, sensor.getEnergyConsumption());
    }

    @Test
    void cloneKeepsState() {
        TestSensor sensor = new TestSensor("Aqara", "T1", 0.2);

        TestSensor copy = sensor.clone();

        assertNotSame(sensor, copy);
        assertEquals(sensor, copy);
        assertEquals(sensor.hashCode(), copy.hashCode());
    }

    private static class TestSensor extends Sensor {
        TestSensor() {
            super();
        }

        TestSensor(String brand, String model, double consumptionPerHour) {
            super(brand, model, consumptionPerHour);
        }

        TestSensor(TestSensor sensor) {
            super(sensor);
        }

        @Override
        public void updateFromState(SimulationState state) {
        }

        @Override
        public TestSensor clone() {
            return new TestSensor(this);
        }
    }
}
