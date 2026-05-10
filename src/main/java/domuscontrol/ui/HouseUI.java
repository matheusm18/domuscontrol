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
import domuscontrol.simulation.SimulationState;
import domuscontrol.routines.Automation;
import domuscontrol.routines.AutomationType;
import domuscontrol.suggestions.AutomationSuggestion;
import domuscontrol.user.UserRole;
import domuscontrol.utils.Ansi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    /** The application model facade. */
    private DomusControl model;

    /** Shared scanner for reading user input. */
    private final Scanner sc;

    /** Sub-UI for automation and scenario operations. */
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
            Ansi.error("Error: User not found.");
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
        }, () -> stateHeader(model.getCurrentState()));

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
        menu.setHandler(4, () -> { if (manageUsers(houseId, email)) menu.stop(); });
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
            Ansi.listRow(String.format("%-12s %d", "Divisions", model.getDivisions(houseId).size()));
            Ansi.listRow(String.format("%-12s %d", "Devices", model.getDevices(houseId).size()));
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        }
    }

    // ---- Divisions ----

    private void manageDivisions(int houseId) {
        Menu menu = new Menu("Divisions", new String[]{"List Divisions", "Add Division", "Remove Division"}, () -> stateHeader(model.getCurrentState()));
        menu.setPreCondition(1, () -> houseHasDivisions(houseId));
        menu.setPreCondition(3, () -> houseHasDivisions(houseId));
        menu.setHandler(1, () -> listDivisions(houseId));
        menu.setHandler(2, () -> addDivision(houseId));
        menu.setHandler(3, () -> removeDivision(houseId));
        menu.run();
    }

    private void listDivisions(int houseId) {
        try {
            Map<String, List<Device>> divisions = model.getDivisions(houseId);
            if (divisions.isEmpty()) { System.out.println("  No divisions."); return; }
            int idW = divisions.values().stream()
                .flatMap(List::stream)
                .mapToInt(d -> String.valueOf(d.getId()).length())
                .max().orElse(1);
            Ansi.listTitle("Divisions");
            for (Map.Entry<String, List<Device>> entry : divisions.entrySet()) {
                Ansi.listRow(entry.getKey());
                for (Device d : entry.getValue())
                    Ansi.listRow(String.format("    [#%-" + idW + "d] %-18s %s %s",
                        d.getId(), d.getClass().getSimpleName(), d.getBrand(), d.getModel()));
            }
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
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
            Ansi.error("Error: house not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: division name already exists.");
        }
    }

    private void removeDivision(int houseId) {
        try {
            Map<String, List<Device>> divisions = model.getDivisions(houseId);
            if (divisions.isEmpty()) { System.out.println("  No divisions."); return; }

            List<String> names = new ArrayList<>(divisions.keySet());
            Ansi.listTitle("Select Division");
            for (int i = 0; i < names.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, names.get(i)));
            Ansi.listSeparator();
            int choice = readSelection("Division (0 to cancel)", names.size());
            if (choice == 0) return;

            String divName = names.get(choice - 1);
            int deviceCount = divisions.get(divName).size();
            if (deviceCount > 0) {
                String confirm;
                do {
                    System.out.printf("  Warning: %d device(s) will also be deleted. Confirm? (y/n) ", deviceCount);
                    confirm = sc.nextLine().trim().toLowerCase();
                } while (!confirm.equals("y") && !confirm.equals("n"));
                if (!confirm.equals("y")) return;
            }

            model.removeDivision(houseId, divName);
            System.out.println("  Division removed.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        }
    }

    // ---- Devices ----

    private void manageDevices(int houseId) {
        Menu menu = new Menu("Devices", 
                            new String[]{
                                "List Devices", 
                                "Add Device", 
                                "Remove Device"
                            }, () -> stateHeader(model.getCurrentState()));

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
            Map<Integer, Device> devices = model.getDevices(houseId);
            if (devices.isEmpty()) { System.out.println("  No devices."); return; }
            Map<Integer, String> divisionMap = model.getDeviceDivisionMap(houseId);
            List<Device> sorted = devices.values().stream()
                .sorted(java.util.Comparator.comparingInt(Device::getId)).toList();
            int[] w = deviceColWidths(sorted, divisionMap);
            Ansi.listTitle("Select Device");
            sorted.forEach(device ->
                Ansi.listRow(String.format("%-" + w[0] + "d  %-" + w[1] + "s %-" + w[2] + "s %-" + w[3] + "s %s",
                    device.getId(),
                    device.getClass().getSimpleName(),
                    device.getBrand(),
                    device.getModel(),
                    divisionMap.getOrDefault(device.getId(), "")))
            );
            Ansi.listSeparator();
            while (true) {
                System.out.print(Ansi.prompt("Device (0 to cancel)"));
                int selectedId = readInt();
                if (selectedId == 0) return;
                if (devices.containsKey(selectedId)) { showDeviceInfo(devices.get(selectedId)); break; }
                Ansi.error("Invalid selection.");
            }
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        }
    }

    private void showDeviceInfo(Device device) {
        String[] lines = device.toString().split("\n");
        if (lines.length == 0) return;
        
        Ansi.listTitle(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isEmpty()) {
                Ansi.listRow(line);
            }
        }
        Ansi.listSeparator();
    }

    private void addDevice(int houseId) {
        try {
            Map<String, List<Device>> divisions = model.getDivisions(houseId);
            if (divisions.isEmpty()) { System.out.println("  Add a division first."); return; }

            List<String> divNames = new ArrayList<>(divisions.keySet());
            Ansi.listTitle("Select Division");
            for (int i = 0; i < divNames.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, divNames.get(i)));
            Ansi.listSeparator();
            int divChoice = readSelection("Division (0 to cancel)", divNames.size());
            if (divChoice == 0) return;
            String division = divNames.get(divChoice - 1);

            Menu typeMenu = new Menu("Device Type", new String[]{
                    "Lamp", "Speaker", "Curtain", "Gate", "Plug", "Relay",
                    "Heater", "Fan", "Air Conditioner", "Television",
                    "Temperature Sensor", "Luminosity Sensor", "Rainfall Sensor"
            }, () -> stateHeader(model.getCurrentState()));
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
            Ansi.error("Error: house not found.");
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
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Brightness (0-100)"));
        int brightness = readInt();
        if (brightness < 0 || brightness > 100) {
            Ansi.error("Error: Brightness must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Color temperature (K, e.g. 2700-4000)"));
        int colorTemp = readInt();
        if (colorTemp < 2700 || colorTemp > 4000) {
            Ansi.error("Error: Color temperature must be between 2700 and 4000 K.");
            return;
        }
        try {
            Lamp lamp = new Lamp(base.brand(), base.modelName(), base.consumption(), brightness, colorTemp);
            model.addDeviceToDivision(houseId, lamp, division);
            System.out.println("  " + lamp.getBrand() + " " + lamp.getModel() + " [#" + lamp.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addSpeaker(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Volume (0-100)"));
        int volume = readInt();
        if (volume < 0 || volume > 100) {
            Ansi.error("Error: Volume must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Source"));
        String source = sc.nextLine();
        try {
            Speaker speaker = new Speaker(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, speaker, division);
            System.out.println("  " + speaker.getBrand() + " " + speaker.getModel() + " [#" + speaker.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addCurtain(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        if (opening < 0 || opening > 100) {
            Ansi.error("Error: Opening level must be between 0 and 100.");
            return;
        }
        try {
            Curtain curtain = new Curtain(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, curtain, division);
            System.out.println("  " + curtain.getBrand() + " " + curtain.getModel() + " [#" + curtain.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addGate(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        if (opening < 0 || opening > 100) {
            Ansi.error("Error: Opening level must be between 0 and 100.");
            return;
        }
        try {
            Gate gate = new Gate(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, gate, division);
            System.out.println("  " + gate.getBrand() + " " + gate.getModel() + " [#" + gate.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addPlug(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        try {
            Plug plug = new Plug(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, plug, division);
            System.out.println("  " + plug.getBrand() + " " + plug.getModel() + " [#" + plug.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addRelay(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        try {
            Relay relay = new Relay(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, relay, division);
            System.out.println("  " + relay.getBrand() + " " + relay.getModel() + " [#" + relay.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addHeater(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Heating power (0-100)"));
        int power = readInt();
        if (power < 0 || power > 100) {
            Ansi.error("Error: Heating power must be between 0 and 100.");
            return;
        }
        try {
            Heater heater = new Heater(base.brand(), base.modelName(), base.consumption(), power);
            model.addDeviceToDivision(houseId, heater, division);
            System.out.println("  " + heater.getBrand() + " " + heater.getModel() + " [#" + heater.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addFan(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Speed (0-100)"));
        int speed = readInt();
        if (speed < 0 || speed > 100) {
            Ansi.error("Error: Speed must be between 0 and 100.");
            return;
        }
        try {
            Fan fan = new Fan(base.brand(), base.modelName(), base.consumption(), speed);
            model.addDeviceToDivision(houseId, fan, division);
            System.out.println("  " + fan.getBrand() + " " + fan.getModel() + " [#" + fan.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addAirConditioner(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Cooling power (0-100)"));
        int coolingPower = readInt();
        if (coolingPower < 0 || coolingPower > 100) {
            Ansi.error("Error: Cooling power must be between 0 and 100.");
            return;
        }
        try {
            AirConditioner airConditioner = new AirConditioner(base.brand(), base.modelName(), base.consumption(), coolingPower);
            model.addDeviceToDivision(houseId, airConditioner, division);
            System.out.println("  " + airConditioner.getBrand() + " " + airConditioner.getModel() + " [#" + airConditioner.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addTelevision(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        }
        catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        System.out.print(Ansi.prompt("Volume (0-100)"));
        int volume = readInt();
        if (volume < 0 || volume > 100) {
            Ansi.error("Error: Volume must be between 0 and 100.");
            return;
        }
        System.out.print(Ansi.prompt("Source"));
        String source = sc.nextLine();
        try {
            Television television = new Television(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, television, division);
            System.out.println("  " + television.getBrand() + " " + television.getModel() + " [#" + television.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addTemperatureSensor(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        } catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        try {
            TemperatureSensor sensor = new TemperatureSensor(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, sensor, division);
            System.out.println("  " + sensor.getBrand() + " " + sensor.getModel() + " [#" + sensor.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addLuminositySensor(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        } catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        try {
            LuminositySensor sensor = new LuminositySensor(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, sensor, division);
            System.out.println("  " + sensor.getBrand() + " " + sensor.getModel() + " [#" + sensor.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void addRainfallSensor(int houseId, String division) {
        DeviceBase base;
        try {
            base = readBaseFields();
        } catch (IllegalArgumentException e) {
            Ansi.error("Error: consumption cannot be negative.");
            return;
        }
        try {
            RainfallSensor sensor = new RainfallSensor(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, sensor, division);
            System.out.println("  " + sensor.getBrand() + " " + sensor.getModel() + " [#" + sensor.getId() + "] added.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DivisionNotFoundException e) {
            Ansi.error("Error: division not found.");
        } catch (NameAlreadyExistsException e) {
            Ansi.error("Error: device already in this division.");
        }
    }

    private void removeDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            try {
                model.removeDevice(houseId, device.getId());
                System.out.println("  " + device.getBrand() + " " + device.getModel() + " [#" + device.getId() + "] removed.");
            } catch (DeviceNotFoundException e) {
                Ansi.error("Error: device not found.");
            } catch (HouseNotFoundException e) {
                Ansi.error("Error: house not found.");
            }
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
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
                            }, () -> stateHeader(model.getCurrentState()));

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
            Ansi.error("Error: house not found.");
        }
    }

    private void addUser(int houseId) {
        System.out.print(Ansi.prompt("User email"));
        String email = sc.nextLine().trim();
        int roleChoice;
        while (true) {
            System.out.print(Ansi.prompt("Role (1-Admin, 2-User)"));
            roleChoice = readInt();
            if (roleChoice == 1 || roleChoice == 2) break;
            Ansi.error("Invalid selection.");
        }
        UserRole role = roleChoice == 1 ? UserRole.ADMINISTRATOR : UserRole.USER;
        try {
            Integer userId = model.getUserByEmail(email).getId();
            model.assignUserToHouse(houseId, userId, role);

            System.out.println("  User added to house with role " + role + ".");

        } catch (UserNotFoundException e) {
            Ansi.error("Error: User with email '" + email + "' not found.");
        } catch (UserAlreadyExistsException e) {
            Ansi.error("Error: This user is already a member of this house.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
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
            Ansi.error("Error: User with email '" + targetEmail + "' not found.");
        } catch (LastAdminException e) {
            Ansi.error("Error: Cannot remove the last administrator of the house.");
        } catch (UserAlreadyExistsException e) {
            Ansi.error("Error: internal user update conflict.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
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
            Ansi.error("Error: house not found.");
        }
    }

    private Device pickDevice(int houseId) throws HouseNotFoundException {
        Map<Integer, Device> devices = model.getDevices(houseId);
        if (devices.isEmpty()) { System.out.println("  No devices."); return null; }

        Map<Integer, String> divisionMap = model.getDeviceDivisionMap(houseId);
        List<Device> deviceList = devices.values().stream()
            .sorted(java.util.Comparator.comparingInt(Device::getId)).toList();
        int[] w = deviceColWidths(deviceList, divisionMap);
        Ansi.listTitle("Select Device");
        for (Device d : deviceList) {
            Ansi.listRow(String.format("%-" + w[0] + "d  %-" + w[1] + "s %-" + w[2] + "s %-" + w[3] + "s %s",
                    d.getId(), d.getClass().getSimpleName(),
                    d.getBrand(), d.getModel(),
                    divisionMap.getOrDefault(d.getId(), "")));
        }
        Ansi.listSeparator();
        while (true) {
            System.out.print(Ansi.prompt("Device (0 to cancel)"));
            int choice = readInt();
            if (choice == 0) return null;
            if (devices.containsKey(choice)) return devices.get(choice);
            Ansi.error("Invalid selection.");
        }
    }

    private void operateSelectedDevice(int houseId, int deviceId, int userId) {
        try {
            Device dev = model.getDevice(houseId, deviceId);

            showDeviceInfo(dev);

            Menu opMenu = new Menu("Operate Device",
                    new String[] { "Toggle ON/OFF", "Set Level", "Set Opening", "Set Color Temperature" },
                    () -> stateHeader(model.getCurrentState()));

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
            Ansi.error("Error: house not found.");
        } catch (DeviceNotFoundException e) {
            Ansi.error("Error: device not found.");
        }
    }

    private void toggleDevice(int houseId, int deviceId, int userId) {
        try {
            model.toggleDevice(houseId, deviceId, userId);
            System.out.println("  Device toggled.");
            showDeviceInfo(model.getDevice(houseId, deviceId));
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DeviceNotFoundException e) {
            Ansi.error("Error: device not found.");
        } catch (DeviceIsNotInstanceOfSwitchableDeviceException e) {
            Ansi.error("Error: device cannot be toggled.");
        }
    }

    private void setDeviceLevel(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Level (0-100)"));
        int level = readIntInRange(0, 100, "Level (0-100)");
        try {
            model.setDeviceLevel(houseId, deviceId, level, userId);
            System.out.println("  Level set.");
            showDeviceInfo(model.getDevice(houseId, deviceId));
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DeviceNotFoundException e) {
            Ansi.error("Error: device not found.");
        } catch (DeviceIsNotInstanceOfAdjustableDeviceException e) {
            Ansi.error("Error: device level cannot be adjusted.");
        }
    }

    private void setDeviceColorTemperature(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Color temperature (2700-4000K)"));
        int temperature = readIntInRange(2700, 4000, "Color temperature (2700-4000K)");
        try {
            model.setDeviceColorTemperature(houseId, deviceId, temperature, userId);
            System.out.println("  Color temperature set.");
            showDeviceInfo(model.getDevice(houseId, deviceId));
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DeviceNotFoundException e) {
            Ansi.error("Error: device not found.");
        } catch (DeviceIsNotInstanceOfColorAdjustableDeviceException e) {
            Ansi.error("Error: device color temperature cannot be adjusted.");
        }
    }

    private void setDeviceOpening(int houseId, int deviceId, int userId) {
        System.out.print(Ansi.prompt("Opening percentage (0-100)"));
        int pct = readIntInRange(0, 100, "Opening percentage (0-100)");
        try {
            model.setDeviceOpening(houseId, deviceId, pct, userId);
            System.out.println("  Opening set.");
            showDeviceInfo(model.getDevice(houseId, deviceId));
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (DeviceNotFoundException e) {
            Ansi.error("Error: device not found.");
        } catch (DeviceIsNotInstanceOfOpenableDeviceException e) {
            Ansi.error("Error: device opening cannot be adjusted.");
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
            Ansi.error("Error: Automation already exists.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            Ansi.error("Error: suggestion contains invalid schedule conditions.");
        }
    }

    // ---- Helpers ----

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print(Ansi.prompt("Invalid input. Please enter an integer"));
            }
        }
    }

    private int readSelection(String prompt, int max) {
        while (true) {
            System.out.print(Ansi.prompt(prompt));
            int choice = readInt();
            if (choice == 0 || (choice >= 1 && choice <= max)) return choice;
            Ansi.error("Invalid selection.");
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

    private int[] deviceColWidths(Collection<Device> devices, Map<Integer, String> divisionMap) {
        int id    = devices.stream().mapToInt(d -> String.valueOf(d.getId()).length()).max().orElse(1);
        int type  = devices.stream().mapToInt(d -> d.getClass().getSimpleName().length()).max().orElse(10);
        int brand = devices.stream().mapToInt(d -> d.getBrand().length()).max().orElse(8);
        int model = devices.stream().mapToInt(d -> d.getModel().length()).max().orElse(10);
        int div   = divisionMap.values().stream().mapToInt(String::length).max().orElse(10);
        return new int[]{id, type + 2, brand + 2, model + 2, div + 2};
    }

    private boolean houseHasDevices(int houseId) {
        try {
            return !model.getDevices(houseId).isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private boolean houseHasDivisions(int houseId) {
        try {
            return !model.getDivisions(houseId).isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print(Ansi.prompt("Invalid input. Please enter a decimal (e.g. 15.5)"));
            }
        }
    }

    private static String stateHeader(SimulationState s) {
        return s.getCurrentDateTime().toLocalDate() + "  " + s.getCurrentDateTime().toLocalTime() + "\n" +
               String.format("%.1fºC  %s  %.0f lx", s.getTemperature(), s.getWeather(), s.getLuminosity());
    }
}
