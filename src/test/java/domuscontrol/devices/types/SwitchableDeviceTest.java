package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SwitchableDeviceTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStartsOffAndNotConsuming() {
        TestSwitchableDevice device = new TestSwitchableDevice();

        assertEquals(DeviceStatus.OFF, device.getStatus());
        assertFalse(device.isOn());
        assertFalse(device.isConsuming());
    }

    @Test
    void turnOnUpdatesStatusAndConsumption() {
        TestSwitchableDevice device = new TestSwitchableDevice("Brand", "Model", 10.0);

        device.turnOn();

        assertEquals(DeviceStatus.ON, device.getStatus());
        assertTrue(device.isOn());
        assertTrue(device.isConsuming());
        assertEquals(1, device.getTotalActivations());
    }

    @Test
    void turnOffUpdatesStatusAndStopsConsumption() {
        TestSwitchableDevice device = new TestSwitchableDevice();

        device.turnOn();
        device.turnOff();

        assertEquals(DeviceStatus.OFF, device.getStatus());
        assertFalse(device.isOn());
        assertFalse(device.isConsuming());
    }

    @Test
    void tickOnlyCountsMinutesWhenOn() {
        TestSwitchableDevice device = new TestSwitchableDevice("Brand", "Model", 120.0);

        device.tick(30);
        assertEquals(0, device.getTotalMinutesOn());

        device.turnOn();
        device.tick(30);

        assertEquals(30, device.getTotalMinutesOn());
        assertEquals(60.0, device.getEnergyConsumption());
    }

    private static class TestSwitchableDevice extends SwitchableDevice {
        TestSwitchableDevice() {
            super();
        }

        TestSwitchableDevice(String brand, String model, double consumptionPerHour) {
            super(brand, model, consumptionPerHour);
        }

        TestSwitchableDevice(TestSwitchableDevice device) {
            super(device);
        }

        @Override
        public TestSwitchableDevice clone() {
            return new TestSwitchableDevice(this);
        }
    }
}
