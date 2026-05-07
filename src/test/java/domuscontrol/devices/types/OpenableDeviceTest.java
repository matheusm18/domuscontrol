package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenableDeviceTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void defaultConstructorStartsClosedAndNotConsuming() {
        TestOpenableDevice device = new TestOpenableDevice();

        assertEquals(0, device.getOpeningLevel());
        assertEquals(DeviceStatus.CLOSED, device.getStatus());
        assertFalse(device.isOpen());
        assertFalse(device.isConsuming());
    }

    @Test
    void constructorSetsInitialOpeningAndStatus() {
        TestOpenableDevice device = new TestOpenableDevice("Brand", "Model", 40.0, 30);

        assertEquals("Brand", device.getBrand());
        assertEquals("Model", device.getModel());
        assertEquals(40.0, device.getConsumptionPerHour());
        assertEquals(30, device.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, device.getStatus());
        assertTrue(device.isOpen());
        assertTrue(device.isConsuming());
        assertEquals(1, device.getTotalActivations());
    }

    @Test
    void setOpeningClampsValuesToValidRange() {
        TestOpenableDevice device = new TestOpenableDevice();

        device.setOpening(-10);
        assertEquals(0, device.getOpeningLevel());
        assertEquals(DeviceStatus.CLOSED, device.getStatus());

        device.setOpening(150);
        assertEquals(100, device.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, device.getStatus());

        device.setOpening(60);
        assertEquals(60, device.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, device.getStatus());
    }

    @Test
    void settingOpeningToZeroClosesDevice() {
        TestOpenableDevice device = new TestOpenableDevice("Brand", "Model", 40.0, 75);

        device.setOpening(0);

        assertEquals(0, device.getOpeningLevel());
        assertEquals(DeviceStatus.CLOSED, device.getStatus());
        assertFalse(device.isOpen());
        assertFalse(device.isConsuming());
    }

    @Test
    void tickOnlyCountsMinutesWhenOpen() {
        TestOpenableDevice device = new TestOpenableDevice("Brand", "Model", 120.0, 0);

        device.tick(20);
        assertEquals(0, device.getTotalMinutesOn());

        device.setOpening(50);
        device.tick(20);

        assertEquals(20, device.getTotalMinutesOn());
        assertEquals(40.0, device.getEnergyConsumption());
    }

    @Test
    void cloneKeepsDeviceFieldsAndOpeningLevel() {
        TestOpenableDevice device = new TestOpenableDevice("Brand", "Model", 40.0, 75);
        device.tick(10);

        TestOpenableDevice copy = device.clone();

        assertNotSame(device, copy);
        assertEquals(device, copy);
        assertEquals(device.hashCode(), copy.hashCode());
        assertEquals(75, copy.getOpeningLevel());
        assertEquals(10, copy.getTotalMinutesOn());
    }

    @Test
    void toStringIncludesOpeningLevel() {
        TestOpenableDevice device = new TestOpenableDevice("Brand", "Model", 40.0, 35);

        assertTrue(device.toString().contains("Opening: 35%"));
    }

    private static class TestOpenableDevice extends OpenableDevice {
        TestOpenableDevice() {
            super();
        }

        TestOpenableDevice(String brand, String model, double consumptionPerHour, int openingLevel) {
            super(brand, model, consumptionPerHour, openingLevel);
        }

        TestOpenableDevice(TestOpenableDevice device) {
            super(device);
        }

        @Override
        public TestOpenableDevice clone() {
            return new TestOpenableDevice(this);
        }
    }
}
