package domuscontrol.routines;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.actions.TurnOnAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorCreatesUnnamedScenario() {
        Scenario scenario = new Scenario();

        assertEquals("Unnamed Routine", scenario.getName());
        assertTrue(scenario.getActions().isEmpty());
    }

    @Test
    void constructorStoresNameAndCopiesActions() {
        TurnOnAction action = new TurnOnAction(1);
        Scenario scenario = new Scenario("Movie", List.of(action));

        action.setDeviceId(2);

        assertEquals("Movie", scenario.getName());
        assertEquals(new TurnOnAction(1), scenario.getActions().get(0));
    }

    @Test
    void executeScenarioRunsActions() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");
        Scenario scenario = new Scenario("Turn on plug", List.of(new TurnOnAction(plug.getId())));

        scenario.executeScenario(house);

        assertEquals(DeviceStatus.ON, house.getDevice(plug.getId()).getStatus());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseRoutineFields() {
        Scenario scenario = new Scenario("Movie", List.of(new TurnOnAction(1)));

        Scenario copy = scenario.clone();

        assertNotSame(scenario, copy);
        assertEquals(scenario, copy);
        assertEquals(scenario.hashCode(), copy.hashCode());
        assertEquals("Scenario { Name: 'Movie', Actions: 1 }", scenario.toString());
    }

    @Test
    void equalsRequiresSameClass() {
        Scenario scenario = new Scenario("Movie", List.of(new TurnOnAction(1)));

        assertNotEquals(scenario, new Automation());
    }

    private House houseWithDivision() {
        House house = new House();
        house.addDivision("Kitchen");
        return house;
    }
}
