package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TelevisionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresVolumeAndSourceWithoutTurningOn() {
        Television television = new Television("Samsung", "Frame", 140.0, 35, "HDMI");

        assertEquals("Samsung", television.getBrand());
        assertEquals("Frame", television.getModel());
        assertEquals(140.0, television.getConsumptionPerHour());
        assertEquals(35, television.getVolume());
        assertEquals("HDMI", television.getSource());
        assertFalse(television.isOn());
    }

    @Test
    void volumeIsClampedAndSourceCanBeChanged() {
        Television television = new Television();

        television.setVolume(200);
        television.setSource("Netflix");

        assertEquals(100, television.getVolume());
        assertEquals("Netflix", television.getSource());

        television.setVolume(-10);
        assertEquals(0, television.getVolume());
    }

    @Test
    void turnOnAllowsConsumptionAndTick() {
        Television television = new Television("Samsung", "Frame", 120.0, 35, "HDMI");

        television.turnOn();
        television.tick(30);

        assertTrue(television.isConsuming());
        assertEquals(30, television.getTotalMinutesOn());
        assertEquals(60.0, television.getEnergyConsumption());
    }

    @Test
    void cloneKeepsStateVolumeAndSource() {
        Television television = new Television("Samsung", "Frame", 140.0, 35, "HDMI");
        television.turnOn();

        Television copy = television.clone();

        assertNotSame(television, copy);
        assertEquals(television, copy);
        assertEquals(television.hashCode(), copy.hashCode());
        assertEquals(35, copy.getVolume());
        assertEquals("HDMI", copy.getSource());
    }

    @Test
    void toStringIncludesSource() {
        Television television = new Television("Samsung", "Frame", 140.0, 35, "HDMI");

        assertTrue(television.toString().contains("Source: HDMI"));
    }
}
