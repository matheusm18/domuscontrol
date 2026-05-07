package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioDoesntExistExceptionTest {

    @Test
    void constructorStoresMessage() {
        ScenarioDoesntExistException exception = new ScenarioDoesntExistException("Scenario not found");

        assertEquals("Scenario not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
