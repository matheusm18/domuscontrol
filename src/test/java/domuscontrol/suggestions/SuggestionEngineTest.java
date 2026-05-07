package domuscontrol.suggestions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.devices.sensors.LuminositySensor;
import domuscontrol.devices.sensors.RainfallSensor;
import domuscontrol.devices.sensors.TemperatureSensor;
import domuscontrol.routines.AutomationType;
import domuscontrol.routines.actions.SetLevelAction;
import domuscontrol.routines.actions.TurnOnAction;
import domuscontrol.routines.conditions.DeviceStateCondition;
import domuscontrol.routines.conditions.LuminositySensorCondition;
import domuscontrol.routines.conditions.RainfallSensorCondition;
import domuscontrol.routines.conditions.TemperatureSensorCondition;
import domuscontrol.routines.conditions.TimeCondition;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SuggestionEngineTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
    }

    @Test
    void suggestReturnsEmptyWhenThereAreNoPatternsForUser() throws Exception {
        InteractionLogger logger = new InteractionLogger(List.of(
            interaction(1, InteractionType.TURN_ON, 7, time(12, 0))
        ));

        assertTrue(SuggestionEngine.suggest(logger, Map.of(1, new Plug()), 8).isEmpty());
    }

    @Test
    void detectsSchedulePatternForRepeatedActionAroundSameTime() throws Exception {
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        InteractionLogger logger = new InteractionLogger(List.of(
            interaction(plug.getId(), InteractionType.TURN_ON, 7, time(8, 0)),
            interaction(plug.getId(), InteractionType.TURN_ON, 7, time(8, 5)),
            interaction(plug.getId(), InteractionType.TURN_ON, 7, time(8, 10))
        ));

        List<AutomationSuggestion> suggestions = SuggestionEngine.suggest(logger, Map.of(plug.getId(), plug), 7);

        assertEquals(1, suggestions.size());
        assertEquals(AutomationType.SCHEDULE, suggestions.get(0).getAutomation().getType());
        assertInstanceOf(TimeCondition.class, suggestions.get(0).getAutomation().getConditions().get(0));
        assertInstanceOf(TurnOnAction.class, suggestions.get(0).getAutomation().getActions().get(0));
    }

    @Test
    void detectsSequencePatternAcrossDevices() throws Exception {
        Plug trigger = new Plug("A", "Trigger", 1.0);
        Plug target = new Plug("B", "Target", 1.0);
        InteractionLogger logger = new InteractionLogger(List.of(
            interaction(trigger.getId(), InteractionType.TURN_ON, 7, time(8, 0)),
            interaction(target.getId(), InteractionType.TURN_ON, 7, time(8, 1)),
            interaction(trigger.getId(), InteractionType.TURN_ON, 7, time(9, 0)),
            interaction(target.getId(), InteractionType.TURN_ON, 7, time(9, 1)),
            interaction(trigger.getId(), InteractionType.TURN_ON, 7, time(10, 0)),
            interaction(target.getId(), InteractionType.TURN_ON, 7, time(10, 1))
        ));

        List<AutomationSuggestion> suggestions = SuggestionEngine.suggest(
            logger,
            Map.of(trigger.getId(), trigger, target.getId(), target),
            7
        );

        assertTrue(suggestions.stream().anyMatch(s ->
            s.getAutomation().getType() == AutomationType.AUTOMATION
                && s.getAutomation().getConditions().get(0) instanceof DeviceStateCondition
                && s.getAutomation().getActions().get(0) instanceof TurnOnAction
        ));
    }

    @Test
    void detectsEnvironmentalPatternsWhenSensorsExist() throws Exception {
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 50, 3000);
        TemperatureSensor temperatureSensor = new TemperatureSensor();
        LuminositySensor luminositySensor = new LuminositySensor();
        RainfallSensor rainfallSensor = new RainfallSensor();
        InteractionLogger logger = new InteractionLogger(List.of(
            valueInteraction(lamp.getId(), InteractionType.SET_LEVEL, 80.0, 7, time(8, 0), WeatherCondition.RAINING, 26.0, 800.0),
            valueInteraction(lamp.getId(), InteractionType.SET_LEVEL, 80.0, 7, time(9, 0), WeatherCondition.RAINING, 27.0, 850.0),
            valueInteraction(lamp.getId(), InteractionType.SET_LEVEL, 80.0, 7, time(10, 0), WeatherCondition.RAINING, 28.0, 900.0)
        ));

        List<AutomationSuggestion> suggestions = SuggestionEngine.suggest(
            logger,
            Map.of(
                lamp.getId(), lamp,
                temperatureSensor.getId(), temperatureSensor,
                luminositySensor.getId(), luminositySensor,
                rainfallSensor.getId(), rainfallSensor
            ),
            7
        );

        assertTrue(suggestions.stream().anyMatch(s -> s.getAutomation().getConditions().get(0) instanceof TemperatureSensorCondition));
        assertTrue(suggestions.stream().anyMatch(s -> s.getAutomation().getConditions().get(0) instanceof LuminositySensorCondition));
        assertTrue(suggestions.stream().anyMatch(s -> s.getAutomation().getConditions().get(0) instanceof RainfallSensorCondition));
        assertTrue(suggestions.stream().anyMatch(s -> s.getAutomation().getActions().get(0) instanceof SetLevelAction));
    }

    private DeviceInteraction interaction(int deviceId, InteractionType type, int userId, LocalDateTime timestamp) {
        return new DeviceInteraction(deviceId, type, userId, timestamp);
    }

    private DeviceInteraction valueInteraction(int deviceId, InteractionType type, Double value, int userId,
                                               LocalDateTime timestamp, WeatherCondition weather,
                                               Double temperature, Double luminosity) {
        return new DeviceInteraction(deviceId, type, value, userId, timestamp, weather, temperature, luminosity);
    }

    private LocalDateTime time(int hour, int minute) {
        return LocalDateTime.of(2026, 1, 1, hour, minute);
    }
}
