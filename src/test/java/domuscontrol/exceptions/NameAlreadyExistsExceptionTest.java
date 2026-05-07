package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NameAlreadyExistsExceptionTest {

    @Test
    void constructorStoresMessage() {
        NameAlreadyExistsException exception = new NameAlreadyExistsException("Name already exists");

        assertEquals("Name already exists", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
