package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FanTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresSpeedAndTurnsOnWhenAboveZero() {
        Fan fan = new Fan("Dyson", "Air", 75.0, 30);

        assertEquals("Dyson", fan.getBrand());
        assertEquals("Air", fan.getModel());
        assertEquals(75.0, fan.getConsumptionPerHour());
        assertEquals(30, fan.getSpeed());
        assertTrue(fan.isOn());
    }

    @Test
    void speedControlsOnOffStateAndIsClamped() {
        Fan fan = new Fan();

        fan.setSpeed(150);
        assertEquals(100, fan.getSpeed());
        assertEquals(DeviceStatus.ON, fan.getStatus());

        fan.setSpeed(0);
        assertEquals(0, fan.getSpeed());
        assertEquals(DeviceStatus.OFF, fan.getStatus());
    }

    @Test
    void cloneKeepsStateAndSpeed() {
        Fan fan = new Fan("Dyson", "Air", 75.0, 30);
        fan.tick(20);

        Fan copy = fan.clone();

        assertNotSame(fan, copy);
        assertEquals(fan, copy);
        assertEquals(fan.hashCode(), copy.hashCode());
        assertEquals(30, copy.getSpeed());
        assertEquals(20, copy.getTotalMinutesOn());
    }
}
