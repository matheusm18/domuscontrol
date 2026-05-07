package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HouseAlreadyExistsExceptionTest {

    @Test
    void constructorStoresMessage() {
        HouseAlreadyExistsException exception = new HouseAlreadyExistsException("House already exists");

        assertEquals("House already exists", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
