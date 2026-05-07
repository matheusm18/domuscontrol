package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceNotFoundExceptionTest {

    @Test
    void constructorStoresMessage() {
        DeviceNotFoundException exception = new DeviceNotFoundException("Device not found");

        assertEquals("Device not found", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
