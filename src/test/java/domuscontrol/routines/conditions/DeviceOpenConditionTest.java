package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Gate;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceOpenConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesEqualsZeroWithoutTargetDevice() {
        DeviceOpenCondition condition = new DeviceOpenCondition();

        assertEquals(-1, condition.getDeviceId());
        assertEquals(0, condition.getTriggerLevel());
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void constructorAndSettersDefineTargetLevelAndOperator() {
        DeviceOpenCondition condition = new DeviceOpenCondition(3, 50, Operator.GREATER_THAN);

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
        DeviceOpenCondition condition = new DeviceOpenCondition(3, 50, null);

        assertEquals(Operator.EQUALS, condition.getOperator());

        condition.setOperator(null);
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void evaluateSupportsAllOperatorsForOpenableDevice() throws Exception {
        House house = houseWithDivision();
        Gate gate = new Gate("Nice", "Road", 80.0, 50);
        house.addDeviceToDivision(gate, "Kitchen");

        assertTrue(new DeviceOpenCondition(gate.getId(), 50, Operator.EQUALS).evaluate(house, null));
        assertTrue(new DeviceOpenCondition(gate.getId(), 40, Operator.GREATER_THAN).evaluate(house, null));
        assertTrue(new DeviceOpenCondition(gate.getId(), 60, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new DeviceOpenCondition(gate.getId(), 70, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseForMissingOrNonOpenableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        assertFalse(new DeviceOpenCondition(99, 50, Operator.EQUALS).evaluate(house, null));
        assertFalse(new DeviceOpenCondition(plug.getId(), 50, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        DeviceOpenCondition condition = new DeviceOpenCondition(3, 50, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        DeviceOpenCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceLevelAndOperator() {
        assertEquals(
            new DeviceOpenCondition(3, 50, Operator.EQUALS),
            new DeviceOpenCondition(3, 50, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceOpenCondition(3, 50, Operator.EQUALS),
            new DeviceOpenCondition(4, 50, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceOpenCondition(3, 50, Operator.EQUALS),
            new DeviceOpenCondition(3, 60, Operator.EQUALS)
        );
        assertNotEquals(
            new DeviceOpenCondition(3, 50, Operator.EQUALS),
            new DeviceOpenCondition(3, 50, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesDeviceOpeningAndOperator() {
        assertEquals(
            "DeviceOpenCondition { Device ID: 3, Opening GREATER_THAN 50% }",
            new DeviceOpenCondition(3, 50, Operator.GREATER_THAN).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }
}
