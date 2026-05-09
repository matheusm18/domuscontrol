package domuscontrol.routines.conditions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Plug;
import domuscontrol.devices.sensors.RainfallSensor;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RainfallSensorConditionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorUsesGreaterThanFiveWithoutTargetSensor() {
        RainfallSensorCondition condition = new RainfallSensorCondition();

        assertEquals(-1, condition.getSensorId());
        assertEquals(5.0, condition.getTriggerRainfall());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());
        assertFalse(condition.hasDeviceId(1));
    }

    @Test
    void constructorStoresSensorThresholdAndOperator() {
        RainfallSensorCondition condition =
            new RainfallSensorCondition(3, 12.5, Operator.LESS_THAN);

        assertEquals(3, condition.getSensorId());
        assertEquals(12.5, condition.getTriggerRainfall());
        assertEquals(Operator.LESS_THAN, condition.getOperator());
        assertTrue(condition.hasDeviceId(3));
    }

    @Test
    void nullOperatorFallsBackToGreaterThan() {
        RainfallSensorCondition condition = new RainfallSensorCondition(3, 12.5, null);

        assertEquals(Operator.GREATER_THAN, condition.getOperator());
    }

    @Test
    void evaluateSupportsEqualsAndLessThanForClearWeatherReading() throws Exception {
        House house = houseWithDivision();
        RainfallSensor sensor = new RainfallSensor("Aqara", "Rain", 0.1);
        house.addDeviceToDivision(sensor, "Kitchen");
        updateSensor(house, sensor.getId(), WeatherCondition.SUNNY);

        assertTrue(new RainfallSensorCondition(sensor.getId(), 0.0, Operator.EQUALS).evaluate(house, null));
        assertTrue(new RainfallSensorCondition(sensor.getId(), 1.0, Operator.LESS_THAN).evaluate(house, null));
        assertFalse(new RainfallSensorCondition(sensor.getId(), 1.0, Operator.GREATER_THAN).evaluate(house, null));
    }

    @Test
    void evaluateSupportsGreaterThanForStormyReading() throws Exception {
        House house = houseWithDivision();
        RainfallSensor sensor = new RainfallSensor("Aqara", "Rain", 0.1);
        house.addDeviceToDivision(sensor, "Kitchen");
        updateSensor(house, sensor.getId(), WeatherCondition.STORMY);

        assertTrue(new RainfallSensorCondition(sensor.getId(), 10.0, Operator.GREATER_THAN).evaluate(house, null));
    }

    @Test
    void evaluateReturnsFalseWhenSensorIsOffMissingOrWrongType() throws Exception {
        House house = houseWithDivision();
        RainfallSensor sensor = new RainfallSensor("Aqara", "Rain", 0.1);
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(sensor, "Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");
        updateSensor(house, sensor.getId(), WeatherCondition.STORMY);

        house.interactWithDevice(sensor.getId(), device -> ((RainfallSensor) device).turnOff());

        assertFalse(new RainfallSensorCondition(sensor.getId(), 10.0, Operator.GREATER_THAN).evaluate(house, null));
        assertFalse(new RainfallSensorCondition(99, 10.0, Operator.GREATER_THAN).evaluate(house, null));
        assertFalse(new RainfallSensorCondition(plug.getId(), 10.0, Operator.GREATER_THAN).evaluate(house, null));
    }

    @Test
    void copyAndCloneKeepFields() {
        RainfallSensorCondition condition =
            new RainfallSensorCondition(3, 12.5, Operator.GREATER_THAN);

        Condition copy = condition.copy();
        RainfallSensorCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassSensorThresholdAndOperator() {
        assertEquals(
            new RainfallSensorCondition(3, 12.5, Operator.EQUALS),
            new RainfallSensorCondition(3, 12.5, Operator.EQUALS)
        );
        assertNotEquals(
            new RainfallSensorCondition(3, 12.5, Operator.EQUALS),
            new RainfallSensorCondition(4, 12.5, Operator.EQUALS)
        );
        assertNotEquals(
            new RainfallSensorCondition(3, 12.5, Operator.EQUALS),
            new RainfallSensorCondition(3, 13.0, Operator.EQUALS)
        );
        assertNotEquals(
            new RainfallSensorCondition(3, 12.5, Operator.EQUALS),
            new RainfallSensorCondition(3, 12.5, Operator.LESS_THAN)
        );
    }

    @Test
    void toStringIncludesSensorThresholdAndOperator() {
        String text = new RainfallSensorCondition(3, 12.5, Operator.GREATER_THAN).toString();

        assertTrue(text.contains("Sensor #3"));
        assertTrue(text.contains("rainfall GREATER_THAN 12.5"));
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }

    private void updateSensor(House house, int sensorId, WeatherCondition weather) throws Exception {
        house.interactWithDevice(sensorId, device ->
            ((RainfallSensor) device).updateFromState(state(weather))
        );
    }

    private SimulationState state(WeatherCondition weather) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationStateStub(now, now.minusHours(1), 20.0, 500.0, weather);
    }
}
