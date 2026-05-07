package domuscontrol.devices.types;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorAdjustableDeviceTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void lampImplementsColorAdjustableDevice() {
        Lamp lamp = new Lamp();

        assertInstanceOf(ColorAdjustableDevice.class, lamp);
    }

    @Test
    void colorTemperatureCanBeReadAndUpdatedThroughInterface() {
        ColorAdjustableDevice device = new Lamp("Brand", "Model", 8.0, 50, 3000);

        assertEquals(3000, device.getColorTemperature());

        device.setColorTemperature(3500);

        assertEquals(3500, device.getColorTemperature());
    }

    @Test
    void lampClampsColorTemperatureToSupportedRange() {
        ColorAdjustableDevice device = new Lamp();

        device.setColorTemperature(1000);
        assertEquals(2700, device.getColorTemperature());

        device.setColorTemperature(5000);
        assertEquals(4000, device.getColorTemperature());
    }

    @Test
    void interfaceExposesDeviceId() {
        ColorAdjustableDevice device = new Lamp();

        assertEquals(1, device.getId());
    }
}
