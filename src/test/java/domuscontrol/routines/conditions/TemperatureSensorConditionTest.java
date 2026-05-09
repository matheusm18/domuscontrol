package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Plug;
import domuscontrol.devices.sensors.TemperatureSensor;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TemperatureSensorConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesEqualsTwentyWithoutTargetSensor() {
        TemperatureSensorCondition condition = new TemperatureSensorCondition();

        assertEquals(-1, condition.getSensorId());
        assertEquals(20, condition.getTriggerTemperature());
        assertEquals(Operator.EQUALS, condition.getOperator());
        assertFalse(condition.hasDeviceId(1));
    }

    @Test
    void constructorStoresSensorThresholdAndOperator() {
        TemperatureSensorCondition condition =
            new TemperatureSensorCondition(3, 22, Operator.GREATER_THAN);

        assertEquals(3, condition.getSensorId());
        assertEquals(22, condition.getTriggerTemperature());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());
        assertTrue(condition.hasDeviceId(3));
    }

    @Test
    void nullOperatorFallsBackToEquals() {
        TemperatureSensorCondition condition = new TemperatureSensorCondition(3, 22, null);

        assertEquals(Operator.EQUALS, condition.getOperator());
    }

    @Test
    void evaluateSupportsAllOperatorsWhenSensorIsOn() throws Exception {
        House house = houseWithDivision();
        TemperatureSensor sensor = new TemperatureSensor("Aqara", "Temp", 0.1);
        house.addDeviceToDivision(sensor, "Kitchen");
        updateSensor(house, sensor.getId(), 22.4);

        assertTrue(new TemperatureSensorCondition(sensor.getId(), 22, Operator.EQUALS).evaluate(house, null));
        assertTrue(new TemperatureSensorCondition(sensor.getId(), 20, Operator.GREATER_THAN).evaluate(house, null));
        assertTrue(new TemperatureSensorCondition(sensor.getId(), 25, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new TemperatureSensorCondition(sensor.getId(), 30, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseWhenSensorIsOffMissingOrWrongType() throws Exception {
        House house = houseWithDivision();
        TemperatureSensor sensor = new TemperatureSensor("Aqara", "Temp", 0.1);
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(sensor, "Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");
        updateSensor(house, sensor.getId(), 22.0);

        house.interactWithDevice(sensor.getId(), device -> ((TemperatureSensor) device).turnOff());

        assertFalse(new TemperatureSensorCondition(sensor.getId(), 22, Operator.EQUALS).evaluate(house, null));
        assertFalse(new TemperatureSensorCondition(99, 22, Operator.EQUALS).evaluate(house, null));
        assertFalse(new TemperatureSensorCondition(plug.getId(), 22, Operator.EQUALS).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        TemperatureSensorCondition condition =
            new TemperatureSensorCondition(3, 22, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        TemperatureSensorCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassSensorThresholdAndOperator() {
        assertEquals(
            new TemperatureSensorCondition(3, 22, Operator.EQUALS),
            new TemperatureSensorCondition(3, 22, Operator.EQUALS)
        );
        assertNotEquals(
            new TemperatureSensorCondition(3, 22, Operator.EQUALS),
            new TemperatureSensorCondition(4, 22, Operator.EQUALS)
        );
        assertNotEquals(
            new TemperatureSensorCondition(3, 22, Operator.EQUALS),
            new TemperatureSensorCondition(3, 23, Operator.EQUALS)
        );
        assertNotEquals(
            new TemperatureSensorCondition(3, 22, Operator.EQUALS),
            new TemperatureSensorCondition(3, 22, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesSensorThresholdAndOperator() {
        String text = new TemperatureSensorCondition(3, 22, Operator.GREATER_THAN).toString();

        assertTrue(text.contains("Sensor #3"));
        assertTrue(text.contains("temperature GREATER_THAN 22"));
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }

    private void updateSensor(House house, int sensorId, double temperature) throws Exception {
        house.interactWithDevice(sensorId, device ->
            ((TemperatureSensor) device).updateFromState(state(temperature))
        );
    }

    private SimulationState state(double temperature) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationStateStub(now, now.minusHours(1), temperature, 500.0, WeatherCondition.SUNNY);
    }
}
