package domuscontrol.suggestions;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InteractionLoggerTest {

    @Test
    void constructorCopiesInteractions() {
        DeviceInteraction interaction = interaction(1);
        List<DeviceInteraction> interactions = new ArrayList<>(List.of(interaction));

        InteractionLogger logger = new InteractionLogger(interactions);
        interaction.setDeviceId(99);
        interactions.clear();

        assertEquals(1, logger.size());
        assertEquals(1, logger.getInteractions().get(0).getDeviceId());
    }

    @Test
    void getInteractionsReturnsUnmodifiableCopies() {
        InteractionLogger logger = new InteractionLogger();
        logger.log(interaction(1));

        List<DeviceInteraction> interactions = logger.getInteractions();
        interactions.get(0).setDeviceId(99);

        assertThrows(UnsupportedOperationException.class, () -> interactions.add(interaction(2)));
        assertEquals(1, logger.getInteractions().get(0).getDeviceId());
    }

    @Test
    void logIgnoresNullAndClearRemovesInteractions() {
        InteractionLogger logger = new InteractionLogger();

        logger.log(null);
        logger.log(interaction(1));
        assertEquals(1, logger.size());

        logger.clear();
        assertEquals(0, logger.size());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseInteractions() {
        InteractionLogger logger = new InteractionLogger(List.of(interaction(1)));

        InteractionLogger copy = logger.clone();

        assertNotSame(logger, copy);
        assertEquals(logger, copy);
        assertEquals(logger.hashCode(), copy.hashCode());
        assertEquals("InteractionLogger { Total Interactions: 1 }", logger.toString());
    }

    private DeviceInteraction interaction(int deviceId) {
        return new DeviceInteraction(deviceId, InteractionType.TURN_ON, 7, LocalDateTime.of(2026, 1, 1, 12, 0));
    }
}
