package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LastAdminExceptionTest {

    @Test
    void constructorStoresMessage() {
        LastAdminException exception = new LastAdminException("Cannot remove last admin");

        assertEquals("Cannot remove last admin", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
