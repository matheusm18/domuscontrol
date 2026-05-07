package domuscontrol.houses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DivisionInfoTest {

    @BeforeEach
    void setUp() {
        House.setNextId(0);
    }

    @Test
    void constructorStoresDivisionNameAndCopiesDevicesList() {
        House house = new House();
        house.setName("Home");
        List<String> devices = new ArrayList<>(List.of("Lamp", "Plug"));

        DivisionInfo info = new DivisionInfo(house, "Living Room", devices);
        devices.add("Relay");

        assertEquals("Living Room", info.getDivisionName());
        assertEquals(List.of("Lamp", "Plug"), info.getDevices());
    }

    @Test
    void getDevicesReturnsCopy() {
        DivisionInfo info = new DivisionInfo(null, "Kitchen", List.of("Lamp"));

        List<String> devices = info.getDevices();
        devices.add("Plug");

        assertEquals(List.of("Lamp"), info.getDevices());
    }

    @Test
    void constructorAndGetterCloneHouse() {
        House house = new House();
        house.setName("Original");

        DivisionInfo info = new DivisionInfo(house, "Office", List.of());
        house.setName("Changed");

        House firstCopy = info.getHouse();
        firstCopy.setName("Mutated copy");

        assertEquals("Original", info.getHouse().getName());
        assertNotSame(house, info.getHouse());
    }

    @Test
    void nullHouseAndDevicesAreHandled() {
        DivisionInfo info = new DivisionInfo(null, "Empty", null);

        assertNull(info.getHouse());
        assertEquals("Empty", info.getDivisionName());
        assertTrue(info.getDevices().isEmpty());
    }
}
