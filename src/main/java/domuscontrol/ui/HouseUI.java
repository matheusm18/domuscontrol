package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.AirConditioner;
import domuscontrol.devices.Curtain;
import domuscontrol.devices.Device;
import domuscontrol.devices.Fan;
import domuscontrol.devices.Gate;
import domuscontrol.devices.Heater;
import domuscontrol.devices.Lamp;
import domuscontrol.devices.Plug;
import domuscontrol.devices.Relay;
import domuscontrol.devices.Speaker;
import domuscontrol.devices.Television;
import domuscontrol.devices.sensors.LuminositySensor;
import domuscontrol.devices.sensors.RainfallSensor;
import domuscontrol.devices.sensors.TemperatureSensor;
import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.DeviceIsNotInstanceOfAdjustableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfColorAdjustableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfOpenableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfSwitchableDeviceException;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.LastAdminException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.routines.Automation;
import domuscontrol.routines.AutomationType;
import domuscontrol.suggestions.AutomationSuggestion;
import domuscontrol.user.UserRole;
import domuscontrol.utils.Ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles all operations scoped to a specific house: divisions, devices,
 * device control, and future automations/schedules/scenarios.
 * Received from {@link UserUI} after the user selects a house.
 */
/**
 * User interface class for house-related operations and interactions.
 * Handles house management, device control, and division operations for users.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class HouseUI {

    private DomusControl model;
    private final Scanner sc;
    private final ActionsUI actionsUI;

    /**
     * Initialises the model and shared Scanner (created once in DomusControlUI).
     * 
     * @param model shared model instance.
     * @param sc    shared Scanner (created once in DomusControlUI).
     */
    public HouseUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
        this.actionsUI = new ActionsUI(model, sc);
    }

    /**
     * Sets the model for this UI controller.
     *
     * @param model The DomusControl model instance.
     */
    public void setModel(DomusControl model) {
        this.model = model;
        this.actionsUI.setModel(model);
    }

    /**
     * Displays the house management interface for a specific house.
     *
     * @param email The email of the user.
     * @param houseId The ID of the house to display.
     * @param houseName The name of the house to display.
     */
    public void show(String email, int houseId, String houseName) {
        int userId;
        try {
            userId = model.getUserByEmail(email).getId();
        } catch (UserNotFoundException e) {
            System.out.println("  Error: User not found.");
            return;
        }

        Menu menu = new Menu(houseName, new String[]{
                "View House Details",
                "Manage Divisions",
                "Manage Devices",
                "Manage Users",
                "Operate a Device",
                "Suggestions",
                "Automations",
                "Schedules",
                "Scenarios"
        }, model::getCurrentState);

        menu.setPreCondition(2, () -> isAdmin(email, houseId));
        menu.setPreCondition(3, () -> isAdmin(email, houseId) && houseHasDivisions(houseId));
        menu.setPreCondition(4, () -> isAdmin(email, houseId));
        menu.setPreCondition(5, () -> houseHasDevices(houseId));
        menu.setPreCondition(6, () -> houseHasDevices(houseId));
        menu.setPreCondition(7, () -> houseHasDevices(houseId));
        menu.setPreCondition(8, () -> houseHasDevices(houseId));
        menu.setPreCondition(9, () -> houseHasDevices(houseId));

        menu.setHandler(1, () -> viewHouseDetails(houseId));
        menu.setHandler(2, () -> manageDivisions(houseId));
        menu.setHandler(3, () -> manageDevices(houseId));
        menu.setHandler(4, () -> manageUsers(houseId, email));
        menu.setHandler(5, () -> operateDevice(houseId, userId));
        menu.setHandler(6, () -> showSuggestions(houseId, userId));
        menu.setHandler(7, () -> actionsUI.manageAutomations(houseId, email));
        menu.setHandler(8, () -> actionsUI.manageSchedules(houseId, email));
        menu.setHandler(9, () -> actionsUI.manageScenarios(houseId, email));

        menu.run();
    }

    private boolean isAdmin(String email, int houseId) {
        try {
            return model.getUserRoleInHouse(email, houseId) == UserRole.ADMINISTRATOR;
        } catch (UserNotFoundException | HouseNotFoundException e) {
            return false;
        }
    }

    // ---- View ----

    private void viewHouseDetails(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Ansi.listTitle("House Details");
            Ansi.listRow(String.format("%-12s %s", "Name", house.getName()));
            Ansi.listRow(String.format("%-12s %d", "ID", house.getId()));
            Ansi.listRow(String.format("%-12s %d", "Divisions", house.getDivisions().size()));
            Ansi.listRow(String.format("%-12s %d", "Devices", house.getDevices().size()));
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    // ---- Divisions ----

    private void manageDivisions(int houseId) {
        Menu menu = new Menu("Divisions", new String[]{"List Divisions", "Add Division", "Remove Division"}, model::getCurrentState);
        menu.setPreCondition(1, () -> houseHasDivisions(houseId));
        menu.setPreCondition(3, () -> houseHasDivisions(houseId));
        menu.setHandler(1, () -> listDivisions(houseId));
        menu.setHandler(2, () -> addDivision(houseId));
        menu.setHandler(3, () -> removeDivision(houseId));
        menu.run();
    }

    private void listDivisions(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("  No divisions."); return; }
            Ansi.listTitle("Divisions");
            for (Map.Entry<String, List<Device>> entry : divisions.entrySet()) {
                Ansi.listRow(entry.getKey());
                for (Device d : entry.getValue())
                    Ansi.listRow(String.format("    [#%-3d] %-18s %s %s",
                        d.getId(), d.getClass().getSimpleName(), d.getBrand(), d.getModel()));
            }
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void addDivision(int houseId) {
        System.out.print(Ansi.prompt("Division name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("\n  Name cannot be empty."); return; }
        try {
            model.addDivision(houseId, name);
            System.out.println("  Division '" + name + "' added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void removeDivision(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("  No divisions."); return; }

            List<String> names = new ArrayList<>(divisions.keySet());
            Ansi.listTitle("Select Division");
            for (int i = 0; i < names.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, names.get(i)));
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Division (0 to cancel)"));
            int choice = readInt();
            if (choice < 1 || choice > names.size()) return;

            model.removeDivision(houseId, names.get(choice - 1));
            System.out.println("  Division removed.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    // ---- Devices ----

    private void manageDevices(int houseId) {
        Menu menu = new Menu("Devices", 
                            new String[]{
                                "List Devices", 
                                "Add Device", 
                                "Remove Device"
                            }, model::getCurrentState);

        menu.setPreCondition(1, () -> houseHasDevices(houseId));
        menu.setPreCondition(2, () -> houseHasDivisions(houseId));
        menu.setPreCondition(3, () -> houseHasDevices(houseId));

        menu.setHandler(1, () -> listDevices(houseId));
        menu.setHandler(2, () -> addDevice(houseId));
        menu.setHandler(3, () -> removeDevice(houseId));
        menu.run();
    }

    private void listDevices(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<Integer, Device> devices = house.getDevices();
            if (devices.isEmpty()) { System.out.println("  No devices."); return; }
            Ansi.listTitle("Select Device");
            devices.values().forEach(device ->
                Ansi.listRow(String.format("%d  %-15s %-12s %s",
                    device.getId(),
                    device.getClass().getSimpleName(),
                    device.getBrand(),
                    device.getModel()))
            );
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Device (0 to cancel)"));
            
            String input = sc.nextLine().trim();
            if (input.equals("0")) return;
            
            try {
                int selectedId = Integer.parseInt(input);
                if (devices.containsKey(selectedId)) {
                    showDeviceInfo(devices.get(selectedId));
                } else {
                    System.out.println("  Invalid Device ID.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void showDeviceInfo(Device device) {
        Ansi.listTitle(device.getClass().getSimpleName());
        Ansi.listRow(String.format("%-13s %d", "ID", device.getId()));
        Ansi.listRow(String.format("%-13s %s", "Brand", device.getBrand()));
        Ansi.listRow(String.format("%-13s %s", "Model", device.getModel()));
        Ansi.listRow(String.format("%-13s %.1f Wh/h", "Consumption", device.getConsumptionPerHour()));
        Ansi.listRow(String.format("%-13s %s", "Status", device.getStatus()));
        Ansi.listRow(String.format("%-13s %d", "Minutes on", device.getTotalMinutesOn()));
        Ansi.listRow(String.format("%-13s %d", "Activations", device.getTotalActivations()));
        Ansi.listRow(String.format("%-13s %.1f Wh", "Energy used", device.getEnergyConsumption()));
        if (device instanceof AdjustableDevice adjustableDevice) {
            Ansi.listRow(String.format("%-13s %d%%", "Level", adjustableDevice.getLevel()));
        }
        if (device instanceof ColorAdjustableDevice colorAdjustableDevice) {
            Ansi.listRow(String.format("%-13s %dK", "Color Temp", colorAdjustableDevice.getColorTemperature()));
        }
        if (device instanceof Speaker speaker) {
            Ansi.listRow(String.format("%-13s %s", "Source", speaker.getSource()));
        }
        if (device instanceof Television television) {
            Ansi.listRow(String.format("%-13s %s", "Source", television.getSource()));
        }
        if (device instanceof OpenableDevice openableDevice) {
            Ansi.listRow(String.format("%-13s %d%%", "Opening", openableDevice.getOpeningLevel()));
        }
        if (device instanceof TemperatureSensor ts) {
            Ansi.listRow(String.format("%-13s %.1f ºC", "Temperature", ts.getTemperature()));
        }
        if (device instanceof LuminositySensor ls) {
            Ansi.listRow(String.format("%-13s %.1f lx", "Luminosity", ls.getLuminosity()));
        }
        if (device instanceof RainfallSensor rs) {
            Ansi.listRow(String.format("%-13s %.1f mm/h", "Rainfall", rs.getRainfall()));
        }
        Ansi.listSeparator();
    }

    private void addDevice(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("  Add a division first."); return; }

            List<String> divNames = new ArrayList<>(divisions.keySet());
            Ansi.listTitle("Select Division");
            for (int i = 0; i < divNames.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, divNames.get(i)));
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Division (0 to cancel)"));
            int divChoice = readInt();
            if (divChoice < 1 || divChoice > divNames.size()) return;
            String division = divNames.get(divChoice - 1);

            Menu typeMenu = new Menu("Device Type", new String[]{
                    "Lamp", "Speaker", "Curtain", "Gate", "Plug", "Relay",
                    "Heater", "Fan", "Air Conditioner", "Television",
                    "Temperature Sensor", "Luminosity Sensor", "Rainfall Sensor"
            }, model::getCurrentState);
            typeMenu.setHandler(1, () -> addLamp(houseId, division));
            typeMenu.setHandler(2, () -> addSpeaker(houseId, division));
            typeMenu.setHandler(3, () -> addCurtain(houseId, division));
            typeMenu.setHandler(4, () -> addGate(houseId, division));
            typeMenu.setHandler(5, () -> addPlug(houseId, division));
            typeMenu.setHandler(6, () -> addRelay(houseId, division));
            typeMenu.setHandler(7, () -> addHeater(houseId, division));
            typeMenu.setHandler(8, () -> addFan(houseId, division));
            typeMenu.setHandler(9, () -> addAirConditioner(houseId, division));
            typeMenu.setHandler(10, () -> addTelevision(houseId, division));
            typeMenu.setHandler(11, () -> addTemperatureSensor(houseId, division));
            typeMenu.setHandler(12, () -> addLuminositySensor(houseId, division));
            typeMenu.setHandler(13, () -> addRainfallSensor(houseId, division));
            typeMenu.run();

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private record DeviceBase(String brand, String modelName, double consumption) {}

    private DeviceBase readBaseFields() {
        System.out.print(Ansi.prompt("Brand"));
        String brand = sc.nextLine();
        System.out.print(Ansi.prompt("Model name"));
        String modelName = sc.nextLine();
        System.out.print(Ansi.prompt("Consumption per hour (Wh/h)"));
        double consumption = readDouble();
        if (consumption < 0) {
            throw new IllegalArgumentException("Consumption cannot be negative.");
        }
        return new DeviceBase(brand, modelName, consumption);
    }

    private void addLamp(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Brightness (0-100)"));
        int brightness = readInt();
        if (brightness < 0 || brightness > 100) {
            System.out.println("  Error: Brightness must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Color temperature (K, e.g. 2700-4000)"));
        int colorTemp = readInt();
        if (colorTemp < 2700 || colorTemp > 4000) {
            System.out.println("  Error: Color temperature must be between 2700 and 4000 K.");
            return;
        }
        try {
            Lamp lamp = new Lamp(base.brand(), base.modelName(), base.consumption(), brightness, colorTemp);
            model.addDeviceToDivision(houseId, lamp, division);
            System.out.println("  Lamp added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addSpeaker(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Volume (0-100)"));
        int volume = readInt();
        if (volume < 0 || volume > 100) {
            System.out.println("  Error: Volume must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Source"));
        String source = sc.nextLine();
        try {
            Speaker speaker = new Speaker(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, speaker, division);
            System.out.println("  Speaker added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addCurtain(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        if (opening < 0 || opening > 100) {
            System.out.println("  Error: Opening level must be between 0 and 100.");
            return;
        }
        try {
            Curtain curtain = new Curtain(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, curtain, division);
            System.out.println("  Curtain added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addGate(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        if (opening < 0 || opening > 100) {
            System.out.println("  Error: Opening level must be between 0 and 100.");
            return;
        }
        try {
            Gate gate = new Gate(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, gate, division);
            System.out.println("  Gate added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addPlug(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        try {
            Plug plug = new Plug(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, plug, division);
            System.out.println("  Plug added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addRelay(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        try {
            Relay relay = new Relay(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, relay, division);
            System.out.println("  Relay added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addHeater(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Heating power (0-100)"));
        int power = readInt();
        if (power < 0 || power > 100) {
            System.out.println("  Error: Heating power must be between 0 and 100.");
            return;
        }
        try {
            Heater heater = new Heater(base.brand(), base.modelName(), base.consumption(), power);
            model.addDeviceToDivision(houseId, heater, division);
            System.out.println("  Heater added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addFan(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Speed (0-100)"));
        int speed = readInt();
        if (speed < 0 || speed > 100) {
            System.out.println("  Error: Speed must be between 0 and 100.");
            return;
        }
        try {
            Fan fan = new Fan(base.brand(), base.modelName(), base.consumption(), speed);
            model.addDeviceToDivision(houseId, fan, division);
            System.out.println("  Fan added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addAirConditioner(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Cooling power (0-100)"));
        int coolingPower = readInt();
        if (coolingPower < 0 || coolingPower > 100) {
            System.out.println("  Error: Cooling power must be between 0 and 100.");
            return;
        }
        try {
            AirConditioner airConditioner = new AirConditioner(base.brand(), base.modelName(), base.consumption(), coolingPower);
            model.addDeviceToDivision(houseId, airConditioner, division);
            System.out.println("  Air conditioner added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addTelevision(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            System.out.println("  Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Volume (0-100)"));
        int volume = readInt();
        if (volume < 0 || volume > 100) {
            System.out.println("  Error: Volume must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Source"));
        String source = sc.nextLine();
        try {
            Television television = new Television(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, television, division);
            System.out.println("  Television added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DivisionNotFoundException e) {
            System.out.println("  Error: division not found.");
        }
    }

    private void addTemperatureSensor(int houseId, String division) {
        DeviceBase base;
        try { base = readBaseFields(); }
        catch (IllegalArgumentException e) { System.out.println("  Error: consumption cannot be negative."); return; }
        try {
            model.addDeviceToDivision(houseId, new TemperatureSensor(base.brand(), base.modelName(), base.consumption()), division);
            System.out.println("  Temperature sensor added.");
        } catch (HouseNotFoundException e) { System.out.println("  Error: house not found."); }
          catch (DivisionNotFoundException e) { System.out.println("  Error: division not found."); }
    }

    private void addLuminositySensor(int houseId, String division) {
        DeviceBase base;
        try { base = readBaseFields(); }
        catch (IllegalArgumentException e) { System.out.println("  Error: consumption cannot be negative."); return; }
        try {
            model.addDeviceToDivision(houseId, new LuminositySensor(base.brand(), base.modelName(), base.consumption()), division);
            System.out.println("  Luminosity sensor added.");
        } catch (HouseNotFoundException e) { System.out.println("  Error: house not found."); }
          catch (DivisionNotFoundException e) { System.out.println("  Error: division not found."); }
    }

    private void addRainfallSensor(int houseId, String division) {
        DeviceBase base;
        try { base = readBaseFields(); }
        catch (IllegalArgumentException e) { System.out.println("  Error: consumption cannot be negative."); return; }
        try {
            model.addDeviceToDivision(houseId, new RainfallSensor(base.brand(), base.modelName(), base.consumption()), division);
            System.out.println("  Rainfall sensor added.");
        } catch (HouseNotFoundException e) { System.out.println("  Error: house not found."); }
          catch (DivisionNotFoundException e) { System.out.println("  Error: division not found."); }
    }

    private void removeDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            try {
                model.removeDevice(houseId, device.getId());
                System.out.println("  Device removed.");
            } catch (DeviceNotFoundException e) {
                System.out.println("  Error: device not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    // ---- Users ----

    private boolean manageUsers(int houseId, String email) {
        boolean[] leftHouse = {false};
        Menu menu = new Menu("Users", 
                            new String[]{
                                "List Users", 
                                "Add User", 
                                "Remove User"
                            }, model::getCurrentState);

        menu.setHandler(1, () -> listUsers(houseId));
        menu.setHandler(2, () -> addUser(houseId));
        menu.setHandler(3, () -> {
            if (removeUser(houseId, email)) {
                leftHouse[0] = true;
                menu.stop();
            }
        });
        menu.run();
        return leftHouse[0];
    }

    private void listUsers(int houseId) {
        try {
            Map<Integer, UserRole> users = model.getUsersInHouse(houseId);
            if (users.isEmpty()) { System.out.println("  No users."); return; }
            Ansi.listTitle("Users");
            users.forEach((userId, role) -> {
                try {
                    String userName = model.getUserById(userId).getName();
                    Ansi.listRow(String.format("%-22s %s", userName, role));
                } catch (UserNotFoundException e) {
                    Ansi.listRow(String.format("User ID %d not found", userId));
                }
            });
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void addUser(int houseId) {
        System.out.print(Ansi.prompt("User email"));
        String email = sc.nextLine().trim();
        System.out.print(Ansi.prompt("Role (1-Admin, 2-User)"));
        int roleChoice = readInt();
        UserRole role = switch (roleChoice) {
            case 1 -> UserRole.ADMINISTRATOR;
            case 2 -> UserRole.USER;
            default -> null;
        };
        if (role == null) { System.out.println("  Invalid role choice."); return; }
        try {
            Integer userId = model.getUserByEmail(email).getId();
            model.assignUserToHouse(houseId, userId, role);

            System.out.println("  User added to house with role " + role + ".");

        } catch (UserNotFoundException e) {
            System.out.println("  Error: User with email '" + email + "' not found.");
        } catch (UserAlreadyExistsException e) {
            System.out.println("  Error: This user is already a member of this house.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private boolean removeUser(int houseId, String currentEmail) {
        System.out.print(Ansi.prompt("User email"));
        String targetEmail = sc.nextLine().trim();
        try {
            Integer userId = model.getUserByEmail(targetEmail).getId();
            model.deleteUserFromHouse(houseId, userId);
            System.out.println("  User removed from house.");
            return targetEmail.equalsIgnoreCase(currentEmail);
        } catch (UserNotFoundException e) {
            System.out.println("  Error: User with email '" + targetEmail + "' not found.");
        } catch (LastAdminException e) {
            System.out.println("  Error: Cannot remove the last administrator of the house.");
        } catch (UserAlreadyExistsException e) {
            System.out.println("  Error: internal user update conflict.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
        return false;
    }


    // ---- Operate Device ----

    private void operateDevice(int houseId, int userId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            operateSelectedDevice(houseId, device.getId(), userId);
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private Device pickDevice(int houseId) throws HouseNotFoundException {
        House house = model.getHouseById(houseId);
        Map<Integer, Device> devices = house.getDevices();
        if (devices.isEmpty()) { System.out.println("  No devices."); return null; }

        List<Device> deviceList = new ArrayList<>(devices.values());
        Ansi.listTitle("Select Device");
        for (int i = 0; i < deviceList.size(); i++) {
            Device d = deviceList.get(i);
            Ansi.listRow(String.format("%d  %-15s %-12s %s",
                    i + 1, d.getClass().getSimpleName(),
                    d.getBrand(), d.getModel()));
        }
        Ansi.listSeparator();
        System.out.print(Ansi.prompt("Device (0 to cancel)"));
        int choice = readInt();
        if (choice < 1 || choice > deviceList.size()) return null;
        return deviceList.get(choice - 1);
    }

    private void operateSelectedDevice(int houseId, int deviceId, int userId) {
        try {
            Device dev = model.getDevice(houseId, deviceId);
            Ansi.listTitle(dev.getBrand() + " " + dev.getModel());
            Ansi.listRow(String.format("%-12s %s", "Type", dev.getClass().getSimpleName()));
            Ansi.listRow(String.format("%-12s %s", "Status", dev.getStatus()));
            if (dev instanceof AdjustableDevice adjustableDevice) {
                Ansi.listRow(String.format("%-12s %d%%", "Level", adjustableDevice.getLevel()));
            }
            if (dev instanceof ColorAdjustableDevice colorAdjustableDevice) {
                Ansi.listRow(String.format("%-12s %dK", "Color Temp", colorAdjustableDevice.getColorTemperature()));
            }
            if (dev instanceof Speaker speaker) {
                Ansi.listRow(String.format("%-12s %s", "Source", speaker.getSource()));
            }
            if (dev instanceof Television television) {
                Ansi.listRow(String.format("%-12s %s", "Source", television.getSource()));
            }
            if (dev instanceof OpenableDevice openableDevice) {
                Ansi.listRow(String.format("%-12s %d%%", "Opening", openableDevice.getOpeningLevel()));
            }
            Ansi.listSeparator();

            Menu opMenu = new Menu("Operate Device",
                    new String[] { "Toggle ON/OFF", "Set Level", "Set Opening", "Set Color Temperature" },
                    model::getCurrentState);

            opMenu.setPreCondition(1, () -> dev instanceof SwitchableDevice);
            opMenu.setPreCondition(2, () -> dev instanceof AdjustableDevice);
            opMenu.setPreCondition(3, () -> dev instanceof OpenableDevice);
            opMenu.setPreCondition(4, () -> dev instanceof ColorAdjustableDevice);

            opMenu.setHandler(1, () -> toggleDevice(houseId, deviceId, userId));
            opMenu.setHandler(2, () -> setDeviceLevel(houseId, deviceId, userId));
            opMenu.setHandler(3, () -> setDeviceOpening(houseId, deviceId, userId));
            opMenu.setHandler(4, () -> setDeviceColorTemperature(houseId, deviceId, userId));
            opMenu.run();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DeviceNotFoundException e) {
            System.out.println("  Error: device not found.");
        }
    }

    private void toggleDevice(int houseId, int deviceId, int userId) {
        try {
            model.toggleDevice(houseId, deviceId, userId);
            System.out.println("  Device toggled.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DeviceNotFoundException e) {
            System.out.println("  Error: device not found.");
        } catch (DeviceIsNotInstanceOfSwitchableDeviceException e) {
            System.out.println("  Error: device cannot be toggled.");
        }
    }

    private void setDeviceLevel(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Level (0-100)"));
        int level = readIntInRange(0, 100, "Level (0-100)");
        try {
            model.setDeviceLevel(houseId, deviceId, level, userId);
            System.out.println("  Level set.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DeviceNotFoundException e) {
            System.out.println("  Error: device not found.");
        } catch (DeviceIsNotInstanceOfAdjustableDeviceException e) {
            System.out.println("  Error: device level cannot be adjusted.");
        }
    }

    private void setDeviceColorTemperature(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Color temperature (2700-4000K)"));
        int temperature = readIntInRange(2700, 4000, "Color temperature (2700-4000K)");
        try {
            model.setDeviceColorTemperature(houseId, deviceId, temperature, userId);
            System.out.println("  Color temperature set.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DeviceNotFoundException e) {
            System.out.println("  Error: device not found.");
        } catch (DeviceIsNotInstanceOfColorAdjustableDeviceException e) {
            System.out.println("  Error: device color temperature cannot be adjusted.");
        }
    }

    private void setDeviceOpening(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Opening percentage (0-100)"));
        int pct = readIntInRange(0, 100, "Opening percentage (0-100)");
        try {
            model.setDeviceOpening(houseId, deviceId, pct, userId);
            System.out.println("  Opening set.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (DeviceNotFoundException e) {
            System.out.println("  Error: device not found.");
        } catch (DeviceIsNotInstanceOfOpenableDeviceException e) {
            System.out.println("  Error: device opening cannot be adjusted.");
        }
    }

    private void showSuggestions(int houseId, int userId) {
        try {
            List<AutomationSuggestion> suggestions = model.getSuggestions(houseId, userId);
            if (suggestions.isEmpty()) {
                System.out.println("  No suggestions available yet. Interact with devices to generate patterns.");
                return;
            }

            Ansi.listTitle("Suggestions");
            for (int i = 0; i < suggestions.size(); i++) {
                AutomationSuggestion s = suggestions.get(i);
                Ansi.listRow(String.format("%d  %s", i + 1, s.getDescription()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Accept suggestion (0 to skip)"));
            int choice = readInt();
            if (choice < 1 || choice > suggestions.size()) return;

            AutomationSuggestion chosen = suggestions.get(choice - 1);
            Automation automation = chosen.getAutomation();
            model.addAutomation(houseId, automation);
            if (automation.getType() == AutomationType.SCHEDULE) {
                System.out.println("  Schedule added successfully.");
            } else {
                System.out.println("  Automation added successfully.");
            }
        } catch (NameAlreadyExistsException e) {
            System.out.println("  Error: Automation already exists.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: suggestion contains invalid schedule conditions.");
        }
    }

    // ---- Helpers ----

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int readIntInRange(int min, int max, String prompt) {
        while (true) {
            int value = readInt();
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("  Error: value must be between %d and %d.%n", min, max);
            System.out.print(Ansi.prompt(prompt));
        }
    }

    private boolean houseHasDevices(int houseId) {
        try {
            return !model.getHouseById(houseId).getDevices().isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private boolean houseHasDivisions(int houseId) {
        try {
            return !model.getHouseById(houseId).getDivisions().isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private double readDouble() {
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
