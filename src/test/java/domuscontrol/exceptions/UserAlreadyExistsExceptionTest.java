package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserAlreadyExistsExceptionTest {

    @Test
    void constructorStoresMessage() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists");

        assertEquals("User already exists", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
