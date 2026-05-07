package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceIsNotInstanceOfOpenableDeviceExceptionTest {

    @Test
    void constructorStoresMessage() {
        DeviceIsNotInstanceOfOpenableDeviceException exception =
            new DeviceIsNotInstanceOfOpenableDeviceException("Device is not openable");

        assertEquals("Device is not openable", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
