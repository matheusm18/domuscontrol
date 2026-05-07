package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeaterTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresPowerAndTurnsOnWhenAboveZero() {
        Heater heater = new Heater("Bosch", "Heat", 1000.0, 60);

        assertEquals("Bosch", heater.getBrand());
        assertEquals("Heat", heater.getModel());
        assertEquals(1000.0, heater.getConsumptionPerHour());
        assertEquals(60, heater.getPower());
        assertTrue(heater.isOn());
    }

    @Test
    void powerControlsOnOffStateAndIsClamped() {
        Heater heater = new Heater();

        heater.setPower(200);
        assertEquals(100, heater.getPower());
        assertEquals(DeviceStatus.ON, heater.getStatus());

        heater.setPower(-1);
        assertEquals(0, heater.getPower());
        assertEquals(DeviceStatus.OFF, heater.getStatus());
    }

    @Test
    void cloneKeepsStateAndPower() {
        Heater heater = new Heater("Bosch", "Heat", 1000.0, 60);
        heater.tick(25);

        Heater copy = heater.clone();

        assertNotSame(heater, copy);
        assertEquals(heater, copy);
        assertEquals(heater.hashCode(), copy.hashCode());
        assertEquals(60, copy.getPower());
        assertEquals(25, copy.getTotalMinutesOn());
    }
}
