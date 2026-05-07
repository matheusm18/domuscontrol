package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleWithConditionDifferentFromTimeExceptionTest {

    @Test
    void constructorStoresMessage() {
        ScheduleWithConditionDifferentFromTimeException exception =
            new ScheduleWithConditionDifferentFromTimeException("Schedule condition must be time based");

        assertEquals("Schedule condition must be time based", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
