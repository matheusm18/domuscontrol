package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CurtainTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresOpeningAndOpensWhenAboveZero() {
        Curtain curtain = new Curtain("Ikea", "Tradfri", 20.0, 35);

        assertEquals("Ikea", curtain.getBrand());
        assertEquals("Tradfri", curtain.getModel());
        assertEquals(20.0, curtain.getConsumptionPerHour());
        assertEquals(35, curtain.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, curtain.getStatus());
        assertTrue(curtain.isOpen());
    }

    @Test
    void openFullyAndCloseFullyUpdateOpeningAndStatus() {
        Curtain curtain = new Curtain();

        curtain.openFully();
        assertEquals(100, curtain.getOpeningLevel());
        assertEquals(DeviceStatus.OPEN, curtain.getStatus());
        assertTrue(curtain.isConsuming());

        curtain.closeFully();
        assertEquals(0, curtain.getOpeningLevel());
        assertEquals(DeviceStatus.CLOSED, curtain.getStatus());
        assertFalse(curtain.isConsuming());
    }

    @Test
    void setOpeningClampsValues() {
        Curtain curtain = new Curtain();

        curtain.setOpening(120);
        assertEquals(100, curtain.getOpeningLevel());

        curtain.setOpening(-1);
        assertEquals(0, curtain.getOpeningLevel());
    }

    @Test
    void cloneKeepsStateAndOpening() {
        Curtain curtain = new Curtain("Ikea", "Tradfri", 20.0, 35);
        curtain.tick(10);

        Curtain copy = curtain.clone();

        assertNotSame(curtain, copy);
        assertEquals(curtain, copy);
        assertEquals(curtain.hashCode(), copy.hashCode());
        assertEquals(35, copy.getOpeningLevel());
        assertEquals(10, copy.getTotalMinutesOn());
    }
}
