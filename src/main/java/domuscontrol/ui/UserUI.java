package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.menu.Menu;
import domuscontrol.model.device.Device;
import domuscontrol.model.houses.House;
import domuscontrol.user.User;

import java.io.IOException;
import java.util.List;
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
                System.out.printf("%d - %s [ID: %d]%n", i + 1, h.getName(), h.getId());
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
            System.out.printf("House '%s' created [ID: %d].%n", house.getName(), house.getId());
        } catch (UserNotFoundException | HouseAlreadyExistsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void myProfile(String email) {
        try {
            User user = model.getUserByEmail(email);
            System.out.println("\n==| Profile |==");
            System.out.println(user.toString());
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

    private void statistics(String email) {
        Menu menu = new Menu(new String[]{
                "Most consuming house (system-wide)",
                "Top 3 devices by time on",
                "Top 3 devices by activations",
                "Top 3 divisions by device count"
        });

        menu.setHandler(1, () -> {
            House h = model.getMostConsumingHouse();
            if (h == null) System.out.println("No houses in the system.");
            else System.out.printf("Most consuming: %s [ID: %d] — %.2f Wh%n",
                    h.getName(), h.getId(), h.calculateTotalConsumption());
        });
        menu.setHandler(2, () -> houseStatDevices(email, false));
        menu.setHandler(3, () -> houseStatDevices(email, true));
        menu.setHandler(4, () -> houseStatDivisions(email));

        menu.run();
    }

    private void houseStatDevices(String email, boolean byActivations) {
        House house = pickHouse(email);
        if (house == null) return;
        List<Device> result = byActivations
                ? house.top3DevicesTurnedOnTimes()
                : house.top3DevicesTimeConsumption();
        if (result.isEmpty()) System.out.println("No devices.");
        else result.forEach(System.out::println);
    }

    private void houseStatDivisions(String email) {
        House house = pickHouse(email);
        if (house == null) return;
        List<String> divs = house.top3DivisionsWithMostDevices();
        if (divs.isEmpty()) System.out.println("No divisions.");
        else divs.forEach(System.out::println);
    }

    private House pickHouse(String email) {
        try {
            List<House> houses = model.getHousesByUser(email);
            if (houses.isEmpty()) { System.out.println("No houses."); return null; }
            System.out.println("\n==| Select House |==");
            for (int i = 0; i < houses.size(); i++) {
                System.out.printf("%d - %s%n", i + 1, houses.get(i).getName());
            }
            System.out.print("Choice: ");
            int choice = readInt();
            if (choice < 1 || choice > houses.size()) return null;
            return houses.get(choice - 1);
        } catch (UserNotFoundException | HouseNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
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