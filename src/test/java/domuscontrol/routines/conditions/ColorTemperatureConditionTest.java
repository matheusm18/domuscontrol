package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorTemperatureConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesEquals2700WithoutTargetDevice() {
        ColorTemperatureCondition condition = new ColorTemperatureCondition();

        assertEquals(-1, condition.getDeviceId());
        assertEquals(2700, condition.getTriggerTemperature());
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void constructorAndSettersDefineTargetTemperatureAndOperator() {
        ColorTemperatureCondition condition =
            new ColorTemperatureCondition(3, 3200, Operator.GREATER_THAN);

        assertEquals(3, condition.getDeviceId());
        assertEquals(3200, condition.getTriggerTemperature());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());

        condition.setDeviceId(5);
        condition.setTriggerTemperature(3500);
        condition.setOperator(Operator.LESS_THAN);

        assertEquals(5, condition.getDeviceId());
        assertEquals(3500, condition.getTriggerTemperature());
        assertEquals(Operator.LESS_THAN, condition.getOperator());
        assertTrue(condition.hasDeviceId(5));
    }

    @Test
    void nullOperatorFallsBackToEquals() {
        ColorTemperatureCondition condition = new ColorTemperatureCondition(3, 3200, null);

        assertEquals(Operator.EQUALS, condition.getOperator());

        condition.setOperator(null);
        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void evaluateSupportsAllOperatorsForColorAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 50, 3200);
        house.addDeviceToDivision(lamp, "Kitchen");

        assertTrue(new ColorTemperatureCondition(lamp.getId(), 3200, Operator.EQUALS).evaluate(house, null));
        assertTrue(new ColorTemperatureCondition(lamp.getId(), 3000, Operator.GREATER_THAN).evaluate(house, null));
        assertTrue(new ColorTemperatureCondition(lamp.getId(), 3500, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new ColorTemperatureCondition(lamp.getId(), 2700, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseForMissingOrNonColorAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        assertFalse(new ColorTemperatureCondition(99, 3200, Operator.EQUALS).evaluate(house, null));
        assertFalse(new ColorTemperatureCondition(plug.getId(), 3200, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        ColorTemperatureCondition condition =
            new ColorTemperatureCondition(3, 3200, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        ColorTemperatureCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceTemperatureAndOperator() {
        assertEquals(
            new ColorTemperatureCondition(3, 3200, Operator.EQUALS),
            new ColorTemperatureCondition(3, 3200, Operator.EQUALS)
        );
        assertNotEquals(
            new ColorTemperatureCondition(3, 3200, Operator.EQUALS),
            new ColorTemperatureCondition(4, 3200, Operator.EQUALS)
        );
        assertNotEquals(
            new ColorTemperatureCondition(3, 3200, Operator.EQUALS),
            new ColorTemperatureCondition(3, 3500, Operator.EQUALS)
        );
        assertNotEquals(
            new ColorTemperatureCondition(3, 3200, Operator.EQUALS),
            new ColorTemperatureCondition(3, 3200, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesDeviceTemperatureAndOperator() {
        assertEquals(
            "ColorTemperatureCondition { Device ID: 3, Temperature GREATER_THAN 3200K }",
            new ColorTemperatureCondition(3, 3200, Operator.GREATER_THAN).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }
}
