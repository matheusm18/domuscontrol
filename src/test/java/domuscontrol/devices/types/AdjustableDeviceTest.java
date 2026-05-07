package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdjustableDeviceTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void defaultConstructorStartsWithLevelZero() {
        TestAdjustableDevice device = new TestAdjustableDevice();

        assertEquals(0, device.getLevel());
        assertEquals(DeviceStatus.OFF, device.getStatus());
        assertFalse(device.isConsuming());
    }

    @Test
    void constructorSetsInitialLevelWithinRange() {
        TestAdjustableDevice device = new TestAdjustableDevice("Brand", "Model", 20.0, 45);

        assertEquals("Brand", device.getBrand());
        assertEquals("Model", device.getModel());
        assertEquals(20.0, device.getConsumptionPerHour());
        assertEquals(45, device.getLevel());
    }

    @Test
    void setLevelClampsValuesToValidRange() {
        TestAdjustableDevice device = new TestAdjustableDevice();

        device.setLevel(-10);
        assertEquals(0, device.getLevel());

        device.setLevel(150);
        assertEquals(100, device.getLevel());

        device.setLevel(60);
        assertEquals(60, device.getLevel());
    }

    @Test
    void changingLevelDoesNotTurnDeviceOnByItself() {
        TestAdjustableDevice device = new TestAdjustableDevice();

        device.setLevel(80);

        assertEquals(DeviceStatus.OFF, device.getStatus());
        assertFalse(device.isConsuming());
    }

    @Test
    void cloneKeepsDeviceFieldsAndLevel() {
        TestAdjustableDevice device = new TestAdjustableDevice("Brand", "Model", 30.0, 70);
        device.turnOn();
        device.tick(15);

        TestAdjustableDevice copy = device.clone();

        assertNotSame(device, copy);
        assertEquals(device, copy);
        assertEquals(device.hashCode(), copy.hashCode());
        assertEquals(70, copy.getLevel());
        assertEquals(15, copy.getTotalMinutesOn());
    }

    @Test
    void toStringIncludesLevel() {
        TestAdjustableDevice device = new TestAdjustableDevice("Brand", "Model", 30.0, 25);

        assertTrue(device.toString().contains("Level: 25%"));
    }

    private static class TestAdjustableDevice extends AdjustableDevice {
        TestAdjustableDevice() {
            super();
        }

        TestAdjustableDevice(String brand, String model, double consumptionPerHour, int level) {
            super(brand, model, consumptionPerHour, level);
        }

        TestAdjustableDevice(TestAdjustableDevice device) {
            super(device);
        }

        @Override
        public TestAdjustableDevice clone() {
            return new TestAdjustableDevice(this);
        }
    }
}
