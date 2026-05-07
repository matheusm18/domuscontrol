package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginInvalidPasswordExceptionTest {

    @Test
    void constructorStoresMessage() {
        LoginInvalidPasswordException exception = new LoginInvalidPasswordException("Invalid password");

        assertEquals("Invalid password", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
