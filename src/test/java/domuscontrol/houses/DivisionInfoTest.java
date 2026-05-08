package domuscontrol.houses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DivisionInfoTest {

    @Test
    void constructorStoresAllFields() {
        DivisionInfo info = new DivisionInfo("Home", "Living Room", 3);

        assertEquals("Home", info.getHouseName());
        assertEquals("Living Room", info.getDivisionName());
        assertEquals(3, info.getDeviceCount());
    }

    @Test
    void gettersReturnCorrectValues() {
        DivisionInfo info = new DivisionInfo("Casa da Praia", "Kitchen", 0);

        assertEquals("Casa da Praia", info.getHouseName());
        assertEquals("Kitchen", info.getDivisionName());
        assertEquals(0, info.getDeviceCount());
    }

    @Test
    void deviceCountCanBeZero() {
        DivisionInfo info = new DivisionInfo("House", "Empty Room", 0);
        assertEquals(0, info.getDeviceCount());
    }

    @Test
    void divisionNameIsStoredExactly() {
        DivisionInfo info = new DivisionInfo("H", "Sala de Estar", 5);
        assertEquals("Sala de Estar", info.getDivisionName());
    }
}
