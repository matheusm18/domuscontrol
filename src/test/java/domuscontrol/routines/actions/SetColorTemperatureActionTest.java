package domuscontrol.routines.actions;

import domuscontrol.devices.Device;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.houses.House;
import domuscontrol.routines.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SetColorTemperatureActionTest {

    @BeforeEach
    void setUp() {
        Device.setNextId(0);
        House.setNextId(0);
    }

    @Test
    void defaultConstructorStartsWithoutTargetDeviceAndDefaultTemperature() {
        SetColorTemperatureAction action = new SetColorTemperatureAction();

        assertEquals(-1, action.getDeviceId());
        assertEquals(2700, action.getTargetTemperature());
        assertFalse(action.hasDeviceId(1));
    }

    @Test
    void constructorAndSettersDefineTargetDeviceAndTemperature() {
        SetColorTemperatureAction action = new SetColorTemperatureAction(3, 3200);

        assertEquals(3, action.getDeviceId());
        assertEquals(3200, action.getTargetTemperature());

        action.setDeviceId(5);
        action.setTargetTemperature(3800);

        assertEquals(5, action.getDeviceId());
        assertEquals(3800, action.getTargetTemperature());
        assertTrue(action.hasDeviceId(5));
    }

    @Test
    void executeSetsColorTemperatureOnColorAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 10, 3000);
        house.addDeviceToDivision(lamp, "Kitchen");

        new SetColorTemperatureAction(lamp.getId(), 3500).execute(house);

        assertEquals(3500, ((Lamp) house.getDevice(lamp.getId())).getColorTemperature());
    }

    @Test
    void executeUsesDeviceClampingRules() throws Exception {
        House house = houseWithDivision();
        Lamp lamp = new Lamp("Philips", "Hue", 9.0, 10, 3000);
        house.addDeviceToDivision(lamp, "Kitchen");

        new SetColorTemperatureAction(lamp.getId(), 5000).execute(house);

        assertEquals(4000, ((Lamp) house.getDevice(lamp.getId())).getColorTemperature());
    }

    @Test
    void executeDoesNothingForNonColorAdjustableDevice() throws Exception {
        House house = houseWithDivision();
        Plug plug = new Plug("TP-Link", "P100", 3.0);
        house.addDeviceToDivision(plug, "Kitchen");

        new SetColorTemperatureAction(plug.getId(), 3500).execute(house);

        assertEquals(plug, house.getDevice(plug.getId()));
    }

    @Test
    void executeIgnoresMissingDevice() {
        House house = houseWithDivision();

        assertDoesNotThrow(() -> new SetColorTemperatureAction(99, 3500).execute(house));
    }

    @Test
    void copyAndCloneKeepDeviceIdAndTargetTemperature() {
        SetColorTemperatureAction action = new SetColorTemperatureAction(3, 3200);

        Action copy = action.copy();
        SetColorTemperatureAction clone = action.clone();

        assertNotSame(action, copy);
        assertEquals(action, copy);
        assertEquals(action.hashCode(), copy.hashCode());
        assertEquals(action, clone);
    }

    @Test
    void equalsRequiresSameClassDeviceIdAndTargetTemperature() {
        assertEquals(new SetColorTemperatureAction(3, 3200), new SetColorTemperatureAction(3, 3200));
        assertNotEquals(new SetColorTemperatureAction(3, 3200), new SetColorTemperatureAction(4, 3200));
        assertNotEquals(new SetColorTemperatureAction(3, 3200), new SetColorTemperatureAction(3, 3500));
        assertNotEquals(new SetColorTemperatureAction(3, 3200), new TurnOnAction(3));
    }

    @Test
    void toStringIncludesDeviceIdAndTargetTemperature() {
        assertEquals(
            "SetColorTemperatureAction { Device ID: 3, Target Temperature: 3200K }",
            new SetColorTemperatureAction(3, 3200).toString()
        );
    }

    private House houseWithDivision() {
        House house = new House();
        assertDoesNotThrow(() -> house.addDivision("Kitchen"));
        return house;
    }
}
