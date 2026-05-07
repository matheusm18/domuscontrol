package domuscontrol.suggestions;

import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceInteractionTest {

    @Test
    void defaultConstructorUsesEmptyValues() {
        DeviceInteraction interaction = new DeviceInteraction();

        assertEquals(-1, interaction.getDeviceId());
        assertEquals(-1, interaction.getUserId());
        assertNull(interaction.getType());
        assertNull(interaction.getValue());
        assertNull(interaction.getTimestamp());
        assertNull(interaction.getWeather());
        assertNull(interaction.getOutsideTemperature());
        assertNull(interaction.getLuminosity());
    }

    @Test
    void constructorsStoreFieldsWithAndWithoutValue() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        DeviceInteraction turnOn = new DeviceInteraction(1, InteractionType.TURN_ON, 7, time);
        DeviceInteraction level = new DeviceInteraction(2, InteractionType.SET_LEVEL, 50.0, 7, time,
            WeatherCondition.SUNNY, 25.0, 800.0);

        assertEquals(1, turnOn.getDeviceId());
        assertEquals(InteractionType.TURN_ON, turnOn.getType());
        assertNull(turnOn.getValue());
        assertEquals(2, level.getDeviceId());
        assertEquals(50.0, level.getValue());
        assertEquals(WeatherCondition.SUNNY, level.getWeather());
        assertEquals(25.0, level.getOutsideTemperature());
        assertEquals(800.0, level.getLuminosity());
    }

    @Test
    void settersUpdateFields() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        DeviceInteraction interaction = new DeviceInteraction();

        interaction.setDeviceId(1);
        interaction.setUserId(7);
        interaction.setType(InteractionType.SET_OPENING);
        interaction.setValue(80.0);
        interaction.setTimestamp(time);
        interaction.setWeather(WeatherCondition.RAINING);
        interaction.setOutsideTemperature(12.0);
        interaction.setLuminosity(200.0);

        assertEquals(1, interaction.getDeviceId());
        assertEquals(7, interaction.getUserId());
        assertEquals(InteractionType.SET_OPENING, interaction.getType());
        assertEquals(80.0, interaction.getValue());
        assertEquals(time, interaction.getTimestamp());
        assertEquals(WeatherCondition.RAINING, interaction.getWeather());
        assertEquals(12.0, interaction.getOutsideTemperature());
        assertEquals(200.0, interaction.getLuminosity());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseAllFields() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 12, 0);
        DeviceInteraction interaction = new DeviceInteraction(2, InteractionType.SET_LEVEL, 50.0, 7, time,
            WeatherCondition.SUNNY, 25.0, 800.0);

        DeviceInteraction copy = interaction.clone();

        assertNotSame(interaction, copy);
        assertEquals(interaction, copy);
        assertEquals(interaction.hashCode(), copy.hashCode());
        assertTrue(interaction.toString().contains("Device ID: 2"));
        assertTrue(interaction.toString().contains("Value: 50.0"));
    }
}
