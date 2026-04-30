package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseNotFoundException;
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
        System.out.printf("%n==| House: %s |==%n", houseName);

        Menu menu = new Menu(new String[]{
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
        menu.setHandler(4, () -> manageUsers(houseId));
        menu.setHandler(5, () -> operateDevice(houseId));
        menu.setHandler(6, () -> System.out.println("Automations not yet implemented."));
        menu.setHandler(7, () -> System.out.println("Schedules not yet implemented."));
        menu.setHandler(8, () -> System.out.println("Scenarios not yet implemented."));

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
            System.out.println("\n" + house);
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---- Divisions ----

    private void manageDivisions(int houseId) {
        Menu menu = new Menu(new String[]{"List Divisions", "Add Division", "Remove Division"});
        menu.setHandler(1, () -> listDivisions(houseId));
        menu.setHandler(2, () -> addDivision(houseId));
        menu.setHandler(3, () -> removeDivision(houseId));
        menu.run();
    }

    private void listDivisions(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("No divisions."); return; }
            System.out.println("\n==| Divisions |==");
            divisions.forEach((name, devices) ->
                System.out.printf("  %s  (%d device(s))%n", name, devices.size())
            );
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addDivision(int houseId) {
        System.out.print("Division name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        try {
            model.addDivision(houseId, name);
            System.out.println("Division '" + name + "' added.");
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeDivision(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("No divisions."); return; }

            List<String> names = new ArrayList<>(divisions.keySet());
            System.out.println("\n==| Divisions |==");
            for (int i = 0; i < names.size(); i++) {
                System.out.printf("%d - %s%n", i + 1, names.get(i));
            }
            System.out.print("Select (0 to cancel): ");
            int choice = readInt();
            if (choice < 1 || choice > names.size()) return;

            model.removeDivision(houseId, names.get(choice - 1));
            System.out.println("Division removed.");
        } catch (HouseNotFoundException | DivisionNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---- Devices ----

    private void manageDevices(int houseId) {
        Menu menu = new Menu(new String[]{"List Devices", "Add Device", "Remove Device"});
        menu.setHandler(1, () -> listDevices(houseId));
        menu.setHandler(2, () -> addDevice(houseId));
        menu.setHandler(3, () -> removeDevice(houseId));
        menu.run();
    }

    private void listDevices(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<Integer, Device> devices = house.getDevices();
            if (devices.isEmpty()) { System.out.println("No devices."); return; }
            System.out.println("\n==| Devices |==");
            devices.values().forEach(d ->
                System.out.printf("  %s %s (%s) - %s%n",
                        d.getBrand(), d.getModel(),
                        d.getClass().getSimpleName(), d.getStatus())
            );
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addDevice(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            Map<String, List<Device>> divisions = house.getDivisions();
            if (divisions.isEmpty()) { System.out.println("Add a division first."); return; }

            List<String> divNames = new ArrayList<>(divisions.keySet());
            System.out.println("\n==| Select Division |==");
            for (int i = 0; i < divNames.size(); i++) {
                System.out.printf("%d - %s%n", i + 1, divNames.get(i));
            }
            System.out.print("Division (0 to cancel): ");
            int divChoice = readInt();
            if (divChoice < 1 || divChoice > divNames.size()) return;
            String division = divNames.get(divChoice - 1);

            Menu typeMenu = new Menu(new String[]{"Lamp", "Speaker", "Curtain", "Gate", "Plug", "Relay"});
            typeMenu.setHandler(1, () -> addLamp(houseId, division));
            typeMenu.setHandler(2, () -> addSpeaker(houseId, division));
            typeMenu.setHandler(3, () -> addCurtain(houseId, division));
            typeMenu.setHandler(4, () -> addGate(houseId, division));
            typeMenu.setHandler(5, () -> addPlug(houseId, division));
            typeMenu.setHandler(6, () -> addRelay(houseId, division));
            typeMenu.run();

        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private record DeviceBase(String brand, String modelName, double consumption) {}

    private DeviceBase readBaseFields() {
        System.out.print("Brand: ");
        String brand = sc.nextLine();
        System.out.print("Model name: ");
        String modelName = sc.nextLine();
        System.out.print("Consumption per hour (Wh/h): ");
        double consumption = readDouble();
        return new DeviceBase(brand, modelName, consumption);
    }

    private void addLamp(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print("Brightness (0-100): ");
        int brightness = readInt();
        System.out.print("Color temperature (K, e.g. 2700-4000): ");
        int colorTemp = readInt();
        try {
            Lamp lamp = new Lamp(base.brand(), base.modelName(), base.consumption(), brightness, colorTemp);
            model.addDeviceToDivision(houseId, lamp, division);
            System.out.println("Lamp added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addSpeaker(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print("Volume (0-100): ");
        int volume = readInt();
        System.out.print("Source: ");
        String source = sc.nextLine();
        try {
            Speaker speaker = new Speaker(base.brand(), base.modelName(), base.consumption(), volume, source);
            model.addDeviceToDivision(houseId, speaker, division);
            System.out.println("Speaker added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addCurtain(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print("Opening level (0-100): ");
        int opening = readInt();
        try {
            Curtain curtain = new Curtain(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, curtain, division);
            System.out.println("Curtain added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addGate(int houseId, String division) {
        DeviceBase base = readBaseFields();
        System.out.print("Opening level (0-100): ");
        int opening = readInt();
        try {
            Gate gate = new Gate(base.brand(), base.modelName(), base.consumption(), opening);
            model.addDeviceToDivision(houseId, gate, division);
            System.out.println("Gate added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addPlug(int houseId, String division) {
        DeviceBase base = readBaseFields();
        try {
            Plug plug = new Plug(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, plug, division);
            System.out.println("Plug added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addRelay(int houseId, String division) {
        DeviceBase base = readBaseFields();
        try {
            Relay relay = new Relay(base.brand(), base.modelName(), base.consumption());
            model.addDeviceToDivision(houseId, relay, division);
            System.out.println("Relay added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            try {
                model.removeDevice(houseId, device.getId());
                System.out.println("Device removed.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---- Users ----

    private void manageUsers(int houseId) {
        Menu menu = new Menu(new String[]{"List Users", "Add User", "Remove User"});
        menu.setHandler(1, () -> listUsers(houseId));
        menu.setHandler(2, () -> addUser(houseId));
        menu.setHandler(3, () -> removeUser(houseId));
        menu.run();
    }

    private void listUsers(int houseId) {
        try {
            Map<Integer, UserRole> users = model.getUsersInHouse(houseId);
            if (users.isEmpty()) { System.out.println("No users."); return; }
            System.out.println("\n==| Users |==");
            users.forEach((userId, role) -> {
                String userName = model.getUserById(userId).getName();
                System.out.printf("  %s (%s)%n", userName, role);
            });
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addUser(int houseId) {
        //assingnUser to house with role
        System.out.print("User email: ");
        String email = sc.nextLine().trim();
        System.out.print("Role (1-Admin, 2-User): ");
        int roleChoice = readInt();
        UserRole role = switch (roleChoice) {
            case 1 -> UserRole.ADMINISTRATOR;
            case 2 -> UserRole.USER;
            default -> null;
        };
        if (role == null) { System.out.println("Invalid role choice."); return; }
        try {
            // Primeiro, verificamos se o utilizador existe.
            // Se não existir, getUserByEmail lança UserNotFoundException.
            Integer userId = model.getUserByEmail(email).getId();

            // Se o utilizador existe, tentamos adicioná-lo à casa.
            // Este método pode lançar HouseNotFoundException.
            model.assaignUserToHouse(houseId, userId, role);

            System.out.println("User added to house with role " + role + ".");

        } catch (UserNotFoundException e) {
            System.out.println("Error: User with email '" + email + "' not found.");
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Um catch genérico para outros erros inesperados
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void removeUser(int houseId) {
        //unassign user from house
        System.out.print("User email: ");
        String email = sc.nextLine().trim();
        try {
            Integer userId = model.getUserByEmail(email).getId();
            model.deleteUserFromHouse(houseId, userId);
            System.out.println("User removed from house.");
        } catch (UserNotFoundException e) {
            System.out.println("Error: User with email '" + email + "' not found.");
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }


    // ---- Operate Device ----

    private void operateDevice(int houseId) {
        try {
            Device device = pickDevice(houseId);
            if (device == null) return;
            operateSelectedDevice(houseId, device.getId());
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private Device pickDevice(int houseId) throws HouseNotFoundException {
        House house = model.getHouseById(houseId);
        Map<Integer, Device> devices = house.getDevices();
        if (devices.isEmpty()) { System.out.println("No devices."); return null; }

        List<Device> deviceList = new ArrayList<>(devices.values());
        System.out.println("\n==| Devices |==");
        for (int i = 0; i < deviceList.size(); i++) {
            Device d = deviceList.get(i);
            System.out.printf("%d - %s %s (%s) - %s%n",
                    i + 1, d.getBrand(), d.getModel(),
                    d.getClass().getSimpleName(), d.getStatus());
        }
        System.out.print("Select device (0 to cancel): ");
        int choice = readInt();
        if (choice < 1 || choice > deviceList.size()) return null;
        return deviceList.get(choice - 1);
    }

    private void operateSelectedDevice(int houseId, int deviceId) {
        try {
            Device dev = model.getDevice(houseId, deviceId);
            System.out.println("\n" + dev.toString());

            if (dev instanceof AdjustableDevice) {
                Menu opMenu = new Menu(new String[]{"Toggle ON/OFF", "Set Level (0-100)"});
                opMenu.setHandler(1, () -> toggleDevice(houseId, deviceId));
                opMenu.setHandler(2, () -> setDeviceLevel(houseId, deviceId));
                opMenu.run();
            } else if (dev instanceof SwitchableDevice) {
                Menu opMenu = new Menu(new String[]{"Toggle ON/OFF"});
                opMenu.setHandler(1, () -> toggleDevice(houseId, deviceId));
                opMenu.run();
            } else if (dev instanceof OpenableDevice) {
                Menu opMenu = new Menu(new String[]{"Set Opening (%)"});
                opMenu.setHandler(1, () -> setDeviceOpening(houseId, deviceId));
                opMenu.run();
            } else {
                System.out.println("No supported operations for this device.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void toggleDevice(int houseId, int deviceId) {
        try {
            Device d = model.getDevice(houseId, deviceId);
            SwitchableDevice sd = (SwitchableDevice) d;
            if (sd.isOn()) sd.turnOff(); else sd.turnOn();
            model.updateDevice(houseId, d);
            System.out.println("Device is now " + (sd.isOn() ? "ON" : "OFF") + ".");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void setDeviceLevel(int houseId, int deviceId) {
        System.out.print("Level (0-100): ");
        int level = readInt();
        try {
            Device d = model.getDevice(houseId, deviceId);
            AdjustableDevice ad = (AdjustableDevice) d;
            ad.setLevel(level);
            model.updateDevice(houseId, d);
            System.out.println("Level set to " + ad.getLevel() + ".");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void setDeviceOpening(int houseId, int deviceId) {
        System.out.print("Opening percentage (0-100): ");
        int pct = readInt();
        try {
            Device d = model.getDevice(houseId, deviceId);
            OpenableDevice od = (OpenableDevice) d;
            od.setOpening(pct);
            model.updateDevice(houseId, d);
            System.out.println("Opening set to " + od.getOpeningLevel() + "%.");
        } catch (HouseNotFoundException | DeviceNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
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