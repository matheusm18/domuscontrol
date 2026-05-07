package domuscontrol.devices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceStatusTest {

    @Test
    void hasExpectedStatusesInOrder() {
        assertArrayEquals(
            new DeviceStatus[] {
                DeviceStatus.ON,
                DeviceStatus.OFF,
                DeviceStatus.OPEN,
                DeviceStatus.CLOSED
            },
            DeviceStatus.values()
        );
    }

    @Test
    void valueOfReturnsMatchingStatus() {
        assertEquals(DeviceStatus.ON, DeviceStatus.valueOf("ON"));
        assertEquals(DeviceStatus.OFF, DeviceStatus.valueOf("OFF"));
        assertEquals(DeviceStatus.OPEN, DeviceStatus.valueOf("OPEN"));
        assertEquals(DeviceStatus.CLOSED, DeviceStatus.valueOf("CLOSED"));
    }
}
