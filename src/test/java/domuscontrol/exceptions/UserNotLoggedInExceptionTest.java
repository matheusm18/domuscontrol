package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserNotLoggedInExceptionTest {

    @Test
    void constructorStoresMessage() {
        UserNotLoggedInException exception = new UserNotLoggedInException("User not logged in");

        assertEquals("User not logged in", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
