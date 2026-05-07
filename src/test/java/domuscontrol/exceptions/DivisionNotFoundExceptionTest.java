package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DivisionNotFoundExceptionTest {

    @Test
    void constructorStoresMessage() {
        DivisionNotFoundException exception = new DivisionNotFoundException("Division not found");

        assertEquals("Division not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
