package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.*;
import domuscontrol.houses.DivisionInfo;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.simulation.ActivationEvent;
import domuscontrol.simulation.SimulationState;
import domuscontrol.user.User;
import domuscontrol.user.UserRole;
import domuscontrol.utils.Ansi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * User interface class for user-related operations and dashboard display.
 * Handles user interactions, dashboard navigation, and delegates house-related UI actions.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class UserUI {

    /** The application model facade. */
    private DomusControl model;

    /** Shared scanner for reading user input. */
    private final Scanner sc;

    /** Sub-UI for house-related operations. */
    private final HouseUI houseUI;

    /**
     * Constructs the UserUI with the given model and scanner.
     *
     * @param model the DomusControl model
     * @param sc the Scanner for user input
     */
    public UserUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
        this.houseUI = new HouseUI(model, sc);
    }

    /**
     * Sets the DomusControl model for this UI and updates the house UI model.
     *
     * @param model the DomusControl model
     */
    public void setModel(DomusControl model) {
        this.model = model;
        this.houseUI.setModel(model);
    }

    /**
     * Displays the user dashboard menu for the given user email.
     *
     * @param email the user's email
     */
    public void show(String email) {
        Menu menu = new Menu("Dashboard", new String[]{
                "My Houses",
                "Create House",
                "My Profile",
                "Statistics",
                "Advance Simulation",
                "Save State"
        }, () -> stateHeader(model.getCurrentState()));

        menu.setPreCondition(1, () -> hasHouses(email));
        menu.setPreCondition(4, () -> hasHouses(email));

        menu.setHandler(1, () -> myHouses(email));
        menu.setHandler(2, () -> createHouse(email));
        menu.setHandler(3, () -> myProfile(email));
        menu.setHandler(4, () -> statistics(email));
        menu.setHandler(5, () -> advanceSimulation(email));
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
            Ansi.error("Error: user not found.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: one of your houses was not found.");
        }
    }

    private void createHouse(String email) {
        System.out.print(Ansi.prompt("House name"));
        String name = sc.nextLine();
        try {
            House house = model.createHouse(email, name);
            System.out.printf("  House '%s' created.%n", house.getName());
        } catch (UserNotFoundException e) {
            Ansi.error("Error: user not found.");
        } catch (HouseAlreadyExistsException | UserAlreadyExistsException e) {
            Ansi.error("Error: conflict creating house.");
        }
    }

    private void myProfile(String email) {
        try {
            User user = model.getUserByEmail(email);
            System.out.println();
            showUserDetails(user);
        } catch (UserNotFoundException e) {
            Ansi.error("Error: user not found.");
            return;
        }

        Menu menu = new Menu("Profile", new String[]{"Change Name", "Change Password"}, () -> stateHeader(model.getCurrentState()));
        menu.setHandler(1, () -> {
            System.out.print(Ansi.prompt("New name"));
            String name = sc.nextLine();
            try {
                model.updateUserName(email, name);
                System.out.println("  Name updated.");
            } catch (UserNotFoundException | UserAlreadyExistsException ex) {
                Ansi.error("Error: " + ex.getMessage());
            }
        });
        menu.setHandler(2, () -> {
            System.out.print(Ansi.prompt("New password"));
            String pass = sc.nextLine();
            try {
                model.updateUserPassword(email, pass);
                System.out.println("  Password updated.");
            } catch (UserNotFoundException | UserAlreadyExistsException ex) {
                Ansi.error("Error: " + ex.getMessage());
            }
        });
        menu.run();
    }

    /**
     * Displays detailed information about a user.
     *
     * @param user The user whose details should be displayed.
     */
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
                "Top 3 most consuming houses",
                "Top 3 devices by active time",
                "Top 3 devices by activations",
                "Top 3 divisions by device count"
        }, () -> stateHeader(model.getCurrentState()));

        menu.setHandler(1, () -> {
            try {
                List<House> topHouses = model.getTopHousesByCriterionForUser(email, 3, House::calculateTotalConsumption);
                if (topHouses.isEmpty()) {
                    System.out.println("  No houses in the system.");
                } else {
                    Ansi.listTitle("Most Consuming Houses");
                    int hw = topHouses.stream().mapToInt(h -> h.getName().length()).max().orElse(10) + 2;
                    for (int i = 0; i < topHouses.size(); i++) {
                        House h = topHouses.get(i);
                        Ansi.listRow(String.format("%d  %-" + hw + "s %.2f Wh", i + 1, h.getName(), h.calculateTotalConsumption()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException | HouseNotFoundException e) {
                Ansi.error("Error: " + e.getMessage());
            }
        });
        menu.setHandler(2, () -> {
            House house = selectHouse(email);
            if (house == null) return;
            try {
                List<Device> topDevices = model.getTopDevicesInHouse(house.getId(), 3, d -> (double) d.getTotalMinutesOn());
                if (topDevices.isEmpty()) { System.out.println("  No devices in this house."); return; }
                Ansi.listTitle("Top Devices By Active Time - " + house.getName());
                int[] w2 = deviceColWidths(topDevices);
                for (int i = 0; i < topDevices.size(); i++) {
                    Device d = topDevices.get(i);
                    Ansi.listRow(String.format("%d  %-" + w2[0] + "s %-" + w2[1] + "s %-" + w2[2] + "s %d min active",
                        i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalMinutesOn()));
                }
                Ansi.listSeparator();
            } catch (HouseNotFoundException e) {
                Ansi.error("Error: house not found.");
            }
        });
        menu.setHandler(3, () -> {
            House house = selectHouse(email);
            if (house == null) return;
            try {
                List<Device> topDevices = model.getTopDevicesInHouse(house.getId(), 3, d -> (double) d.getTotalActivations());
                if (topDevices.isEmpty()) { System.out.println("  No devices in this house."); return; }
                Ansi.listTitle("Top Devices By Activations - " + house.getName());
                int[] w3 = deviceColWidths(topDevices);
                for (int i = 0; i < topDevices.size(); i++) {
                    Device d = topDevices.get(i);
                    Ansi.listRow(String.format("%d  %-" + w3[0] + "s %-" + w3[1] + "s %-" + w3[2] + "s %d activation(s)",
                        i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalActivations()));
                }
                Ansi.listSeparator();
            } catch (HouseNotFoundException e) {
                Ansi.error("Error: house not found.");
            }
        });
        menu.setHandler(4, () -> {
            try {
                List<DivisionInfo> topDivisions = model.getTopDivisionsByCriterionForUser(email, 3, div -> (double) div.getDeviceCount());
                if (topDivisions.isEmpty()) {
                    System.out.println("  No divisions found for your houses.");
                } else {
                    Ansi.listTitle("Top Divisions By Device Count");
                    int dw = topDivisions.stream().mapToInt(d -> d.getDivisionName().length()).max().orElse(10) + 2;
                    int hlw = topDivisions.stream()
                        .mapToInt(d -> (d.getHouseName() + " (#" + d.getHouseId() + ")").length())
                        .max().orElse(10) + 2;
                    for (int i = 0; i < topDivisions.size(); i++) {
                        DivisionInfo div = topDivisions.get(i);
                        String houseLabel = div.getHouseName() + " (#" + div.getHouseId() + ")";
                        Ansi.listRow(String.format("%d  %-" + dw + "s %-" + hlw + "s %d device(s)",
                            i + 1, div.getDivisionName(), houseLabel, div.getDeviceCount()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException | HouseNotFoundException e) {
                Ansi.error("Error: " + e.getMessage());
            }
        });

        menu.run();
    }

    private int[] deviceColWidths(List<Device> devices) {
        int type  = devices.stream().mapToInt(d -> d.getClass().getSimpleName().length()).max().orElse(10);
        int brand = devices.stream().mapToInt(d -> d.getBrand().length()).max().orElse(8);
        int model = devices.stream().mapToInt(d -> d.getModel().length()).max().orElse(10);
        return new int[]{type + 2, brand + 2, model + 2};
    }

    private House selectHouse(String email) {
        try {
            List<House> houses = model.getHousesByUser(email);
            if (houses.isEmpty()) {
                System.out.println("  You have no houses.");
                return null;
            }
            Ansi.listTitle("Your Houses");
            for (int i = 0; i < houses.size(); i++)
                Ansi.listRow(String.format("%d  %s", i + 1, houses.get(i).getName()));
            Ansi.listSeparator();
            System.out.print(Ansi.prompt("Select house (0 to cancel)"));
            int choice = readInt();
            if (choice < 1 || choice > houses.size()) return null;
            return houses.get(choice - 1);
        } catch (UserNotFoundException | HouseNotFoundException e) {
            Ansi.error("Error: " + e.getMessage());
            return null;
        }
    }

    private void advanceSimulation(String email) {
        System.out.print(Ansi.prompt("Minutes to advance"));
        int minutes = readInt();

        if (minutes <= 0) {
            System.out.println("  Must be a positive number.");
            return;
        }
        if (minutes > 525600) {
            System.out.println("  Maximum is 525600 minutes (1 year).");
            return;
        }

        List<ActivationEvent> activated;
        try {
            activated = model.tick(minutes);
        } catch (InvalidMinutesException e) {
            Ansi.error("Invalid number of minutes.");
            return;
        }
        System.out.println("  Simulation advanced by " + minutes + " minute(s).");

        try {
            List<Integer> myHouseIds = model.getHousesByUser(email).stream()
                .map(House::getId)
                .toList();
            List<ActivationEvent> mine = activated.stream()
                .filter(event -> myHouseIds.contains(event.getHouseId()))
                .toList();
            if (!mine.isEmpty()) {
                Ansi.listTitle("Routines Activated");
                for (ActivationEvent event : mine) {
                    Ansi.listRow(" - " + event.getHouseName() + ": " + event.getRoutineName());
                }
                Ansi.listSeparator();
            }
        } catch (UserNotFoundException e) {
            Ansi.error("Error: user not found.");
        } catch (HouseNotFoundException e) {
            Ansi.error("Error: house not found.");
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
            Ansi.error("Error saving state: " + e.getMessage());
        }
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print(Ansi.prompt("Invalid input. Please enter an integer"));
            }
        }
    }

    private static String stateHeader(SimulationState s) {
        return s.getCurrentDateTime().toLocalDate() + "  " + s.getCurrentDateTime().toLocalTime() + "\n" +
               String.format("%.1fºC  %s  %.0f lx", s.getTemperature(), s.getWeather(), s.getLuminosity());
    }
}
