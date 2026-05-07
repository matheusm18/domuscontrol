package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.user.User;
import domuscontrol.utils.Ansi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class DomusControlUI {

    private DomusControl model;
    private final Scanner sc;
    private final UserUI userUI;
    private String currentUserEmail;

    public DomusControlUI() {
        this.model = new DomusControl();
        this.sc = new Scanner(System.in);
        this.userUI = new UserUI(this.model, this.sc);
        this.currentUserEmail = null;
    }

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
        Menu menu = new Menu(new String[]{
                "User Statistics",
                "House Statistics",
                "Device Statistics",
                "Back"
        }, () -> model.getCurrentState());

        menu.setPreCondition(1, () -> !model.getAllUsers().isEmpty());
        menu.setPreCondition(2, () -> !model.getAllHouses().isEmpty());
        menu.setPreCondition(3, () -> !model.getAllDevices().isEmpty());

        menu.setHandler(1, this::doUserStatistics);
        menu.setHandler(2, this::doHouseStatistics);
        menu.setHandler(3, this::doDeviceStatistics);

        menu.run();
    }

    // Menu para escolher ver o user com mais casas, o user com mais dispositivos, o user com mais consumo de energia, etc.
    private void doUserStatistics() {
        Menu menu = new Menu(new String[]{
                "Top 3 Users with most houses",
                "Top 3 Users with most devices",
                "Top 3 Users with most energy consumption",
                "Back"
        }, () -> model.getCurrentState());

        // usa getTopUsersByCriterion passando o critério correspondente, passa lhe o criterio só
        menu.setHandler(1, () -> {
            List<User> topUsers = model.getTopUsersByCriterion(3, u -> u.getHouseIds().size());
            if (topUsers.isEmpty()) {
                System.out.println("  No users in the system.");
            } else {
                Ansi.listTitle("Top 3 Users with Most Houses");
                for (int i = 0; i < topUsers.size(); i++) {
                    User u = topUsers.get(i);
                    Ansi.listRow(String.format("%d  %-22s %d", i + 1, u.getName(), u.getHouseIds().size()));
                }
                Ansi.listSeparator();
            }
        });

        menu.setHandler(2, () -> {
            List<User> topUsers = model.getTopUsersByCriterion(3, u -> {
                try { return model.getHousesByUser(u.getEmail()).stream().mapToInt(h -> h.getDevices().size()).sum(); }
                catch (Exception e) { return 0; }
            });
            if (topUsers.isEmpty()) {
                System.out.println("  No users in the system.");
            } else {
                Ansi.listTitle("Top 3 Users with Most Devices");
                for (int i = 0; i < topUsers.size(); i++) {
                    User u = topUsers.get(i);
                    int devices = 0;
                    try { devices = model.getHousesByUser(u.getEmail()).stream().mapToInt(h -> h.getDevices().size()).sum(); }
                    catch (Exception ignored) {}
                    Ansi.listRow(String.format("%d  %-22s %d", i + 1, u.getName(), devices));
                }
                Ansi.listSeparator();
            }
        });

        menu.setHandler(3, () -> {
            List<User> topUsers = model.getTopUsersByCriterion(3, u -> {
                try { return (int) Math.round(model.getHousesByUser(u.getEmail()).stream().mapToDouble(House::calculateTotalConsumption).sum()); }
                catch (Exception e) { return 0; }
            });
            if (topUsers.isEmpty()) {
                System.out.println("  No users in the system.");
            } else {
                Ansi.listTitle("Top 3 Users with Most Consumption");
                for (int i = 0; i < topUsers.size(); i++) {
                    User u = topUsers.get(i);
                    double consumption = 0;
                    try { consumption = model.getHousesByUser(u.getEmail()).stream().mapToDouble(House::calculateTotalConsumption).sum(); }
                    catch (Exception ignored) {}
                    Ansi.listRow(String.format("%d  %-22s %.2f", i + 1, u.getName(), consumption));
                }
                Ansi.listSeparator();
            }
        });

        menu.run();
    }

    private void doHouseStatistics() {
        // Similar to doUserStatistics but for houses
        Menu menu = new Menu(new String[]{
                "Top 3 Houses with most devices",
                "Top 3 Houses with most energy consumption",
                "Back"
        }, () -> model.getCurrentState());

        menu.setHandler(1, () -> {
            List<House> topHouses = model.getTopHousesByCriterion(3, h -> (double) h.getDevices().size());
            if (topHouses.isEmpty()) {
                System.out.println("  No houses in the system.");
            } else {
                Ansi.listTitle("Top 3 Houses with Most Devices");
                for (int i = 0; i < topHouses.size(); i++) {
                    House h = topHouses.get(i);
                    Ansi.listRow(String.format("%d  %-22s %d", i + 1, h.getName(), h.getDevices().size()));
                }
                Ansi.listSeparator();
            }
        });

        menu.setHandler(2, () -> {
            List<House> topHouses = model.getTopHousesByCriterion(3, House::calculateTotalConsumption);
            if (topHouses.isEmpty()) {
                System.out.println("  No houses in the system.");
            } else {
                Ansi.listTitle("Top 3 Houses with Most Consumption");
                for (int i = 0; i < topHouses.size(); i++) {
                    House h = topHouses.get(i);
                    double consumption = h.calculateTotalConsumption();
                    Ansi.listRow(String.format("%d  %-22s %.2f", i + 1, h.getName(), consumption));
                }
                Ansi.listSeparator();
            }
        });

        menu.run();
    }

    private void doDeviceStatistics() {
        // Similar to doUserStatistics but for devices
        Menu menu = new Menu(new String[]{
                "Top 3 devices by active time",
                "Top 3 devices by activations",
                "Top 3 Devices with most energy consumption",
                "Back"
        }, () -> model.getCurrentState());

        menu.setHandler(1, () -> {
            try {
                List<Device> topDevices = model.getTopDevicesByCriterion(3, d -> (double) d.getTotalMinutesOn());
                if (topDevices.isEmpty()) {
                    System.out.println("  No devices in the system.");
                } else {
                    Ansi.listTitle("Top 3 Devices by Active Time");
                    for (int i = 0; i < topDevices.size(); i++) {
                        Device d = topDevices.get(i);
                        Ansi.listRow(String.format("%d  %-22s %d", i + 1, d.getModel(), d.getTotalMinutesOn()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }});

        menu.setHandler(2, () -> {
            try {                List<Device> topDevices = model.getTopDevicesByCriterion(3, d -> (double) d.getTotalActivations());
                if (topDevices.isEmpty()) {
                    System.out.println("  No devices in the system.");
                } else {
                    Ansi.listTitle("Top 3 Devices by Activations");
                    for (int i = 0; i < topDevices.size(); i++) {
                        Device d = topDevices.get(i);
                        Ansi.listRow(String.format("%d  %-22s %d", i + 1, d.getModel(), d.getTotalActivations()));
                    }
                    Ansi.listSeparator();
                }
            } catch (UserNotFoundException e) {
                System.out.println("  Error: user not found.");
            } catch (HouseNotFoundException e) {
                System.out.println("  Error: house not found.");
            }});

            menu.setHandler(3, () -> {
                try {
                    List<Device> topDevices = model.getTopDevicesByCriterion(3, Device::getEnergyConsumption);
                    if (topDevices.isEmpty()) {
                        System.out.println("  No devices in the system.");
                    } else {
                        Ansi.listTitle("Top 3 Devices by Energy Consumption");
                        for (int i = 0; i < topDevices.size(); i++) {
                            Device d = topDevices.get(i);
                            double consumption = d.getEnergyConsumption();
                            Ansi.listRow(String.format("%d  %-22s %.2f", i + 1, d.getModel(), consumption));
                        }
                        Ansi.listSeparator();
                    }
                } catch (UserNotFoundException e) {
                    System.out.println("  Error: user not found.");
                } catch (HouseNotFoundException e) {
                    System.out.println("  Error: house not found.");
                }});

        menu.run();
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
