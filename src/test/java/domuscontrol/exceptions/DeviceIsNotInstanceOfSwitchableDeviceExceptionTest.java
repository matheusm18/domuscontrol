package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceIsNotInstanceOfSwitchableDeviceExceptionTest {

    @Test
    void constructorStoresMessage() {
        DeviceIsNotInstanceOfSwitchableDeviceException exception =
            new DeviceIsNotInstanceOfSwitchableDeviceException("Device is not switchable");

        assertEquals("Device is not switchable", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
