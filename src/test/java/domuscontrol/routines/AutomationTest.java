package domuscontrol.routines;

import domuscontrol.devices.Device;
import domuscontrol.devices.DeviceStatus;
import domuscontrol.devices.Plug;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.houses.House;
import domuscontrol.routines.actions.TurnOnAction;
import domuscontrol.routines.conditions.DeviceStateCondition;
import domuscontrol.routines.conditions.TimeCondition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AutomationTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorCreatesAutomationWithoutConditions() {
        Automation automation = new Automation();

        assertEquals("Unnamed Routine", automation.getName());
        assertEquals(AutomationType.AUTOMATION, automation.getType());
        assertTrue(automation.getConditions().isEmpty());
        assertFalse(automation.isWasConditionMetPreviously());
    }

    @Test
    void constructorStoresTypeConditionsAndActionsAsCopies() throws Exception {
        DeviceStateCondition condition = new DeviceStateCondition(1, false);
        TurnOnAction action = new TurnOnAction(2);
        Automation automation = new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(condition),
            List.of(action)
        );

        condition.setDeviceId(99);
        action.setDeviceId(99);

        assertEquals("Auto", automation.getName());
        assertEquals(AutomationType.AUTOMATION, automation.getType());
        assertEquals(new DeviceStateCondition(1, false), automation.getConditions().get(0));
        assertEquals(new TurnOnAction(2), automation.getActions().get(0));
    }

    @Test
    void nullTypeFallsBackToAutomation() throws Exception {
        Automation automation = new Automation("Auto", null, null, null);

        assertEquals(AutomationType.AUTOMATION, automation.getType());
    }

    @Test
    void scheduleRejectsNonTimeBasedConditions() {
        assertThrows(
            ScheduleWithConditionDifferentFromTimeException.class,
            () -> new Automation(
                "Schedule",
                AutomationType.SCHEDULE,
                List.of(new DeviceStateCondition(1, true)),
                List.of()
            )
        );
    }

    @Test
    void scheduleAcceptsTimeBasedConditions() throws Exception {
        Automation automation = new Automation(
            "Schedule",
            AutomationType.SCHEDULE,
            List.of(new TimeCondition(LocalTime.NOON)),
            List.of()
        );

        assertEquals(AutomationType.SCHEDULE, automation.getType());
    }

    @Test
    void setTypeRejectsScheduleWhenExistingConditionIsNotTimeBased() throws Exception {
        Automation automation = new Automation();
        automation.addCondition(new DeviceStateCondition(1, true));

        assertThrows(
            ScheduleWithConditionDifferentFromTimeException.class,
            () -> automation.setType(AutomationType.SCHEDULE)
        );
        assertEquals(AutomationType.AUTOMATION, automation.getType());
    }

    @Test
    void addConditionRejectsNonTimeConditionForSchedule() throws Exception {
        Automation automation = new Automation("Schedule", AutomationType.SCHEDULE, null, null);

        assertThrows(
            ScheduleWithConditionDifferentFromTimeException.class,
            () -> automation.addCondition(new DeviceStateCondition(1, true))
        );
    }

    @Test
    void addAndRemoveConditionUpdateConditionList() throws Exception {
        Automation automation = new Automation();
        DeviceStateCondition condition = new DeviceStateCondition(1, true);

        automation.addCondition(null);
        automation.addCondition(condition);

        assertEquals(1, automation.getConditions().size());
        assertTrue(automation.removeCondition(condition));
        assertFalse(automation.removeCondition(null));
        assertTrue(automation.getConditions().isEmpty());
    }

    @Test
    void checkAndTriggerExecutesActionsOnlyWhenConditionsBecomeTrue() throws Exception {
        House house = houseWithDivision();
        Plug triggerPlug = new Plug("A", "Trigger", 1.0);
        Plug targetPlug = new Plug("B", "Target", 1.0);
        house.addDeviceToDivision(triggerPlug, "Kitchen");
        house.addDeviceToDivision(targetPlug, "Kitchen");
        Automation automation = new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(triggerPlug.getId(), false)),
            List.of(new TurnOnAction(targetPlug.getId()))
        );

        assertTrue(automation.checkAndTrigger(house, state()));
        assertEquals(DeviceStatus.ON, house.getDevice(targetPlug.getId()).getStatus());

        house.interactWithDevice(targetPlug.getId(), device -> ((Plug) device).turnOff());

        assertFalse(automation.checkAndTrigger(house, state()));
        assertEquals(DeviceStatus.OFF, house.getDevice(targetPlug.getId()).getStatus());
    }

    @Test
    void checkAndTriggerResetsAfterConditionsBecomeFalse() throws Exception {
        House house = houseWithDivision();
        Plug triggerPlug = new Plug("A", "Trigger", 1.0);
        Plug targetPlug = new Plug("B", "Target", 1.0);
        house.addDeviceToDivision(triggerPlug, "Kitchen");
        house.addDeviceToDivision(targetPlug, "Kitchen");
        Automation automation = new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(triggerPlug.getId(), false)),
            List.of(new TurnOnAction(targetPlug.getId()))
        );

        assertTrue(automation.checkAndTrigger(house, state()));
        house.interactWithDevice(triggerPlug.getId(), device -> ((Plug) device).turnOn());
        assertFalse(automation.checkAndTrigger(house, state()));
        house.interactWithDevice(triggerPlug.getId(), device -> ((Plug) device).turnOff());
        house.interactWithDevice(targetPlug.getId(), device -> ((Plug) device).turnOff());

        assertTrue(automation.checkAndTrigger(house, state()));
    }

    @Test
    void checkAndTriggerReturnsFalseWithoutConditions() {
        assertFalse(new Automation().checkAndTrigger(new House(), state()));
    }

    @Test
    void removeDeviceByIdRemovesMatchingActionsAndConditions() throws Exception {
        Automation automation = new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(1, true), new DeviceStateCondition(2, true)),
            List.of(new TurnOnAction(1), new TurnOnAction(2))
        );

        automation.removeDeviceById(1);

        assertEquals(List.of(new TurnOnAction(2)), automation.getActions());
        assertEquals(List.of(new DeviceStateCondition(2, true)), automation.getConditions());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseAutomationFields() throws Exception {
        Automation automation = new Automation(
            "Auto",
            AutomationType.AUTOMATION,
            List.of(new DeviceStateCondition(1, true)),
            List.of(new TurnOnAction(1))
        );

        Automation copy = automation.clone();

        assertNotSame(automation, copy);
        assertEquals(automation, copy);
        assertEquals(automation.hashCode(), copy.hashCode());
        assertEquals(
            "Automation { Name: 'Auto', Type: AUTOMATION, Conditions: 1, Actions: 1 }",
            automation.toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }

    private SimulationState state() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new SimulationStateStub(now, now.minusMinutes(1), 20.0, 500.0, WeatherCondition.SUNNY);
    }
}
