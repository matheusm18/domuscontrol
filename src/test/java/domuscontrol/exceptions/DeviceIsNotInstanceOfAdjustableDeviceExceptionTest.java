package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceIsNotInstanceOfAdjustableDeviceExceptionTest {

    @Test
    void constructorStoresMessage() {
        DeviceIsNotInstanceOfAdjustableDeviceException exception =
            new DeviceIsNotInstanceOfAdjustableDeviceException("Device is not adjustable");

        assertEquals("Device is not adjustable", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
