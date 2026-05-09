package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.houses.DivisionInfo;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.utils.Ansi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main user interface class for the DomusControl application.
 * Handles application startup, user authentication, and main menu navigation.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DomusControlUI {

    private DomusControl model;
    private final Scanner sc;
    private final UserUI userUI;
    private String currentUserEmail;

    /**
     * Constructor for DomusControlUI. Initializes the model, scanner, user UI, and current user email.
     */
    public DomusControlUI() {
        this.model = new DomusControl();
        this.sc = new Scanner(System.in);
        this.userUI = new UserUI(this.model, this.sc);
        this.currentUserEmail = null;
    }

    /**
     * Runs the main application loop.
     */
    public void run() {
        printWelcome();
        Menu menu = new Menu(new String[]{
                "Login",
                "Register",
                "Global Statistics",
                "Load State"
        }, () -> model.getCurrentState());

        menu.setHandler(1, this::doLogin);
        menu.setHandler(2, this::doRegister);
        menu.setHandler(3, this::doGlobalStatistics);
        menu.setHandler(4, this::doLoadState);

        menu.run();
    }

    private void printWelcome() {
        int w = Ansi.WIDTH;
        String horiz = "═".repeat(w);
        String blank = Ansi.CYAN + " ║" + " ".repeat(w) + "║" + Ansi.RESET;
        String line1 = centeredRow("D O M U S  C O N T R O L", w, true);
        String line2 = centeredRow("Smart Home Automation", w, false);
        System.out.println();
        System.out.println(Ansi.CYAN + " ╔" + horiz + "╗" + Ansi.RESET);
        System.out.println(blank);
        System.out.println(line1);
        System.out.println(line2);
        System.out.println(blank);
        System.out.println(Ansi.CYAN + " ╚" + horiz + "╝" + Ansi.RESET);
        System.out.println();
    }

    private String centeredRow(String text, int width, boolean bold) {
        int pad = Math.max(0, width - text.length());
        int left = pad / 2;
        int right = pad - left;
        String styled = bold
            ? Ansi.BOLD + Ansi.WHITE + text + Ansi.RESET
            : Ansi.DIM + text + Ansi.RESET;
        return Ansi.CYAN + " ║" + Ansi.RESET + " ".repeat(left) + styled + " ".repeat(right) + Ansi.CYAN + "║" + Ansi.RESET;
    }

    private void doLogin() {
        System.out.print(Ansi.prompt("Email"));
        String user = sc.nextLine();
        System.out.print(Ansi.prompt("Password"));
        String password = sc.nextLine();
        try {
            this.currentUserEmail = model.validateLogin(user, password).getEmail();
            userUI.show(this.currentUserEmail);
        } catch (UserNotFoundException e) {
            System.out.println("  No account found with " + user + ".");
        } catch (LoginInvalidPasswordException e) {
            System.out.println("  Incorrect password.");
        }
    }

    private void doRegister() {
        System.out.print(Ansi.prompt("Name"));
        String name = sc.nextLine();
        System.out.print(Ansi.prompt("Email"));
        String email = sc.nextLine();
        System.out.print(Ansi.prompt("Password"));
        String password = sc.nextLine();
        try {
            model.registerUser(name, email, password);
            System.out.println("  Registered successfully.");
            this.currentUserEmail = email;
            userUI.show(this.currentUserEmail);
        } catch (UserAlreadyExistsException e) {
            System.out.println("  Email already registered.");
        }
    }

    private void doGlobalStatistics() {
        Menu menu = new Menu("Statistics", new String[]{
                "Top 3 most consuming houses",
                "Top 3 devices by active time",
                "Top 3 devices by activations",
                "Top 3 divisions by device count"
        }, () -> model.getCurrentState());

        menu.setPreCondition(1, () -> !model.getAllHouses().isEmpty());
        menu.setPreCondition(2, () -> !model.getAllHouses().isEmpty());
        menu.setPreCondition(3, () -> !model.getAllHouses().isEmpty());
        menu.setPreCondition(4, () -> !model.getAllHouses().isEmpty());

        menu.setHandler(1, () -> {
            List<House> top = model.getTopHousesByCriterion(3, h -> h.calculateTotalConsumption());
            if (top.isEmpty()) { System.out.println("  No houses in the system."); return; }
            Ansi.listTitle("Most Consuming Houses");
            for (int i = 0; i < top.size(); i++) {
                House h = top.get(i);
                Ansi.listRow(String.format("%d  %-22s %.2f Wh", i + 1, h.getName(), h.calculateTotalConsumption()));
            }
            Ansi.listSeparator();
        });

        menu.setHandler(2, () -> {
            House house = selectHouseGlobal();
            if (house == null) return;
            try {
                List<Device> top = model.getTopDevicesInHouse(house.getId(), 3, d -> (double) d.getTotalMinutesOn());
                if (top.isEmpty()) { System.out.println("  No devices in this house."); return; }
                Ansi.listTitle("Top Devices By Active Time - " + house.getName());
                int[] w = deviceColWidths(top);
                for (int i = 0; i < top.size(); i++) {
                    Device d = top.get(i);
                    Ansi.listRow(String.format("%d  %-" + w[0] + "s %-" + w[1] + "s %-" + w[2] + "s %d min active",
                        i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalMinutesOn()));
                }
                Ansi.listSeparator();
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });

        menu.setHandler(3, () -> {
            House house = selectHouseGlobal();
            if (house == null) return;
            try {
                List<Device> top = model.getTopDevicesInHouse(house.getId(), 3, d -> (double) d.getTotalActivations());
                if (top.isEmpty()) { System.out.println("  No devices in this house."); return; }
                Ansi.listTitle("Top Devices By Activations - " + house.getName());
                int[] w = deviceColWidths(top);
                for (int i = 0; i < top.size(); i++) {
                    Device d = top.get(i);
                    Ansi.listRow(String.format("%d  %-" + w[0] + "s %-" + w[1] + "s %-" + w[2] + "s %d activation(s)",
                        i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel(), d.getTotalActivations()));
                }
                Ansi.listSeparator();
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }
        });

        menu.setHandler(4, () -> {
            List<DivisionInfo> top = model.getTopDivisionsByCriterion(3, di -> (double) di.getDeviceCount());
            if (top.isEmpty()) { System.out.println("  No divisions in the system."); return; }
            Ansi.listTitle("Top Divisions By Device Count");
            for (int i = 0; i < top.size(); i++) {
                DivisionInfo di = top.get(i);
                Ansi.listRow(String.format("%d  %-18s %-18s %d device(s)",
                    i + 1, di.getDivisionName(), di.getHouseName(), di.getDeviceCount()));
            }
            Ansi.listSeparator();
        });

        menu.run();
    }

    private House selectHouseGlobal() {
        List<House> houses = model.getAllHouses();
        if (houses.isEmpty()) { System.out.println("  No houses in the system."); return null; }
        Ansi.listTitle("All Houses");
        for (int i = 0; i < houses.size(); i++)
            Ansi.listRow(String.format("%d  %s", i + 1, houses.get(i).getName()));
        Ansi.listSeparator();
        while (true) {
            System.out.print(Ansi.prompt("Select house (0 to cancel)"));
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice == 0) return null;
                if (choice >= 1 && choice <= houses.size()) return houses.get(choice - 1);
            } catch (NumberFormatException ignored) {}
            System.out.println("  Invalid selection.");
        }
    }

    private int[] deviceColWidths(List<Device> devices) {
        int type  = devices.stream().mapToInt(d -> d.getClass().getSimpleName().length()).max().orElse(10);
        int brand = devices.stream().mapToInt(d -> d.getBrand().length()).max().orElse(8);
        int model = devices.stream().mapToInt(d -> d.getModel().length()).max().orElse(10);
        return new int[]{type + 2, brand + 2, model + 2};
    }

    private void doLoadState() {
        System.out.print(Ansi.prompt("File name"));
        String path = "saves/" + sc.nextLine().trim();
        try {
            this.model = DomusControl.loadState(path);
            this.userUI.setModel(this.model);
            System.out.println("  State loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("  File not found: " + path);
        } catch (IOException e) {
            System.out.println("  Error loading state: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("  Error loading state: corrupted file.");
        }
    }
}
