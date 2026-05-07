package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelayTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresDetailsAndStartsOff() {
        Relay relay = new Relay("Shelly", "1PM", 2.0);

        assertEquals("Shelly", relay.getBrand());
        assertEquals("1PM", relay.getModel());
        assertEquals(2.0, relay.getConsumptionPerHour());
        assertEquals(DeviceStatus.OFF, relay.getStatus());
        assertFalse(relay.isConsuming());
    }

    @Test
    void turnOnAndTurnOffControlConsumption() {
        Relay relay = new Relay("Shelly", "1PM", 60.0);

        relay.turnOn();
        relay.tick(15);

        assertTrue(relay.isOn());
        assertTrue(relay.isConsuming());
        assertEquals(15, relay.getTotalMinutesOn());
        assertEquals(15.0, relay.getEnergyConsumption());

        relay.turnOff();
        assertFalse(relay.isConsuming());
    }

    @Test
    void cloneKeepsState() {
        Relay relay = new Relay("Shelly", "1PM", 2.0);
        relay.turnOn();

        Relay copy = relay.clone();

        assertNotSame(relay, copy);
        assertEquals(relay, copy);
        assertEquals(relay.hashCode(), copy.hashCode());
    }
}
