package domuscontrol.routines.actions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SetLevelActionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorStartsWithoutTargetDeviceAndLevelZero() {
        SetLevelAction action = new SetLevelAction();

        assertEquals(-1, action.getDeviceId());
        assertEquals(0, action.getTargetLevel());
        assertFalse(action.hasDeviceId(1));
    }

    @Test
    void constructorAndSettersDefineTargetDeviceAndLevel() {
        SetLevelAction action = new SetLevelAction(3, 45);

        assertEquals(3, action.getDeviceId());
        assertEquals(45, action.getTargetLevel());

        action.setDeviceId(5);
        action.setTargetLevel(80);

        assertEquals(5, action.getDeviceId());
        assertEquals(80, action.getTargetLevel());
        assertTrue(action.hasDeviceId(5));
    }

    @Test
    void executeSetsLevelOnAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 10, 3000);
        house.addDeviceToDivision(lamp, "Kitchen");

        new SetLevelAction(lamp.getId(), 75).execute(house);

        assertEquals(75, ((Lamp) house.getDevice(lamp.getId())).getBrightness());
    }

    @Test
    void executeUsesDeviceClampingRules() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 10, 3000);
        house.addDeviceToDivision(lamp, "Kitchen");

        new SetLevelAction(lamp.getId(), 150).execute(house);

        assertEquals(100, ((Lamp) house.getDevice(lamp.getId())).getBrightness());
    }

    @Test
    void executeDoesNothingForNonAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        new SetLevelAction(plug.getId(), 75).execute(house);

        assertEquals(plug, house.getDevice(plug.getId()));
    }

    @Test
    void executeIgnoresMissingDevice() {
        House house = houseWithDivision();

        assertDoesNotThrow(() -> new SetLevelAction(99, 75).execute(house));
    }

    @Test
    void copyAndCloneKeepDeviceIdAndTargetLevel() {
        SetLevelAction action = new SetLevelAction(3, 45);

        Action copy = action.copy();
        SetLevelAction clone = action.clone();

        assertNotSame(action, copy);
        assertEquals(action, copy);
        assertEquals(action.hashCode(), copy.hashCode());
        assertEquals(action, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceIdAndTargetLevel() {
        assertEquals(new SetLevelAction(3, 45), new SetLevelAction(3, 45));
        assertNotEquals(new SetLevelAction(3, 45), new SetLevelAction(4, 45));
        assertNotEquals(new SetLevelAction(3, 45), new SetLevelAction(3, 50));
        assertNotEquals(new SetLevelAction(3, 45), new TurnOnAction(3));
    }

    @Test
    void toStringIncludesDeviceIdAndTargetLevel() {
        assertEquals(
            "SetLevelAction { Device ID: 3, Target Level: 45 }",
            new SetLevelAction(3, 45).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }
}
