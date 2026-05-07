package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleDoesntExistExceptionTest {

    @Test
    void constructorStoresMessage() {
        ScheduleDoesntExistException exception = new ScheduleDoesntExistException("Schedule not found");

        assertEquals("Schedule not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
