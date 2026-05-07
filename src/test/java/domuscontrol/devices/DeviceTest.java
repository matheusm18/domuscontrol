package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void defaultConstructorInitializesEmptyOffDevice() {
        TestDevice device = new TestDevice();

        assertEquals(1, device.getId());
        assertEquals("", device.getBrand());
        assertEquals("", device.getModel());
        assertEquals(0.0, device.getConsumptionPerHour());
        assertEquals(DeviceStatus.OFF, device.getStatus());
        assertEquals(0, device.getTotalMinutesOn());
        assertEquals(0, device.getTotalActivations());
    }

    @Test
    void constructorStoresDeviceDetails() {
        TestDevice device = new TestDevice("Brand", "Model", 15.5);

        assertEquals("Brand", device.getBrand());
        assertEquals("Model", device.getModel());
        assertEquals(15.5, device.getConsumptionPerHour());
    }

    @Test
    void settersUpdateDeviceDetails() {
        TestDevice device = new TestDevice();

        device.setBrand("NewBrand");
        device.setModel("NewModel");
        device.setConsumptionPerHour(25.0);

        assertEquals("NewBrand", device.getBrand());
        assertEquals("NewModel", device.getModel());
        assertEquals(25.0, device.getConsumptionPerHour());
    }

    @Test
    void updateStatusCountsActivationsWhenMovingFromIdleToActive() {
        TestDevice device = new TestDevice();

        device.updateStatus(DeviceStatus.ON);
        device.updateStatus(DeviceStatus.ON);
        device.updateStatus(DeviceStatus.OFF);
        device.updateStatus(DeviceStatus.ON);

        assertEquals(2, device.getTotalActivations());
    }

    @Test
    void tickUpdatesMinutesAndEnergyOnlyWhenConsuming() {
        TestDevice device = new TestDevice("Brand", "Model", 120.0);

        device.tick(30);
        assertEquals(0, device.getTotalMinutesOn());

        device.setConsuming(true);
        device.tick(30);

        assertEquals(30, device.getTotalMinutesOn());
        assertEquals(60.0, device.getEnergyConsumption());
    }

    @Test
    void resetStatsClearsUsageWithoutChangingDetails() {
        TestDevice device = new TestDevice("Brand", "Model", 120.0);
        device.updateStatus(DeviceStatus.ON);
        device.setConsuming(true);
        device.tick(30);

        device.resetStats();

        assertEquals("Brand", device.getBrand());
        assertEquals("Model", device.getModel());
        assertEquals(120.0, device.getConsumptionPerHour());
        assertEquals(0, device.getTotalActivations());
        assertEquals(0, device.getTotalMinutesOn());
    }

    @Test
    void cloneKeepsDeviceFields() {
        TestDevice device = new TestDevice("Brand", "Model", 50.0);
        device.updateStatus(DeviceStatus.ON);
        device.setConsuming(true);
        device.tick(12);

        TestDevice copy = device.clone();

        assertNotSame(device, copy);
        assertEquals(device, copy);
        assertEquals(device.hashCode(), copy.hashCode());
    }

    @Test
    void toStringContainsMainDeviceFields() {
        TestDevice device = new TestDevice("Brand", "Model", 50.0);

        String text = device.toString();

        assertTrue(text.contains("TestDevice"));
        assertTrue(text.contains("Brand: Brand"));
        assertTrue(text.contains("Model: Model"));
        assertTrue(text.contains("Consumption: 50.0 Wh/h"));
        assertTrue(text.contains("Status: OFF"));
    }

    private static class TestDevice extends Device {
        private boolean consuming;

        TestDevice() {
            super();
        }

        TestDevice(String brand, String model, double consumptionPerHour) {
            super(brand, model, consumptionPerHour);
        }

        TestDevice(TestDevice device) {
            super(device);
            this.consuming = device.consuming;
        }

        void setConsuming(boolean consuming) {
            this.consuming = consuming;
        }

        @Override
        public boolean isConsuming() {
            return this.consuming;
        }

        @Override
        public TestDevice clone() {
            return new TestDevice(this);
        }
    }
}
