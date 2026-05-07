package domuscontrol.suggestions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionTypeTest {

    @Test
    void hasExpectedTypesInOrder() {
        assertArrayEquals(
            new InteractionType[] {
                InteractionType.TURN_ON,
                InteractionType.TURN_OFF,
                InteractionType.SET_LEVEL,
                InteractionType.SET_OPENING,
                InteractionType.SET_COLOR_TEMPERATURE
            },
            InteractionType.values()
        );
    }
}
