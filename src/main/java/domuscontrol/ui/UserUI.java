package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.*;
import domuscontrol.houses.DivisionInfo;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.user.User;
import domuscontrol.user.UserRole;
import domuscontrol.utils.Ansi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class UserUI {

    private DomusControl model;
    private final Scanner sc;
    private final HouseUI houseUI;

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
        Menu menu = new Menu("Dashboard", new String[]{
                "My Houses",
                "Create House",
                "My Profile",
                "Statistics",
                "Advance Simulation",
                "Save State"
        }, model::getCurrentState);

        menu.setPreCondition(1, () -> hasHouses(email));
        menu.setPreCondition(4, () -> hasHouses(email));

        menu.setHandler(1, () -> myHouses(email));
        menu.setHandler(2, () -> createHouse(email));
        menu.setHandler(3, () -> myProfile(email));
        menu.setHandler(4, () -> statistics(email));
        menu.setHandler(5, this::advanceSimulation);
        menu.setHandler(6, this::saveState);

        menu.run();
    }

    private boolean hasHouses(String email) {
        try {
            return !model.getHousesByUser(email).isEmpty();
        } catch (UserNotFoundException | HouseNotFoundException e) {
            return false;
        }
    }

    private void myHouses(String email) {
        try {
            List<House> houses = model.getHousesByUser(email);
            if (houses.isEmpty()) {
                System.out.println("  You have no houses yet.");
                return;
            }
            Ansi.listTitle("Your Houses");
            for (int i = 0; i < houses.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, houses.get(i).getName()));
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Select house (0 to cancel)"));
            int choice = readInt();
            if (choice < 1 || choice > houses.size()) return;

            House selected = houses.get(choice - 1);
            houseUI.show(email, selected.getId(), selected.getName());

        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: one of your houses was not found.");
        }
    }

    private void createHouse(String email) {
        System.out.print(Ansi.prompt("House name"));
        String name = sc.nextLine();
        try {
            House house = model.createHouse(email, name);
            System.out.printf("  House '%s' created.%n", house.getName());
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (HouseAlreadyExistsException | UserAlreadyExistsException e) {
            System.out.println("  Error: conflict creating house.");
        }
    }

    private void myProfile(String email) {
        try {
            User user = model.getUserByEmail(email);
            System.out.println();
            showUserDetails(user);
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
            return;
        }

        Menu menu = new Menu("Profile", new String[]{"Change Name", "Change Password"}, model::getCurrentState);
        menu.setHandler(1, () -> {
            System.out.print(Ansi.prompt("New name"));
            String name = sc.nextLine();
            try {
                model.updateUserName(email, name);
                System.out.println("  Name updated.");
            } catch (UserNotFoundException | UserAlreadyExistsException ex) {
                System.out.println("  Error: " + ex.getMessage());
            }
        });
        menu.setHandler(2, () -> {
            System.out.print(Ansi.prompt("New password"));
            String pass = sc.nextLine();
            try {
                model.updateUserPassword(email, pass);
                System.out.println("  Password updated.");
            } catch (UserNotFoundException | UserAlreadyExistsException ex) {
                System.out.println("  Error: " + ex.getMessage());
            }
        });
        menu.run();
    }

    public void showUserDetails(User user) {
        if (user == null) {
            System.out.println("  No user to display.");
            return;
        }
        Ansi.listTitle("Profile");
        Ansi.listRow(String.format("%-10s %s", "ID", user.getId()));
        Ansi.listRow(String.format("%-10s %s", "Name", user.getName()));
        Ansi.listRow(String.format("%-10s %s", "Email", user.getEmail()));

        Map<Integer, UserRole> roles = user.getRolesByHouseId();
        Ansi.listSeparator();
        if (roles.isEmpty()) {
            Ansi.listRow("No roles assigned.");
        } else {
            for (Map.Entry<Integer, UserRole> entry : roles.entrySet()) {
                try {
                    String houseName = this.model.getHouseById(entry.getKey()).getName();
                    Ansi.listRow(String.format("%-22s %s", houseName, entry.getValue()));
                } catch (HouseNotFoundException e) {
                    Ansi.listRow("House ID " + entry.getKey() + " not found");
                }
            }
        }
        Ansi.listSeparator();
    }

    private void statistics(String email) {
        Menu menu = new Menu("Statistics", new String[]{
                "Most consuming houses",
                "Top 3 devices by active time",
                "Top 3 devices by activations",
                "Top 3 divisions by device count"
        }, model::getCurrentState);

        menu.setHandler(1, () -> {
            try {
                List<House> topHouses = model.getTop3MostConsumingHouses(email);
                if (topHouses.isEmpty()) {
                    System.out.println("  No houses in the system.");
                } else {
                    Ansi.listTitle("Most Consuming Houses");
                    for (int i = 0; i < topHouses.size(); i++) {
                        House h = topHouses.get(i);
                        Ansi.listRow(String.format("%d  %-22s %.2f Wh", i + 1, h.getName(), h.calculateTotalConsumption()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });
        menu.setHandler(2, () -> {
            try {
                List<Device> topDevices = model.getTopDevicesByCriterion(email, 3, Device::getTotalMinutesOn);
                if (topDevices.isEmpty()) {
                    System.out.println("  No devices found for your houses.");
                } else {
                    Ansi.listTitle("Top Devices By Active Time");
                    for (int i = 0; i < topDevices.size(); i++) {
                        Device d = topDevices.get(i);
                        Ansi.listRow(String.format("%d  %-10s %-12s %-10s %d min active",
                            i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalMinutesOn()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });
        menu.setHandler(3, () -> {
            try {
                List<Device> topDevices = model.getTopDevicesByCriterion(email, 3, Device::getTotalActivations);
                if (topDevices.isEmpty()) {
                    System.out.println("  No devices found for your houses.");
                } else {
                    Ansi.listTitle("Top Devices By Activations");
                    for (int i = 0; i < topDevices.size(); i++) {
                        Device d = topDevices.get(i);
                        Ansi.listRow(String.format("%d  %-10s %-12s %-10s %d activation(s)",
                            i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalActivations()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });
        menu.setHandler(4, () -> {
            try {
                List<DivisionInfo> topDivisions = model.getTopDivisionsByCriterion(email, 3, div -> div.house.getDivisions().get(div.divisionName).size());
                if (topDivisions.isEmpty()) {
                    System.out.println("  No divisions found for your houses.");
                } else {
                    Ansi.listTitle("Top Divisions By Device Count");
                    for (int i = 0; i < topDivisions.size(); i++) {
                        DivisionInfo div = topDivisions.get(i);
                        Ansi.listRow(String.format("%d  %-18s %-12s %d device(s)",
                            i + 1, div.divisionName, div.house.getName(), div.devices.size()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });

        menu.run();
    }

    private void advanceSimulation() {
        System.out.print(Ansi.prompt("Minutes to advance"));
        int minutes = readInt();

        if (minutes <= 0) {
            System.out.println("  Must be a positive number.");
            return;
        }

        List<String> activated = model.tick(minutes);

        System.out.println("  Simulation advanced by " + minutes + " minute(s).");

        if (!activated.isEmpty()) {
            Ansi.listTitle("Routines Activated");
            for (String name : activated) {
                Ansi.listRow(" - " + name);
            }
            Ansi.listSeparator();
        }
    }

    private void saveState() {
        System.out.print(Ansi.prompt("File name"));
        String name = sc.nextLine().trim();
        String path = "saves/" + name;
        new File("saves").mkdirs();
        try {
            model.saveState(path);
            System.out.println("  State saved to: " + path);
        } catch (IOException e) {
            System.out.println("  Error saving state: " + e.getMessage());
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