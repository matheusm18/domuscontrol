package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutomationDoesntExistExceptionTest {

    @Test
    void constructorStoresMessage() {
        AutomationDoesntExistException exception = new AutomationDoesntExistException("Automation not found");

        assertEquals("Automation not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
