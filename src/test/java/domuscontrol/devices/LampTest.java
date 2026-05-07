package domuscontrol.devices;

import domuscontrol.devices.types.ColorAdjustableDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LampTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresBrightnessColorTemperatureAndTurnsOn() {
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 70, 3200);

        assertEquals("Philips", lamp.getBrand());
        assertEquals("Hue", lamp.getModel());
        assertEquals(9.0, lamp.getConsumptionPerHour());
        assertEquals(70, lamp.getBrightness());
        assertEquals(3200, lamp.getColorTemperature());
        assertTrue(lamp.isOn());
        assertTrue(lamp instanceof ColorAdjustableDevice);
    }

    @Test
    void brightnessControlsOnOffStateAndIsClamped() {
        Lamp lamp = new Lamp();

        lamp.setBrightness(150);
        assertEquals(100, lamp.getBrightness());
        assertEquals(DeviceStatus.ON, lamp.getStatus());

        lamp.setBrightness(0);
        assertEquals(0, lamp.getBrightness());
        assertEquals(DeviceStatus.OFF, lamp.getStatus());
    }

    @Test
    void colorTemperatureIsClampedToSupportedRange() {
        Lamp lamp = new Lamp();

        lamp.setColorTemperature(1000);
        assertEquals(2700, lamp.getColorTemperature());

        lamp.setColorTemperature(5000);
        assertEquals(4000, lamp.getColorTemperature());

        lamp.setColorTemperature(3500);
        assertEquals(3500, lamp.getColorTemperature());
    }

    @Test
    void cloneKeepsStateBrightnessAndColorTemperature() {
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 70, 3200);
        lamp.tick(15);

        Lamp copy = lamp.clone();

        assertNotSame(lamp, copy);
        assertEquals(lamp, copy);
        assertEquals(lamp.hashCode(), copy.hashCode());
        assertEquals(70, copy.getBrightness());
        assertEquals(3200, copy.getColorTemperature());
        assertEquals(15, copy.getTotalMinutesOn());
    }

    @Test
    void toStringIncludesColorTemperature() {
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 70, 3200);

        assertTrue(lamp.toString().contains("Color Temp: 3200K"));
    }
}
