package domuscontrol.routines;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutomationTypeTest {

    @Test
    void hasExpectedTypesInOrder() {
        assertArrayEquals(
            new AutomationType[] {
                AutomationType.AUTOMATION,
                AutomationType.SCHEDULE
            },
            AutomationType.values()
        );
    }

    @Test
    void valueOfReturnsMatchingType() {
        assertEquals(AutomationType.AUTOMATION, AutomationType.valueOf("AUTOMATION"));
        assertEquals(AutomationType.SCHEDULE, AutomationType.valueOf("SCHEDULE"));
    }
}
