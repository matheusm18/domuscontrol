package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.LastAdminException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.menu.Menu;
import domuscontrol.model.device.Curtain;
import domuscontrol.model.device.Device;
import domuscontrol.model.device.Gate;
import domuscontrol.model.device.Lamp;
import domuscontrol.model.device.Plug;
import domuscontrol.model.device.Relay;
import domuscontrol.model.device.Speaker;
import domuscontrol.model.device.types.AdjustableDevice;
import domuscontrol.model.device.types.OpenableDevice;
import domuscontrol.model.device.types.SwitchableDevice;
import domuscontrol.model.houses.House;
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
public class HouseUI {

    private DomusControl model;
    private final Scanner sc;

    /**
     * Initialises the model and shared Scanner (created once in DomusControlUI).
     * 
     * @param model shared model instance.
     * @param sc    shared Scanner (created once in DomusControlUI).
     */
    public HouseUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
    }

    public void setModel(DomusControl model) {
        this.model = model;
    }

    public void show(String email, int houseId, String houseName) {

        Menu menu = new Menu(houseName, new String[]{
                "View House Details",
                "Manage Divisions",
                "Manage Devices",
                "Manage Users",
                "Operate a Device",
                "Automations",
                "Schedules",
                "Scenarios"
        });

        menu.setPreCondition(2, () -> isAdmin(email, houseId));
        menu.setPreCondition(3, () -> isAdmin(email, houseId));
        menu.setPreCondition(4, () -> isAdmin(email, houseId));

        menu.setHandler(1, () -> viewHouseDetails(houseId));
        menu.setHandler(2, () -> manageDivisions(houseId));
        menu.setHandler(3, () -> manageDevices(houseId));
        menu.setHandler(4, () -> { if (manageUsers(houseId, email)) menu.stop(); });
        menu.setHandler(5, () -> operateDevice(houseId));
        menu.setHandler(6, () -> System.out.println("  Automations not yet implemented."));
        menu.setHandler(7, () -> System.out.println("  Schedules not yet implemented."));
        menu.setHandler(8, () -> System.out.println("  Scenarios not yet implemented."));

        menu.run();
    }

    private boolean isAdmin(String email, int houseId) {
        try {
            return model.getUserRoleInHouse(email, houseId) == UserRole.ADMINISTRATOR;
        } catch (Exception e) {
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
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ---- Divisions ----

    private void manageDivisions(int houseId) {
        Menu menu = new Menu("Divisions", new String[]{"List Divisions", "Add Division", "Remove Division"});
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
            divisions.forEach((name, devices) ->
                Ansi.listRow(String.format("%-22s %d device(s)", name, devices.size()))
            );
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addDivision(int houseId) {
        System.out.print(Ansi.prompt("Division name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  Name cannot be empty."); return; }
        try {
            model.addDivision(houseId, name);
            System.out.println("  Division '" + name + "' added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void removeDivision(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("  No divisions."); return; }

            List<String> names = new ArrayList<>(divisions.keySet());
            Ansi.listTitle("Divisions");
            for (int i = 0; i < names.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, names.get(i)));
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Select (0 to cancel)"));
            int choice = readInt();
            if (choice < 1 || choice > names.size()) return;

            model.removeDivision(houseId, names.get(choice - 1));
            System.out.println("  Division removed.");
        } catch (HouseNotFoundException | DivisionNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ---- Devices ----

    private void manageDevices(int houseId) {
        Menu menu = new Menu("Devices", new String[]{"List Devices", "Add Device", "Remove Device"});
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
            Ansi.listTitle("Devices");
            devices.values().forEach(d ->
                Ansi.listRow(String.format("%-10s %-10s %-10s %s",
                        d.getBrand(), d.getModel(),
                        d.getClass().getSimpleName(), d.getStatus()))
            );
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
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

            Menu typeMenu = new Menu("Device Type", new String[]{"Lamp", "Speaker", "Curtain", "Gate", "Plug", "Relay"});
            typeMenu.setHandler(1, () -> addLamp(houseId, division));
            typeMenu.setHandler(2, () -> addSpeaker(houseId, division));
            typeMenu.setHandler(3, () -> addCurtain(houseId, division));
            typeMenu.setHandler(4, () -> addGate(houseId, division));
            typeMenu.setHandler(5, () -> addPlug(houseId, division));
            typeMenu.setHandler(6, () -> addRelay(houseId, division));
            typeMenu.run();

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
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
        return new DeviceBase(brand, modelName, consumption);
    }

    private void addLamp(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print(Ansi.prompt("Brightness (0-100)"));
        int brightness = readInt();
        System.out.print(Ansi.prompt("Color temperature (K, e.g. 2700-4000)"));
        int colorTemp = readInt();
        try {
            Lamp lamp = new Lamp(base.brand(), base.modelName(), base.consumption(), brightness, colorTemp);
            model.addDeviceToDivision(houseId, lamp, division);
            System.out.println("  Lamp added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addSpeaker(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print(Ansi.prompt("Volume (0-100)"));
        int volume = readInt();
        System.out.print(Ansi.prompt("Source"));
        String source = sc.nextLine();
        try {
            Speaker speaker = new Speaker(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, speaker, division);
            System.out.println("  Speaker added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addCurtain(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        try {
            Curtain curtain = new Curtain(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, curtain, division);
            System.out.println("  Curtain added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addGate(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print(Ansi.prompt("Opening level (0-100)"));
        int opening = readInt();
        try {
            Gate gate = new Gate(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, gate, division);
            System.out.println("  Gate added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addPlug(int houseId, String division) {
        DeviceBase base = readBaseFields();
        try {
            Plug plug = new Plug(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, plug, division);
            System.out.println("  Plug added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addRelay(int houseId, String division) {
        DeviceBase base = readBaseFields();
        try {
            Relay relay = new Relay(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, relay, division);
            System.out.println("  Relay added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void removeDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            try {
                model.removeDevice(houseId, device.getId());
                System.out.println("  Device removed.");
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ---- Users ----

    private boolean manageUsers(int houseId, String email) {
        boolean[] leftHouse = {false};
        Menu menu = new Menu("Users", new String[]{"List Users", "Add User", "Remove User"});
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
                String userName = model.getUserById(userId).getName();
                Ansi.listRow(String.format("%-22s %s", userName, role));
            });
            Ansi.listSeparator();
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addUser(int houseId) {
        //assignUser to house with role
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
            // Primeiro, verificamos se o utilizador existe.
            // Se não existir, getUserByEmail lança UserNotFoundException.
            Integer userId = model.getUserByEmail(email).getId();

            // Se o utilizador existe, tentamos adicioná-lo à casa.
            // Este método pode lançar HouseNotFoundException.
            model.assignUserToHouse(houseId, userId, role);

            System.out.println("  User added to house with role " + role + ".");

        } catch (UserNotFoundException e) {
            System.out.println("  Error: User with email '" + email + "' not found.");
        } catch (UserAlreadyExistsException e) {
            System.out.println("  Error: This user is already a member of this house.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  An unexpected error occurred: " + e.getMessage());
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
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  An unexpected error occurred: " + e.getMessage());
        }
        return false;
    }


    // ---- Operate Device ----

    private void operateDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            operateSelectedDevice(houseId, device.getId());
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private Device pickDevice(int houseId) throws HouseNotFoundException {
        House house = model.getHouseById(houseId);
        Map<Integer, Device> devices = house.getDevices();
        if (devices.isEmpty()) { System.out.println("  No devices."); return null; }

        List<Device> deviceList = new ArrayList<>(devices.values());
        Ansi.listTitle("Devices");
        for (int i = 0; i < deviceList.size(); i++) {
            Device d = deviceList.get(i);
            Ansi.listRow(String.format("%d  %-10s %-10s %-10s %s",
                    i + 1, d.getBrand(), d.getModel(),
                    d.getClass().getSimpleName(), d.getStatus()));
        }
        Ansi.listSeparator();
        System.out.print(Ansi.prompt("Select device (0 to cancel)"));
        int choice = readInt();
        if (choice < 1 || choice > deviceList.size()) return null;
        return deviceList.get(choice - 1);
    }

    private void operateSelectedDevice(int houseId, int deviceId) {
        try {
            Device dev = model.getDevice(houseId, deviceId);
            Ansi.listTitle(dev.getBrand() + " " + dev.getModel());
            Ansi.listRow(String.format("%-12s %s", "Type", dev.getClass().getSimpleName()));
            Ansi.listRow(String.format("%-12s %s", "Status", dev.getStatus()));
            Ansi.listSeparator();

            if (dev instanceof AdjustableDevice) {
                Menu opMenu = new Menu("Operate Device", new String[]{"Toggle ON/OFF", "Set Level (0-100)"});
                opMenu.setHandler(1, () -> toggleDevice(houseId, deviceId));
                opMenu.setHandler(2, () -> setDeviceLevel(houseId, deviceId));
                opMenu.run();
            } else if (dev instanceof SwitchableDevice) {
                Menu opMenu = new Menu("Operate Device", new String[]{"Toggle ON/OFF"});
                opMenu.setHandler(1, () -> toggleDevice(houseId, deviceId));
                opMenu.run();
            } else if (dev instanceof OpenableDevice) {
                Menu opMenu = new Menu("Operate Device", new String[]{"Set Opening (%)"});
                opMenu.setHandler(1, () -> setDeviceOpening(houseId, deviceId));
                opMenu.run();
            } else {
                System.out.println("  No supported operations for this device.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void toggleDevice(int houseId, int deviceId) {
        try {
            Device d = model.getDevice(houseId, deviceId);
            SwitchableDevice sd = (SwitchableDevice) d;
            if (sd.isOn()) sd.turnOff(); else sd.turnOn();
            model.updateDevice(houseId, d);
            System.out.println("  Device is now " + (sd.isOn() ? "ON" : "OFF") + ".");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void setDeviceLevel(int houseId, int deviceId) {
        System.out.print(Ansi.prompt("Level (0-100)"));
        int level = readInt();
        try {
            Device d = model.getDevice(houseId, deviceId);
            AdjustableDevice ad = (AdjustableDevice) d;
            ad.setLevel(level);
            model.updateDevice(houseId, d);
            System.out.println("  Level set to " + ad.getLevel() + ".");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void setDeviceOpening(int houseId, int deviceId) {
        System.out.print(Ansi.prompt("Opening percentage (0-100)"));
        int pct = readInt();
        try {
            Device d = model.getDevice(houseId, deviceId);
            OpenableDevice od = (OpenableDevice) d;
            od.setOpening(pct);
            model.updateDevice(houseId, d);
            System.out.println("  Opening set to " + od.getOpeningLevel() + "%.");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
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

    private double readDouble() {
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}