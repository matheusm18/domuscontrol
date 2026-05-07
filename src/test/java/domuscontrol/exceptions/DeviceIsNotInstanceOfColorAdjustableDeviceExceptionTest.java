package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceIsNotInstanceOfColorAdjustableDeviceExceptionTest {

    @Test
    void constructorStoresMessage() {
        DeviceIsNotInstanceOfColorAdjustableDeviceException exception =
            new DeviceIsNotInstanceOfColorAdjustableDeviceException("Device is not color adjustable");

        assertEquals("Device is not color adjustable", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
