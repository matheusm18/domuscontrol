package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Plug;
import domuscontrol.devices.sensors.LuminositySensor;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class LuminositySensorConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesEqualsFiveHundredWithoutTargetSensor() {
        LuminositySensorCondition condition = new LuminositySensorCondition();

        assertEquals(-1, condition.getSensorId());
        assertEquals(500, condition.getTriggerLuminosity());
        assertEquals(Operator.EQUALS, condition.getOperator());
        assertFalse(condition.hasDeviceId(1));
    }

    @Test
    void constructorStoresSensorThresholdAndOperator() {
        LuminositySensorCondition condition =
            new LuminositySensorCondition(3, 700, Operator.GREATER_THAN);

        assertEquals(3, condition.getSensorId());
        assertEquals(700, condition.getTriggerLuminosity());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());
        assertTrue(condition.hasDeviceId(3));
    }

    @Test
    void nullOperatorFallsBackToEquals() {
        LuminositySensorCondition condition = new LuminositySensorCondition(3, 700, null);

        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void evaluateSupportsAllOperatorsWhenSensorIsOn() throws Exception {
        House house = houseWithDivision();
        LuminositySensor sensor = new LuminositySensor("Aqara", "Light", 0.1);
        house.addDeviceToDivision(sensor, "Kitchen");
        updateSensor(house, sensor.getId(), 700.4);

        assertTrue(new LuminositySensorCondition(sensor.getId(), 700, Operator.EQUALS).evaluate(house, null));
        assertTrue(new LuminositySensorCondition(sensor.getId(), 600, Operator.GREATER_THAN).evaluate(house, null));
        assertTrue(new LuminositySensorCondition(sensor.getId(), 800, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new LuminositySensorCondition(sensor.getId(), 900, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseWhenSensorIsOffMissingOrWrongType() throws Exception {
        House house = houseWithDivision();
        LuminositySensor sensor = new LuminositySensor("Aqara", "Light", 0.1);
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(sensor, "Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");
        updateSensor(house, sensor.getId(), 700.0);

        house.interactWithDevice(sensor.getId(), device -> ((LuminositySensor) device).turnOff());

        assertFalse(new LuminositySensorCondition(sensor.getId(), 700, Operator.EQUALS).evaluate(house, null));
        assertFalse(new LuminositySensorCondition(99, 700, Operator.EQUALS).evaluate(house, null));
        assertFalse(new LuminositySensorCondition(plug.getId(), 700, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        LuminositySensorCondition condition =
            new LuminositySensorCondition(3, 700, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        LuminositySensorCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassSensorThresholdAndOperator() {
        assertEquals(
            new LuminositySensorCondition(3, 700, Operator.EQUALS),
            new LuminositySensorCondition(3, 700, Operator.EQUALS)
        );
        assertNotEquals(
            new LuminositySensorCondition(3, 700, Operator.EQUALS),
            new LuminositySensorCondition(4, 700, Operator.EQUALS)
        );
        assertNotEquals(
            new LuminositySensorCondition(3, 700, Operator.EQUALS),
            new LuminositySensorCondition(3, 800, Operator.EQUALS)
        );
        assertNotEquals(
            new LuminositySensorCondition(3, 700, Operator.EQUALS),
            new LuminositySensorCondition(3, 700, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesSensorThresholdAndOperator() {
        String text = new LuminositySensorCondition(3, 700, Operator.GREATER_THAN).toString();

        assertTrue(text.contains("Sensor #3"));
        assertTrue(text.contains("luminosity GREATER_THAN 700"));
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }

    private void updateSensor(House house, int sensorId, double luminosity) throws Exception {
        house.interactWithDevice(sensorId, device ->
            ((LuminositySensor) device).updateFromState(state(luminosity))
        );
    }

    private SimulationState state(double luminosity) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationState(now, now.minusHours(1), 20.0, luminosity, WeatherCondition.SUNNY);
    }
}
