package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GateTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresOpeningAndOpensWhenAboveZero() {
        Gate gate = new Gate("Nice", "Road", 80.0, 45);

        assertEquals("Nice", gate.getBrand());
        assertEquals("Road", gate.getModel());
        assertEquals(80.0, gate.getConsumptionPerHour());
        assertEquals(45, gate.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, gate.getStatus());
        assertTrue(gate.isOpen());
    }

    @Test
    void openFullyAndCloseFullyUpdateOpeningAndStatus() {
        Gate gate = new Gate();

        gate.openFully();
        assertEquals(100, gate.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, gate.getStatus());
        assertTrue(gate.isConsuming());

        gate.closeFully();
        assertEquals(0, gate.getOpeningLevel());
        assertEquals(DeviceStatus.CLOSED, gate.getStatus());
        assertFalse(gate.isConsuming());
    }

    @Test
    void setOpeningClampsValues() {
        Gate gate = new Gate();

        gate.setOpening(130);
        assertEquals(100, gate.getOpeningLevel());

        gate.setOpening(-10);
        assertEquals(0, gate.getOpeningLevel());
    }

    @Test
    void cloneKeepsStateAndOpening() {
        Gate gate = new Gate("Nice", "Road", 80.0, 45);
        gate.tick(10);

        Gate copy = gate.clone();

        assertNotSame(gate, copy);
        assertEquals(gate, copy);
        assertEquals(gate.hashCode(), copy.hashCode());
        assertEquals(45, copy.getOpeningLevel());
        assertEquals(10, copy.getTotalMinutesOn());
    }
}
