package domuscontrol.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirConditionerTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void constructorStoresCoolingPowerAndTurnsOnWhenAboveZero() {
        AirConditioner airConditioner = new AirConditioner("Daikin", "Cool", 900.0, 40);

        assertEquals("Daikin", airConditioner.getBrand());
        assertEquals("Cool", airConditioner.getModel());
        assertEquals(900.0, airConditioner.getConsumptionPerHour());
        assertEquals(40, airConditioner.getCoolingPower());
        assertTrue(airConditioner.isOn());
        assertTrue(airConditioner.isConsuming());
    }

    @Test
    void coolingPowerControlsOnOffStateAndIsClamped() {
        AirConditioner airConditioner = new AirConditioner();

        airConditioner.setCoolingPower(130);
        assertEquals(100, airConditioner.getCoolingPower());
        assertEquals(DeviceStatus.ON, airConditioner.getStatus());

        airConditioner.setCoolingPower(-5);
        assertEquals(0, airConditioner.getCoolingPower());
        assertEquals(DeviceStatus.OFF, airConditioner.getStatus());
    }

    @Test
    void cloneKeepsStateAndCoolingPower() {
        AirConditioner airConditioner = new AirConditioner("Daikin", "Cool", 900.0, 40);
        airConditioner.tick(10);

        AirConditioner copy = airConditioner.clone();

        assertNotSame(airConditioner, copy);
        assertEquals(airConditioner, copy);
        assertEquals(airConditioner.hashCode(), copy.hashCode());
        assertEquals(40, copy.getCoolingPower());
        assertEquals(10, copy.getTotalMinutesOn());
    }
}
