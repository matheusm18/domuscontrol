package domuscontrol.routines.actions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Gate;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SetOpeningActionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorStartsWithoutTargetDeviceAndPercentageZero() {
        SetOpeningAction action = new SetOpeningAction();

        assertEquals(-1, action.getDeviceId());
        assertEquals(0, action.getTargetPercentage());
        assertFalse(action.hasDeviceId(1));
    }

    @Test
    void constructorAndSettersDefineTargetDeviceAndPercentage() {
        SetOpeningAction action = new SetOpeningAction(3, 45);

        assertEquals(3, action.getDeviceId());
        assertEquals(45, action.getTargetPercentage());

        action.setDeviceId(5);
        action.setTargetPercentage(80);

        assertEquals(5, action.getDeviceId());
        assertEquals(80, action.getTargetPercentage());
        assertTrue(action.hasDeviceId(5));
    }

    @Test
    void executeSetsOpeningOnOpenableDevice() throws Exception {
        House house = houseWithDivision();
        Gate gate = new Gate("Nice", "Road", 80.0, 0);
        house.addDeviceToDivision(gate, "Kitchen");

        new SetOpeningAction(gate.getId(), 75).execute(house);

        assertEquals(75, ((Gate) house.getDevice(gate.getId())).getOpeningLevel());
    }

    @Test
    void executeUsesDeviceClampingRules() throws Exception {
        House house = houseWithDivision();
        Gate gate = new Gate("Nice", "Road", 80.0, 0);
        house.addDeviceToDivision(gate, "Kitchen");

        new SetOpeningAction(gate.getId(), 150).execute(house);

        assertEquals(100, ((Gate) house.getDevice(gate.getId())).getOpeningLevel());
    }

    @Test
    void executeDoesNothingForNonOpenableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        new SetOpeningAction(plug.getId(), 75).execute(house);

        assertEquals(plug, house.getDevice(plug.getId()));
    }

    @Test
    void executeIgnoresMissingDevice() {
        House house = houseWithDivision();

        assertDoesNotThrow(() -> new SetOpeningAction(99, 75).execute(house));
    }

    @Test
    void copyAndCloneKeepDeviceIdAndTargetPercentage() {
        SetOpeningAction action = new SetOpeningAction(3, 45);

        Action copy = action.copy();
        SetOpeningAction clone = action.clone();

        assertNotSame(action, copy);
        assertEquals(action, copy);
        assertEquals(action.hashCode(), copy.hashCode());
        assertEquals(action, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceIdAndTargetPercentage() {
        assertEquals(new SetOpeningAction(3, 45), new SetOpeningAction(3, 45));
        assertNotEquals(new SetOpeningAction(3, 45), new SetOpeningAction(4, 45));
        assertNotEquals(new SetOpeningAction(3, 45), new SetOpeningAction(3, 50));
        assertNotEquals(new SetOpeningAction(3, 45), new TurnOnAction(3));
    }

    @Test
    void toStringIncludesDeviceIdAndTargetPercentage() {
        assertEquals(
            "SetOpeningAction { Device ID: 3, Target Percentage: 45 }",
            new SetOpeningAction(3, 45).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }
}
