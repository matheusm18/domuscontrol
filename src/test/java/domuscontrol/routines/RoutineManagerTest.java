package domuscontrol.routines;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Plug;
import domuscontrol.exceptions.AutomationDoesntExistException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScenarioDoesntExistException;
import domuscontrol.exceptions.UserDoesntHaveScenarios;
import domuscontrol.houses.House;
import domuscontrol.routines.actions.TurnOnAction;
import domuscontrol.routines.conditions.DeviceStateCondition;
import domuscontrol.routines.conditions.TimeCondition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RoutineManagerTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorStartsEmpty() {
        RoutineManager manager = new RoutineManager();

        assertTrue(manager.getScenariosByUser().isEmpty());
        assertTrue(manager.getAutomationsByName().isEmpty());
        assertEquals("RoutineManager { Users With Scenarios: 0, Routines: 0 }", manager.toString());
    }

    @Test
    void constructorCopiesMapsAndNormalizesKeys() throws Exception {
        Scenario scenario = new Scenario("Morning", List.of(new TurnOnAction(1)));
        Automation automation = automation("Auto", 1);
        Map<Integer, Map<String, Scenario>> scenarios = new HashMap<>();
        scenarios.put(7, new HashMap<>(Map.of("Morning", scenario)));
        Map<String, Automation> automations = new HashMap<>(Map.of("Auto", automation));

        RoutineManager manager = new RoutineManager(scenarios, automations);
        scenario.setName("Changed");
        automation.setName("Changed");

        assertTrue(manager.getScenariosByUser().get(7).containsKey("morning"));
        assertTrue(manager.getAutomationsByName().containsKey("auto"));
        assertEquals("Morning", manager.getScenarioByName(7, "MORNING").getName());
        assertEquals("Auto", manager.getAutomationByName("AUTO").getName());
    }

    @Test
    void settersReplaceDataWithCopies() throws Exception {
        RoutineManager manager = new RoutineManager();
        Scenario scenario = new Scenario("Morning", List.of(new TurnOnAction(1)));
        Automation automation = automation("Auto", 1);

        manager.setScenariosByUser(Map.of(7, Map.of("Morning", scenario)));
        manager.setAutomations(Map.of("Auto", automation));
        scenario.setName("Changed");
        automation.setName("Changed");

        assertEquals("Morning", manager.getScenarioByName(7, "morning").getName());
        assertEquals("Auto", manager.getAutomationByName("auto").getName());

        manager.setScenariosByUser(null);
        manager.setAutomations(null);

        assertTrue(manager.getScenariosByUser().isEmpty());
        assertTrue(manager.getAutomationsByName().isEmpty());
    }

    @Test
    void addScenarioStoresCloneAndRejectsDuplicateNameForSameUser() throws Exception {
        RoutineManager manager = new RoutineManager();
        Scenario scenario = new Scenario("Morning", List.of(new TurnOnAction(1)));

        manager.addScenario(7, scenario);
        scenario.setName("Changed");

        assertEquals("Morning", manager.getScenarioByName(7, "MORNING").getName());
        assertThrows(NameAlreadyExistsException.class, () ->
            manager.addScenario(7, new Scenario("morning", List.of()))
        );
    }

    @Test
    void getScenarioMethodsThrowExpectedExceptions() {
        RoutineManager manager = new RoutineManager();

        assertThrows(UserDoesntHaveScenarios.class, () -> manager.getScenariosForUser(7));
        assertThrows(UserDoesntHaveScenarios.class, () -> manager.getScenarioByName(7, "Missing"));
    }

    @Test
    void getScenarioByNameThrowsWhenScenarioIsMissingForExistingUser() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addScenario(7, new Scenario("Morning", List.of()));

        ScenarioDoesntExistException exception =
            assertThrows(ScenarioDoesntExistException.class, () -> manager.getScenarioByName(7, "Night"));

        assertEquals("Night", exception.getMessage());
    }

    @Test
    void addAutomationStoresCloneAndRejectsDuplicateName() throws Exception {
        RoutineManager manager = new RoutineManager();
        Automation automation = automation("Auto", 1);

        manager.addAutomation(automation);
        automation.setName("Changed");

        assertEquals("Auto", manager.getAutomationByName("AUTO").getName());
        assertThrows(NameAlreadyExistsException.class, () -> manager.addAutomation(automation("auto", 2)));
    }

    @Test
    void getAutomationByNameThrowsWhenMissing() {
        RoutineManager manager = new RoutineManager();

        AutomationDoesntExistException exception =
            assertThrows(AutomationDoesntExistException.class, () -> manager.getAutomationByName("Auto"));

        assertEquals("Auto", exception.getMessage());
    }

    @Test
    void filtersSchedulesAndAutomations() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addAutomation(automation("Auto", 1));
        manager.addAutomation(schedule("Noon"));

        assertEquals(List.of("Auto"), manager.getOnlyAutomations().stream().map(Automation::getName).toList());
        assertEquals(List.of("Noon"), manager.getOnlySchedules().stream().map(Automation::getName).toList());
        assertEquals(2, manager.getAutomations().size());
    }

    @Test
    void removeAutomationDeletesByCaseInsensitiveName() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addAutomation(automation("Auto", 1));

        manager.removeAutomation("AUTO");

        assertTrue(manager.getAutomations().isEmpty());
        assertThrows(AutomationDoesntExistException.class, () -> manager.removeAutomation("AUTO"));
    }

    @Test
    void removeScenarioDeletesByCaseInsensitiveName() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addScenario(7, new Scenario("Morning", List.of()));

        manager.removeScenario(7, "MORNING");

        assertTrue(manager.getScenariosForUser(7).isEmpty());
        assertThrows(ScenarioDoesntExistException.class, () -> manager.removeScenario(7, "MORNING"));
        assertThrows(UserDoesntHaveScenarios.class, () -> manager.removeScenario(8, "MORNING"));
    }

    @Test
    void executeScenarioByNameRunsScenarioActions() throws Exception {
        RoutineManager manager = new RoutineManager();
        House house = houseWithPlug();
        Plug plug = (Plug) house.getDevices().values().iterator().next();
        manager.addScenario(7, new Scenario("Morning", List.of(new TurnOnAction(plug.getId()))));

        manager.executeScenarioByName(7, "MORNING", house);

        assertEquals(DeviceStatus.ON, house.getDevice(plug.getId()).getStatus());
    }

    @Test
    void executeScenarioByNameThrowsExpectedExceptions() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addScenario(7, new Scenario("Morning", List.of()));

        assertThrows(UserDoesntHaveScenarios.class, () -> manager.executeScenarioByName(8, "Morning", new House()));
        assertThrows(ScenarioDoesntExistException.class, () -> manager.executeScenarioByName(7, "Night", new House()));
    }

    @Test
    void tickReturnsTriggeredAutomationNames() throws Exception {
        RoutineManager manager = new RoutineManager();
        House house = houseWithPlug();
        Plug plug = (Plug) house.getDevices().values().iterator().next();
        manager.addAutomation(new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(plug.getId(), false)),
            List.of(new TurnOnAction(plug.getId()))
        ));

        List<String> activated = manager.tick(house, state());

        assertEquals(List.of("Auto"), activated);
        assertEquals(DeviceStatus.ON, house.getDevice(plug.getId()).getStatus());
        assertTrue(manager.tick(house, state()).isEmpty());
    }

    @Test
    void removeDeviceCleansMatchingRoutines() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addAutomation(new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(1, true)),
            List.of(new TurnOnAction(1))
        ));
        manager.addScenario(7, new Scenario("Morning", List.of(new TurnOnAction(1))));

        manager.removeDevice(1);

        assertTrue(manager.getAutomations().isEmpty());
        assertTrue(manager.getScenariosForUser(7).isEmpty());
    }

    @Test
    void cloneEqualsAndHashCodeUseStoredData() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addAutomation(automation("Auto", 1));
        manager.addScenario(7, new Scenario("Morning", List.of(new TurnOnAction(1))));

        RoutineManager copy = manager.clone();

        assertNotSame(manager, copy);
        assertEquals(manager, copy);
        assertEquals(manager.hashCode(), copy.hashCode());
    }

    @Test
    void returnedMapsAndListsAreCopies() throws Exception {
        RoutineManager manager = new RoutineManager();
        manager.addAutomation(automation("Auto", 1));
        manager.addScenario(7, new Scenario("Morning", List.of(new TurnOnAction(1))));

        manager.getAutomationsByName().clear();
        manager.getScenariosByUser().get(7).clear();
        manager.getAutomations().get(0).setName("Changed");
        manager.getScenariosForUser(7).get(0).setName("Changed");

        assertEquals("Auto", manager.getAutomationByName("auto").getName());
        assertEquals("Morning", manager.getScenarioByName(7, "morning").getName());
    }

    private Automation automation(String name, int deviceId) throws Exception {
        return new Automation(
            name,
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(deviceId, true)),
            List.of(new TurnOnAction(deviceId))
        );
    }

    private Automation schedule(String name) throws Exception {
        return new Automation(
            name,
            AutomationType.SCHEDULE,
            List.of(new TimeCondition(LocalTime.NOON)),
            List.of()
        );
    }

    private House houseWithPlug() throws Exception {
        House house = new House();
        house.addDivision("Kitchen");
        house.addDeviceToDivision(new Plug("TP-Link", "P100", 3.0), "Kitchen");
        return house;
    }

    private SimulationState state() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationState(now, now.minusMinutes(1), 20.0, 500.0, WeatherCondition.SUNNY);
    }
}
