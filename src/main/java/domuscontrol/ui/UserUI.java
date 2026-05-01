package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.menu.Menu;
import domuscontrol.model.device.Device;
import domuscontrol.model.houses.House;
import domuscontrol.model.houses.DivisionInfo;
import domuscontrol.user.User;
import domuscontrol.user.UserRole;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles the authenticated user's dashboard.
 * Receives the model and Scanner from {@link DomusControlUI} and delegates
 * house-level operations to {@link HouseUI}.
 */
public class UserUI {

    private DomusControl model;
    private final Scanner sc;
    private final HouseUI houseUI;

    /**
     * Initialises the UserUI with the shared model and Scanner, and creates the HouseUI sub-component.
     * 
     * @param model shared model instance.
     * @param sc    shared Scanner (created once in DomusControlUI).
     */
    public UserUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
        this.houseUI = new HouseUI(model, sc);
    }

    public void setModel(DomusControl model) {
        this.model = model;
        this.houseUI.setModel(model);
    }

    public void show(String email) {
        Menu menu = new Menu(new String[]{
                "My Houses",
                "Create House",
                "My Profile",
                "Statistics",
                "Advance Simulation",
                "Save State"
        });

        menu.setHandler(1, () -> myHouses(email));
        menu.setHandler(2, () -> createHouse(email));
        menu.setHandler(3, () -> myProfile(email));
        menu.setHandler(4, () -> statistics(email));
        menu.setHandler(5, this::advanceSimulation);
        menu.setHandler(6, this::saveState);

        menu.run();
    }

    private void myHouses(String email) {
        try {
            List<House> houses = model.getHousesByUser(email);
            if (houses.isEmpty()) {
                System.out.println("You have no houses yet.");
                return;
            }
            System.out.println("\n==| Your Houses |==");
            for (int i = 0; i < houses.size(); i++) {
                House h = houses.get(i);
                System.out.printf("%d - %s %n", i + 1, h.getName());
            }
            System.out.print("Select house (0 to cancel): ");
            int choice = readInt();
            if (choice < 1 || choice > houses.size()) return;

            House selected = houses.get(choice - 1);
            houseUI.show(email, selected.getId(), selected.getName());

        } catch (UserNotFoundException | HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void createHouse(String email) {
        System.out.print("House name: ");
        String name = sc.nextLine();
        try {
            House house = model.createHouse(email, name);
            System.out.printf("House '%s' created.%n", house.getName());
        } catch (UserNotFoundException | HouseAlreadyExistsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void myProfile(String email) {
        try {
            User user = model.getUserByEmail(email);
            System.out.println("\n==| Profile |==");
            showUserDetails(user);

        } catch (UserNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        Menu menu = new Menu(new String[]{"Change Name", "Change Password"});
        menu.setHandler(1, () -> {
            System.out.print("New name: ");
            String name = sc.nextLine();
            try {
                model.updateUserName(email, name);
                System.out.println("Name updated.");
            } catch (UserNotFoundException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });
        menu.setHandler(2, () -> {
            System.out.print("New password: ");
            String pass = sc.nextLine();
            try {
                model.updateUserPassword(email, pass);
                System.out.println("Password updated.");
            } catch (UserNotFoundException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });
        menu.run();
    }

/**
     * Displays the details of a user.
     * @param user The user to display.
     */
    public void showUserDetails(User user) {
        if (user == null) {
            System.out.println("No user to display.");
            return;
        }
        System.out.println("User Details:");
        System.out.println("ID: " + user.getId());
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Roles:");

        Map<Integer, UserRole> roles = user.getRolesByHouseId();
        if (roles.isEmpty()) {
            System.out.println("  No roles assigned.");
        } else {
            for (Map.Entry<Integer, UserRole> entry : roles.entrySet()) {
                try {
                    // Usar o novo método para obter o nome da casa pelo ID
                    String houseName = this.model.getHouseById(entry.getKey()).getName();
                    System.out.println("  " + houseName + ": " + entry.getValue());
                } catch (HouseNotFoundException e) {
                    // Esta exceção pode acontecer se uma casa for removida mas a referência no user permanecer
                    System.out.println("  House with ID " + entry.getKey() + " not found: " + entry.getValue());
                }
            }
        }
    }

    private void statistics(String email) {
        Menu menu = new Menu(new String[]{
                "Most consuming houseses",
                "Top 3 devices by time on",
                "Top 3 devices by activations",
                "Top 3 divisions by device count"
        });

        menu.setHandler(1, () -> {
            List<House> topHouses = model.getTop3MostConsumingHouses(email);
            if (topHouses.isEmpty()) {
                System.out.println("No houses in the system.");
            } else {
                for (int i = 0; i < topHouses.size(); i++) {
                    House h = topHouses.get(i);
                    System.out.printf("%d. %s — %.2f Wh%n", i + 1, h.getName(), h.calculateTotalConsumption());
                }
            }
        });
        menu.setHandler(2, () -> {
            List<Device> topDevices = model.getTopDevicesByCriterion(email, 3, Device::getTotalMinutesOn);
            if (topDevices.isEmpty()) {
                System.out.println("No devices found for your houses.");
            } else {
                for (int i = 0; i < topDevices.size(); i++) {
                    Device d = topDevices.get(i);
                    System.out.printf("[%d] %d. %s %s — %d minutes on%n", 
                        d.getClass().getSimpleName(), i + 1, d.getBrand(), d.getModel(), d.getTotalMinutesOn());
                }
            }
        });
        menu.setHandler(3, () -> {
            List<Device> topDevices = model.getTopDevicesByCriterion(email, 3, Device::getTotalActivations);
            if (topDevices.isEmpty()) {
                System.out.println("No devices found for your houses.");
            } else {
                for (int i = 0; i < topDevices.size(); i++) {
                    Device d = topDevices.get(i);
                    System.out.printf("[%d] %d. %s %s — %d activations%n", 
                        d.getClass().getSimpleName(), i + 1, d.getBrand(), d.getModel(), d.getTotalActivations());
                }
            }
        });
        menu.setHandler(4, () -> {
            List<DivisionInfo> topDivisions = model.getTopDivisionsByCriterion(email, 3, div -> div.house.getDivisions().get(div.divisionName).size());

            if (topDivisions.isEmpty()) {
                System.out.println("No divisions found for your houses.");
            } else {
                for (int i = 0; i < topDivisions.size(); i++) {
                    DivisionInfo div = topDivisions.get(i);
                    System.out.printf("%d. Divisão: %s (Casa: %s) — %d dispositivos%n", 
                        i + 1, 
                        div.divisionName, 
                        div.house.getName(), 
                        div.devices.size());
                }
            }
        });

        menu.run();
    }

    private void advanceSimulation() {
        System.out.print("Minutes to advance: ");
        int minutes = readInt();
        if (minutes <= 0) { System.out.println("Must be a positive number."); return; }
        model.tick(minutes);
        System.out.println("Simulation advanced by " + minutes + " minute(s).");
    }

    private void saveState() {
        System.out.print("File name: ");
        String name = sc.nextLine().trim();
        String path = "saves/" + name;
        new java.io.File("saves").mkdirs();
        try {
            model.saveState(path);
            System.out.println("State saved to: " + path);
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}