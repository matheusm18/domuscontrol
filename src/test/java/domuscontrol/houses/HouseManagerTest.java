package domuscontrol.houses;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Plug;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HouseManagerTest {

    @BeforeEach
    void setUp() {
        House.setNextId(0);
        Device.setNextId(0);
    }

    @Test
    void createHouseRegistersHouseWithName() throws Exception {
        HouseManager manager = new HouseManager();

        House house = manager.createHouse("Main Home");

        assertEquals("Main Home", house.getName());
        assertTrue(manager.existsHouseWithId(house.getId()));
        assertEquals(1, manager.getAllHouses().size());
    }

    @Test
    void getHouseByIdReturnsClone() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");

        House copy = manager.getHouseById(house.getId());
        copy.setName("Changed");

        assertNotSame(house, copy);
        assertEquals("Main Home", manager.getHouseById(house.getId()).getName());
    }

    @Test
    void getMissingHouseThrowsException() {
        HouseManager manager = new HouseManager();

        HouseNotFoundException exception =
            assertThrows(HouseNotFoundException.class, () -> manager.getHouseById(99));

        assertEquals("99", exception.getMessage());
    }

    @Test
    void addDivisionAndRemoveDivisionAreAppliedToStoredHouse() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");

        manager.addDivision(house.getId(), "Kitchen");
        assertEquals(1, manager.getHouseById(house.getId()).divisionsNumber());

        manager.removeDivision(house.getId(), "Kitchen");
        assertEquals(0, manager.getHouseById(house.getId()).divisionsNumber());
    }

    @Test
    void addDivisionToMissingHouseThrowsException() {
        HouseManager manager = new HouseManager();

        assertThrows(HouseNotFoundException.class, () -> manager.addDivision(99, "Kitchen"));
    }

    @Test
    void addDeviceToDivisionAndGetDeviceWorkThroughManager() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");
        Plug plug = new Plug("TP-Link", "P100", 3.0);

        manager.addDivision(house.getId(), "Kitchen");
        manager.addDeviceToDivision(house.getId(), plug, "Kitchen");

        Device stored = manager.getDevice(house.getId(), plug.getId());

        assertEquals(plug, stored);
        assertEquals(1, manager.getAllDevices().size());
    }

    @Test
    void addDeviceToMissingDivisionThrowsException() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");
        Plug plug = new Plug();

        DivisionNotFoundException exception = assertThrows(
            DivisionNotFoundException.class,
            () -> manager.addDeviceToDivision(house.getId(), plug, "Garage")
        );

        assertEquals("Garage", exception.getMessage());
    }

    @Test
    void getDeviceFromMissingHouseThrowsHouseNotFound() {
        HouseManager manager = new HouseManager();

        assertThrows(HouseNotFoundException.class, () -> manager.getDevice(99, 1));
    }

    @Test
    void getMissingDeviceThrowsDeviceNotFound() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");

        DeviceNotFoundException exception =
            assertThrows(DeviceNotFoundException.class, () -> manager.getDevice(house.getId(), 99));

        assertEquals("99", exception.getMessage());
    }

    @Test
    void interactWithDeviceMutatesStoredDevice() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");
        Plug plug = new Plug("TP-Link", "P100", 60.0);
        manager.addDivision(house.getId(), "Kitchen");
        manager.addDeviceToDivision(house.getId(), plug, "Kitchen");

        manager.interactWithDevice(house.getId(), plug.getId(), device -> {
            Plug storedPlug = (Plug) device;
            storedPlug.turnOn();
            storedPlug.tick(30);
        });

        Plug stored = (Plug) manager.getDevice(house.getId(), plug.getId());
        assertEquals(DeviceStatus.ON, stored.getStatus());
        assertEquals(30, stored.getTotalMinutesOn());
        assertEquals(30.0, stored.getEnergyConsumption());
    }

    @Test
    void removeDeviceDeletesItFromStoredHouse() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        manager.addDivision(house.getId(), "Kitchen");
        manager.addDeviceToDivision(house.getId(), plug, "Kitchen");

        manager.removeDevice(house.getId(), plug.getId());

        assertTrue(manager.getAllDevices().isEmpty());
        assertThrows(DeviceNotFoundException.class, () -> manager.getDevice(house.getId(), plug.getId()));
    }

    @Test
    void getAllHousesReturnsClones() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");

        List<House> houses = manager.getAllHouses();
        houses.get(0).setName("Changed");

        assertEquals("Main Home", manager.getHouseById(house.getId()).getName());
    }

    @Test
    void getAllDevicesReturnsClones() throws Exception {
        HouseManager manager = new HouseManager();
        House house = manager.createHouse("Main Home");
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        manager.addDivision(house.getId(), "Kitchen");
        manager.addDeviceToDivision(house.getId(), plug, "Kitchen");

        Plug copy = (Plug) manager.getAllDevices().get(0);
        copy.turnOn();

        assertEquals(DeviceStatus.OFF, manager.getDevice(house.getId(), plug.getId()).getStatus());
    }

    @Test
    void getMostConsumingHouseReturnsCloneOfHighestConsumer() throws Exception {
        HouseManager manager = new HouseManager();
        House low = manager.createHouse("Low");
        House high = manager.createHouse("High");
        Plug lowPlug = new Plug("A", "1", 60.0);
        Plug highPlug = new Plug("B", "2", 120.0);
        manager.addDivision(low.getId(), "Room");
        manager.addDivision(high.getId(), "Room");
        manager.addDeviceToDivision(low.getId(), lowPlug, "Room");
        manager.addDeviceToDivision(high.getId(), highPlug, "Room");

        manager.interactWithDevice(low.getId(), lowPlug.getId(), device -> {
            ((Plug) device).turnOn();
            device.tick(30);
        });
        manager.interactWithDevice(high.getId(), highPlug.getId(), device -> {
            ((Plug) device).turnOn();
            device.tick(30);
        });

        House mostConsuming = manager.getMostConsumingHouse();

        assertEquals("High", mostConsuming.getName());
        mostConsuming.setName("Changed");
        assertEquals("High", manager.getHouseById(high.getId()).getName());
    }

    @Test
    void getMostConsumingHouseReturnsNullWhenThereAreNoHouses() {
        HouseManager manager = new HouseManager();

        assertNull(manager.getMostConsumingHouse());
    }
}
