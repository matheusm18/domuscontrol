package domuscontrol.routines.actions;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Plug;
import domuscontrol.devices.sensors.TemperatureSensor;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TurnOnActionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorStartsWithoutTargetDevice() {
        TurnOnAction action = new TurnOnAction();

        assertEquals(-1, action.getDeviceId());
        assertFalse(action.hasDeviceId(1));
    }

    @Test
    void constructorAndSetterDefineTargetDevice() {
        TurnOnAction action = new TurnOnAction(3);

        assertEquals(3, action.getDeviceId());
        assertTrue(action.hasDeviceId(3));

        action.setDeviceId(5);
        assertEquals(5, action.getDeviceId());
        assertTrue(action.hasDeviceId(5));
    }

    @Test
    void executeTurnsOnSwitchableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        new TurnOnAction(plug.getId()).execute(house);

        assertEquals(DeviceStatus.ON, house.getDevice(plug.getId()).getStatus());
    }

    @Test
    void executeIgnoresMissingDevice() {
        House house = houseWithDivision();

        assertDoesNotThrow(() -> new TurnOnAction(99).execute(house));
    }

    @Test
    void copyAndCloneKeepDeviceId() {
        TurnOnAction action = new TurnOnAction(3);

        Action copy = action.copy();
        TurnOnAction clone = action.clone();

        assertNotSame(action, copy);
        assertEquals(action, copy);
        assertEquals(action.hashCode(), copy.hashCode());
        assertEquals(action, clone);
    }

    @Test
    void equalsRequiresSameClassAndDeviceId() {
        assertEquals(new TurnOnAction(3), new TurnOnAction(3));
        assertNotEquals(new TurnOnAction(3), new TurnOnAction(4));
        assertNotEquals(new TurnOnAction(3), new TurnOffAction(3));
    }

    @Test
    void toStringIncludesDeviceId() {
        assertEquals("TurnOnAction { Device ID: 3 }", new TurnOnAction(3).toString());
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }
}
