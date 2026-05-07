package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserNotFoundExceptionTest {

    @Test
    void constructorStoresMessage() {
        UserNotFoundException exception = new UserNotFoundException("User not found");

        assertEquals("User not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
