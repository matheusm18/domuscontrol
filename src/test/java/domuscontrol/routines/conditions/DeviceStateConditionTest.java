package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Gate;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceStateConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorTargetsNoDeviceAndTriggersWhenOn() {
        DeviceStateCondition condition = new DeviceStateCondition();

        assertEquals(-1, condition.getDeviceId());
        assertTrue(condition.getTriggerWhenOn());
        assertFalse(condition.hasDeviceId(1));
    }

    @Test
    void constructorAndSettersDefineTargetAndState() {
        DeviceStateCondition condition = new DeviceStateCondition(3, false);

        assertEquals(3, condition.getDeviceId());
        assertFalse(condition.getTriggerWhenOn());

        condition.setDeviceId(5);
        condition.setTriggerWhenOn(true);

        assertEquals(5, condition.getDeviceId());
        assertTrue(condition.getTriggerWhenOn());
        assertTrue(condition.hasDeviceId(5));
    }

    @Test
    void evaluateMatchesSwitchableDeviceState() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        assertTrue(new DeviceStateCondition(plug.getId(), false).evaluate(house, null));

        house.interactWithDevice(plug.getId(), device -> ((Plug) device).turnOn());

        assertTrue(new DeviceStateCondition(plug.getId(), true).evaluate(house, null));
        assertFalse(new DeviceStateCondition(plug.getId(), false).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseForMissingOrNonSwitchableDevice() throws Exception {
        House house = houseWithDivision();
        Gate gate = new Gate("Nice", "Road", 80.0, 50);
        house.addDeviceToDivision(gate, "Kitchen");

        assertFalse(new DeviceStateCondition(99, true).evaluate(house, null));
        assertFalse(new DeviceStateCondition(gate.getId(), true).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        DeviceStateCondition condition = new DeviceStateCondition(3, true);

        Condition copy = condition.copy();
        DeviceStateCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceAndState() {
        assertEquals(new DeviceStateCondition(3, true), new DeviceStateCondition(3, true));
        assertNotEquals(new DeviceStateCondition(3, true), new DeviceStateCondition(4, true));
        assertNotEquals(new DeviceStateCondition(3, true), new DeviceStateCondition(3, false));
        assertNotEquals(new DeviceStateCondition(3, true), new TimeCondition());
    }

    @Test
    void toStringIncludesDeviceAndState() {
        assertEquals(
            "DeviceStateCondition { Device ID: 3, Trigger when ON: true }",
            new DeviceStateCondition(3, true).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }
}
