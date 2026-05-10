package domuscontrol.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherConditionTest {

    @Test
    void hasExpectedConditionsInOrder() {
        assertArrayEquals(
            new WeatherCondition[] {
                WeatherCondition.SUNNY,
                WeatherCondition.PARTLY_CLOUDY,
                WeatherCondition.CLOUDY,
                WeatherCondition.FOGGY,
                WeatherCondition.RAINING,
                WeatherCondition.STORMY,
                WeatherCondition.SNOWING
            },
            WeatherCondition.values()
        );
    }

    @Test
    void exposesLuminosityMultiplierAndDisplayName() {
        assertEquals(1.0, WeatherCondition.SUNNY.getLuminosityMultiplier());
        assertEquals(0.15, WeatherCondition.STORMY.getLuminosityMultiplier());
        assertEquals("SNOWING", WeatherCondition.SNOWING.toString());
    }
}
