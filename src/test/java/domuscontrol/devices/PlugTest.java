package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlugTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresDetailsAndStartsOff() {
        Plug plug = new Plug("TP-Link", "P100", 3.0);

        assertEquals("TP-Link", plug.getBrand());
        assertEquals("P100", plug.getModel());
        assertEquals(3.0, plug.getConsumptionPerHour());
        assertEquals(DeviceStatus.OFF, plug.getStatus());
        assertFalse(plug.isConsuming());
    }

    @Test
    void turnOnAndTurnOffControlConsumption() {
        Plug plug = new Plug("TP-Link", "P100", 60.0);

        plug.turnOn();
        plug.tick(30);

        assertTrue(plug.isOn());
        assertTrue(plug.isConsuming());
        assertEquals(30, plug.getTotalMinutesOn());
        assertEquals(30.0, plug.getEnergyConsumption());

        plug.turnOff();
        assertFalse(plug.isConsuming());
    }

    @Test
    void cloneKeepsState() {
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        plug.turnOn();

        Plug copy = plug.clone();

        assertNotSame(plug, copy);
        assertEquals(plug, copy);
        assertEquals(plug.hashCode(), copy.hashCode());
    }
}
