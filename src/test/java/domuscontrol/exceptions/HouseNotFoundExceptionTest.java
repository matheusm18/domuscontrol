package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HouseNotFoundExceptionTest {

    @Test
    void constructorStoresMessage() {
        HouseNotFoundException exception = new HouseNotFoundException("House not found");

        assertEquals("House not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
