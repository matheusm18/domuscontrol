package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceLevelConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesEqualsZeroWithoutTargetDevice() {
        DeviceLevelCondition condition = new DeviceLevelCondition();

        assertEquals(-1, condition.getDeviceId());
        assertEquals(0, condition.getTriggerLevel());
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void constructorAndSettersDefineTargetLevelAndOperator() {
        DeviceLevelCondition condition = new DeviceLevelCondition(3, 50, Operator.GREATER_THAN);

        assertEquals(3, condition.getDeviceId());
        assertEquals(50, condition.getTriggerLevel());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());

        condition.setDeviceId(5);
        condition.setTriggerLevel(70);
        condition.setOperator(Operator.LESS_THAN);

        assertEquals(5, condition.getDeviceId());
        assertEquals(70, condition.getTriggerLevel());
        assertEquals(Operator.LESS_THAN, condition.getOperator());
        assertTrue(condition.hasDeviceId(5));
    }

    @Test
    void nullOperatorFallsBackToEquals() {
        DeviceLevelCondition condition = new DeviceLevelCondition(3, 50, null);

        assertEquals(Operator.EQUALS, condition.getOperator());

        condition.setOperator(null);
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void evaluateSupportsAllOperatorsForAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 50, 3000);
        house.addDeviceToDivision(lamp, "Kitchen");

        assertTrue(new DeviceLevelCondition(lamp.getId(), 50, Operator.EQUALS).evaluate(house, null));
        assertTrue(new DeviceLevelCondition(lamp.getId(), 40, Operator.GREATER_THAN).evaluate(house, null));
        assertTrue(new DeviceLevelCondition(lamp.getId(), 60, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new DeviceLevelCondition(lamp.getId(), 70, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseForMissingOrNonAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        assertFalse(new DeviceLevelCondition(99, 50, Operator.EQUALS).evaluate(house, null));
        assertFalse(new DeviceLevelCondition(plug.getId(), 50, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        DeviceLevelCondition condition = new DeviceLevelCondition(3, 50, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        DeviceLevelCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceLevelAndOperator() {
        assertEquals(
            new DeviceLevelCondition(3, 50, Operator.EQUALS),
            new DeviceLevelCondition(3, 50, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceLevelCondition(3, 50, Operator.EQUALS),
            new DeviceLevelCondition(4, 50, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceLevelCondition(3, 50, Operator.EQUALS),
            new DeviceLevelCondition(3, 60, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceLevelCondition(3, 50, Operator.EQUALS),
            new DeviceLevelCondition(3, 50, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesDeviceLevelAndOperator() {
        assertEquals(
            "DeviceLevelCondition { Device ID: 3, Level GREATER_THAN 50 }",
            new DeviceLevelCondition(3, 50, Operator.GREATER_THAN).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }
}
