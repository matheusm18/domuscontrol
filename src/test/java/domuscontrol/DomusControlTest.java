package domuscontrol;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.exceptions.DeviceIsNotInstanceOfAdjustableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfSwitchableDeviceException;
import domuscontrol.exceptions.LastAdminException;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.houses.House;
import domuscontrol.routines.Scenario;
import domuscontrol.routines.actions.TurnOnAction;
import domuscontrol.user.User;
import domuscontrol.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DomusControlTest {

    @BeforeEach
    void setUp() {
        User.setNextId(0);
        House.setNextId(0);
        Device.setNextId(0);
    }

    @Test
    void registersUsersAndValidatesLogin() throws Exception {
        DomusControl model = new DomusControl();

        model.registerUser("Ada", "ada@example.com", "secret");

        assertTrue(model.existsUserWithEmail("ADA@example.com"));
        assertEquals("Ada", model.validateLogin("ada@example.com", "secret").getName());
        assertThrows(LoginInvalidPasswordException.class, () -> model.validateLogin("ada@example.com", "wrong"));
    }

    @Test
    void updatesUserFields() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Ada", "ada@example.com", "secret");

        model.updateUserName("ada@example.com", "Ada Lovelace");
        model.updateUserPassword("ada@example.com", "new");
        model.updateUserEmail("ada@example.com", "lovelace@example.com");

        assertFalse(model.existsUserWithEmail("ada@example.com"));
        assertEquals("Ada Lovelace", model.validateLogin("lovelace@example.com", "new").getName());
    }

    @Test
    void createHouseAssignsOwnerAsAdministrator() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Ada", "ada@example.com", "secret");

        House house = model.createHouse("ada@example.com", "Home");

        assertEquals("Home", house.getName());
        assertEquals(UserRole.ADMINISTRATOR, model.getUserRoleInHouse("ada@example.com", house.getId()));
        assertEquals(List.of(house), model.getHousesByUser("ada@example.com"));
        assertEquals(1, model.getUsersInHouse(house.getId()).size());
    }

    @Test
    void assignAndDeleteUserFromHouseRespectLastAdminRule() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Admin", "admin@example.com", "secret");
        model.registerUser("User", "user@example.com", "secret");
        House house = model.createHouse("admin@example.com", "Home");
        User user = model.getUserByEmail("user@example.com");

        model.assignUserToHouse(house.getId(), user.getId(), UserRole.USER);

        assertEquals(UserRole.USER, model.getUserRoleInHouse("user@example.com", house.getId()));
        assertThrows(LastAdminException.class, () -> model.deleteUserFromHouse(house.getId(), model.getUserByEmail("admin@example.com").getId()));

        model.deleteUserFromHouse(house.getId(), user.getId());
        assertFalse(model.getUserByEmail("user@example.com").hasRoleInHouse(house.getId()));
    }

    @Test
    void divisionsDevicesAndDeviceOperationsWorkThroughFacade() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Ada", "ada@example.com", "secret");
        House house = model.createHouse("ada@example.com", "Home");
        User user = model.getUserByEmail("ada@example.com");
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 0, 2700);
        domuscontrol.devices.Gate gate = new domuscontrol.devices.Gate("Nice", "Road", 80.0, 0);

        model.addDivision(house.getId(), "Kitchen");
        model.addDeviceToDivision(house.getId(), plug, "Kitchen");
        model.addDeviceToDivision(house.getId(), lamp, "Kitchen");
        model.addDeviceToDivision(house.getId(), gate, "Kitchen");

        model.toggleDevice(house.getId(), plug.getId(), user.getId());
        model.setDeviceLevel(house.getId(), lamp.getId(), 70, user.getId());
        model.setDeviceColorTemperature(house.getId(), lamp.getId(), 3500, user.getId());

        assertEquals(DeviceStatus.ON, model.getDevice(house.getId(), plug.getId()).getStatus());
        assertEquals(70, ((Lamp) model.getDevice(house.getId(), lamp.getId())).getBrightness());
        assertEquals(3500, ((Lamp) model.getDevice(house.getId(), lamp.getId())).getColorTemperature());
        assertThrows(DeviceIsNotInstanceOfAdjustableDeviceException.class,
            () -> model.setDeviceLevel(house.getId(), plug.getId(), 50, user.getId()));
        assertThrows(DeviceIsNotInstanceOfSwitchableDeviceException.class,
            () -> model.toggleDevice(house.getId(), gate.getId(), user.getId()));
    }

    @Test
    void scenariosCanBeAddedAndExecuted() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Ada", "ada@example.com", "secret");
        House house = model.createHouse("ada@example.com", "Home");
        User user = model.getUserByEmail("ada@example.com");
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        model.addDivision(house.getId(), "Kitchen");
        model.addDeviceToDivision(house.getId(), plug, "Kitchen");

        model.addScenario(house.getId(), user.getId(), new Scenario("Morning", List.of(new TurnOnAction(plug.getId()))));
        model.executeScenario(house.getId(), user.getId(), "Morning");

        assertEquals(DeviceStatus.ON, model.getDevice(house.getId(), plug.getId()).getStatus());
        assertEquals(1, model.getScenarios(house.getId(), user.getId()).size());
        model.removeScenario(house.getId(), user.getId(), "Morning");
    }

    @Test
    void simulationHelpersAndTickWork() {
        DomusControl model = new DomusControl();
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 14, 0);

        model.setCurrentTime(now);

        assertEquals(now, model.getCurrentDateTime());
        assertEquals(now.toLocalTime(), model.getCurrentTime());
        assertEquals(20.0, model.getTemperature());
        assertEquals(1000.0, model.getLuminosity());
        assertNotNull(model.getCurrentState());
        assertThrows(IllegalArgumentException.class, () -> model.tick(-1));
        assertDoesNotThrow(() -> model.tick(1));
    }

    @Test
    void saveAndLoadStateRoundTrip() throws Exception {
        DomusControl model = new DomusControl();
        model.registerUser("Ada", "ada@example.com", "secret");
        Path file = Files.createTempFile("domus-control", ".bin");

        model.saveState(file.toString());
        DomusControl loaded = DomusControl.loadState(file.toString());

        assertTrue(loaded.existsUserWithEmail("ada@example.com"));
        Files.deleteIfExists(file);
    }
}
