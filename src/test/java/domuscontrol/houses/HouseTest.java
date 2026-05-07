package domuscontrol.houses;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HouseTest {

    @BeforeEach
    void setUp() {
        House.setNextId(0);
        Device.setNextId(0);
    }

    @Test
    void defaultConstructorCreatesEmptyHouse() {
        House house = new House();

        assertEquals(1, house.getId());
        assertEquals("", house.getName());
        assertEquals(0, house.devicesNumber());
        assertEquals(0, house.divisionsNumber());
        assertTrue(house.getDevices().isEmpty());
        assertTrue(house.getDivisions().isEmpty());
    }

    @Test
    void setNameUpdatesHouseName() {
        House house = new House();

        house.setName("Main Home");

        assertEquals("Main Home", house.getName());
    }

    @Test
    void addDivisionCreatesEmptyDivision() {
        House house = new House();

        house.addDivision("Kitchen");

        assertEquals(1, house.divisionsNumber());
        assertTrue(house.getDivisions().containsKey("Kitchen"));
        assertTrue(house.getDivisions().get("Kitchen").isEmpty());
    }

    @Test
    void deleteDivisionRemovesDivisionAndItsDevices() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        house.deleteDivision("Kitchen");

        assertEquals(0, house.divisionsNumber());
        assertEquals(0, house.devicesNumber());
        assertThrows(DeviceNotFoundException.class, () -> house.getDevice(plug.getId()));
    }

    @Test
    void deleteMissingDivisionThrowsException() {
        House house = new House();

        DivisionNotFoundException exception =
            assertThrows(DivisionNotFoundException.class, () -> house.deleteDivision("Garage"));

        assertEquals("Garage", exception.getMessage());
    }

    @Test
    void addDeviceToDivisionStoresDeviceAndAvoidsDuplicates() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");

        house.addDeviceToDivision(plug, "Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        assertEquals(1, house.devicesNumber());
        assertEquals(1, house.getDivisions().get("Kitchen").size());
        assertEquals(plug, house.getDevice(plug.getId()));
    }

    @Test
    void addDeviceToMissingDivisionThrowsException() {
        House house = new House();
        Plug plug = new Plug();

        DivisionNotFoundException exception =
            assertThrows(DivisionNotFoundException.class, () -> house.addDeviceToDivision(plug, "Garage"));

        assertEquals("Garage", exception.getMessage());
    }

    @Test
    void getDeviceReturnsClone() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        Plug copy = (Plug) house.getDevice(plug.getId());
        copy.turnOn();

        assertNotSame(plug, copy);
        assertEquals(DeviceStatus.OFF, house.getDevice(plug.getId()).getStatus());
    }

    @Test
    void interactWithDeviceMutatesStoredDevice() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 60.0);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        house.interactWithDevice(plug.getId(), device -> {
            Plug storedPlug = (Plug) device;
            storedPlug.turnOn();
            storedPlug.tick(30);
        });

        Plug stored = (Plug) house.getDevice(plug.getId());
        assertTrue(stored.isOn());
        assertEquals(30, stored.getTotalMinutesOn());
        assertEquals(30.0, stored.getEnergyConsumption());
    }

    @Test
    void readDeviceReturnsComputedValueFromStoredDevice() throws Exception {
        House house = new House();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 40, 3000);
        house.addDivision("Living Room");
        house.addDeviceToDivision(lamp, "Living Room");

        int brightness = house.readDevice(lamp.getId(), device -> ((Lamp) device).getBrightness());

        assertEquals(40, brightness);
    }

    @Test
    void removeDeviceDeletesItFromAllDivisions() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");
        house.addDivision("Office");
        house.addDeviceToDivision(plug, "Kitchen");
        house.addDeviceToDivision(plug, "Office");

        house.removeDevice(plug.getId());

        assertEquals(0, house.devicesNumber());
        assertTrue(house.getDivisions().get("Kitchen").isEmpty());
        assertTrue(house.getDivisions().get("Office").isEmpty());
    }

    @Test
    void removeDeviceFromDivisionKeepsDeviceRegisteredGlobally() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        house.removeDeviceFromDivision(plug.getId(), "Kitchen");

        assertTrue(house.getDivisions().get("Kitchen").isEmpty());
        assertEquals(1, house.devicesNumber());
        assertEquals(plug, house.getDevice(plug.getId()));
    }

    @Test
    void getDevicesAndDivisionsReturnCopies() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");

        Map<Integer, Device> devices = house.getDevices();
        devices.clear();
        Map<String, List<Device>> divisions = house.getDivisions();
        divisions.get("Kitchen").clear();

        assertEquals(1, house.devicesNumber());
        assertEquals(1, house.getDivisions().get("Kitchen").size());
    }

    @Test
    void calculateTotalConsumptionSumsRegisteredDevices() throws Exception {
        House house = new House();
        Plug plug = new Plug("TP-Link", "P100", 60.0);
        Lamp lamp = new Lamp("Philips", "Hue", 120.0, 50, 3000);
        house.addDivision("Kitchen");
        house.addDeviceToDivision(plug, "Kitchen");
        house.addDeviceToDivision(lamp, "Kitchen");

        house.interactWithDevice(plug.getId(), device -> {
            ((Plug) device).turnOn();
            device.tick(30);
        });
        house.interactWithDevice(lamp.getId(), device -> device.tick(30));

        assertEquals(90.0, house.calculateTotalConsumption());
    }

    @Test
    void top3DevicesAndDivisionsUseProvidedCriteria() throws Exception {
        House house = new House();
        Plug first = new Plug("A", "1", 1.0);
        Plug second = new Plug("B", "2", 1.0);
        Plug third = new Plug("C", "3", 1.0);
        Plug fourth = new Plug("D", "4", 1.0);
        house.addDivision("One");
        house.addDivision("Two");
        house.addDeviceToDivision(first, "One");
        house.addDeviceToDivision(second, "One");
        house.addDeviceToDivision(third, "Two");
        house.addDeviceToDivision(fourth, "Two");

        house.interactWithDevice(second.getId(), device -> ((Plug) device).turnOn());
        house.interactWithDevice(third.getId(), device -> {
            Plug plug = (Plug) device;
            plug.turnOn();
            plug.turnOff();
            plug.turnOn();
        });

        List<Device> topDevices = house.top3Devices(Device::getTotalActivations);
        List<String> topDivisions = house.top3Divisions(List::size);

        assertEquals(3, topDevices.size());
        assertEquals(third.getId(), topDevices.get(0).getId());
        assertEquals(second.getId(), topDevices.get(1).getId());
        assertEquals(2, topDivisions.size());
        assertTrue(topDivisions.containsAll(List.of("One", "Two")));
    }

    @Test
    void cloneKeepsFieldsButIsIndependent() throws Exception {
        House house = new House();
        house.setName("Main Home");
        house.addDivision("Kitchen");
        house.addDeviceToDivision(new Plug("TP-Link", "P100", 3.0), "Kitchen");

        House copy = house.clone();
        copy.setName("Copy");
        copy.addDivision("Office");

        assertNotSame(house, copy);
        assertEquals("Main Home", house.getName());
        assertEquals(1, house.divisionsNumber());
        assertEquals(2, copy.divisionsNumber());
    }

    @Test
    void toStringIncludesHouseAndDivisionInformation() {
        House house = new House();
        house.setName("Main Home");
        house.addDivision("Kitchen");

        String text = house.toString();

        assertTrue(text.contains("=== House: Main Home [ID: 1] ==="));
        assertTrue(text.contains("Division: Kitchen"));
    }
}
