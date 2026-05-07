package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpeakerTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresVolumeAndSourceWithoutTurningOn() {
        Speaker speaker = new Speaker("Sonos", "One", 25.0, 45, "Bluetooth");

        assertEquals("Sonos", speaker.getBrand());
        assertEquals("One", speaker.getModel());
        assertEquals(25.0, speaker.getConsumptionPerHour());
        assertEquals(45, speaker.getVolume());
        assertEquals("Bluetooth", speaker.getSource());
        assertFalse(speaker.isOn());
    }

    @Test
    void volumeIsClampedAndSourceCanBeChanged() {
        Speaker speaker = new Speaker();

        speaker.setVolume(120);
        speaker.setSource("Radio");

        assertEquals(100, speaker.getVolume());
        assertEquals("Radio", speaker.getSource());

        speaker.setVolume(-2);
        assertEquals(0, speaker.getVolume());
    }

    @Test
    void turnOnAllowsConsumptionAndTick() {
        Speaker speaker = new Speaker("Sonos", "One", 60.0, 45, "Bluetooth");

        speaker.turnOn();
        speaker.tick(15);

        assertTrue(speaker.isConsuming());
        assertEquals(15, speaker.getTotalMinutesOn());
        assertEquals(15.0, speaker.getEnergyConsumption());
    }

    @Test
    void cloneKeepsStateVolumeAndSource() {
        Speaker speaker = new Speaker("Sonos", "One", 25.0, 45, "Bluetooth");
        speaker.turnOn();

        Speaker copy = speaker.clone();

        assertNotSame(speaker, copy);
        assertEquals(speaker, copy);
        assertEquals(speaker.hashCode(), copy.hashCode());
        assertEquals(45, copy.getVolume());
        assertEquals("Bluetooth", copy.getSource());
    }

    @Test
    void toStringIncludesSource() {
        Speaker speaker = new Speaker("Sonos", "One", 25.0, 45, "Bluetooth");

        assertTrue(speaker.toString().contains("Source: Bluetooth"));
    }
}
