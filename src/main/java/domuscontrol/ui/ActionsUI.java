package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.devices.Device;
import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.*;
import domuscontrol.houses.House;
import domuscontrol.menu.Menu;
import domuscontrol.routines.*;
import domuscontrol.routines.actions.*;
import domuscontrol.routines.conditions.*;
import domuscontrol.simulation.Simulation;
import domuscontrol.user.User;
import domuscontrol.utils.Ansi;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ActionsUI {

    private DomusControl model;
    private final Scanner sc;

    public ActionsUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
    }

    public void setModel(DomusControl model) {
        this.model = model;
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

    // ------------------------------------------------------------------------
    // AUTOMATIONS (may have any conditions: time, climate, temperature)
    // ------------------------------------------------------------------------

    public void manageAutomations(int houseId, String email) {
        Menu menu = new Menu("Automations", new String[] {
                "List Automations",
                "Add Automation",
                "Remove Automation"
        }, model::getCurrentState);

        menu.setPreCondition(1, () -> hasAutomations(houseId));
        menu.setPreCondition(2, () -> houseHasDevices(houseId));
        menu.setPreCondition(3, () -> hasAutomations(houseId));
        
        menu.setHandler(1, () -> listAutomations(houseId));
        menu.setHandler(2, () -> addAutomation(houseId));
        menu.setHandler(3, () -> removeAutomation(houseId));
        menu.run();
    }

    private void listAutomations(int houseId) {
        try {
            List<Automation> automations = model.getAutomations(houseId);
            if (automations.isEmpty()) {
                System.out.println("  No automations.");
                return;
            }
            Ansi.listTitle("Automations");
            for (int i = 0; i < automations.size(); i++) {
                Ansi.listRow(String.format("%d  %-22s", i + 1, automations.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Automation (0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Automation selected = automations.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                showAutomationInfo(selected);
            } else {
                System.out.println("  Invalid Automation Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void showAutomationInfo(Automation automation) {
        Ansi.listTitle(automation.getType().toString());
        Ansi.listRow(String.format("%-12s %s", "Name", automation.getName()));
        Ansi.listRow(String.format("%-12s %s", "Type", automation.getType()));
        Ansi.listRow(String.format("%-12s %d", "Conditions", automation.getConditions().size()));
        Ansi.listRow(String.format("%-12s %d", "Actions", automation.getActions().size()));
        Ansi.listSeparator();
    }

    private void showScenarioInfo(Scenario scenario) {
        Ansi.listTitle("Scenario");
        Ansi.listRow(String.format("%-12s %s", "Name", scenario.getName()));
        Ansi.listRow(String.format("%-12s %d", "Actions", scenario.getActions().size()));
        Ansi.listSeparator();
    }

    private void addAutomation(int houseId) {
        System.out.print(Ansi.prompt("Automation name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("\n  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            List<Action> actions = readActionsDialog(house);

            Ansi.listTitle("Selected Actions");
            for (Action a : actions) {
                Ansi.listRow(" - " + a);
            }
            Ansi.listSeparator();

            List<Condition> conditions = readConditionsDialog(house, false);

            Ansi.listTitle("Selected Conditions");
            for (Condition c : conditions) {
                Ansi.listRow(" - " + c);
            }
            Ansi.listSeparator();

            Automation automation = new Automation(name, AutomationType.AUTOMATION, conditions, actions);
            model.addAutomation(houseId, automation);
            System.out.println("  Automation '" + name + "' added.");

            for (Condition c : conditions) {
                if (c instanceof TimeWindowCondition twc) {
                    List<Condition> endConditions = new ArrayList<>();
                    endConditions.add(new TimeCondition(twc.getEndTime()));
                    for (Condition other : conditions) {
                        if (other != c)
                            endConditions.add(other.copy());
                    }
                    Automation endAutomation = new Automation(name + " [END]", AutomationType.SCHEDULE, endConditions,
                            actions);
                    model.addAutomation(houseId, endAutomation);
                    break;
                }
            }

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (NameAlreadyExistsException e) {
            System.out.println("  Error: routine name already exists.");
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: schedule conditions must be time-based.");
        }
    }

    private void removeAutomation(int houseId) {
        try {
            List<Automation> automations = model.getAutomations(houseId);
            
            if (automations.isEmpty()) {
                System.out.println("  No automations to remove.");
                return;
            }

            Ansi.listTitle("Select Automation to Remove");
            for (int i = 0; i < automations.size(); i++) {
                Ansi.listRow(String.format("%d  %s", i + 1, automations.get(i).getName()));
            }
            Ansi.listSeparator();
            
            System.out.print(Ansi.prompt("Automation (0 to cancel)"));
            int choice = readInt();
            
            if (choice < 1 || choice > automations.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = automations.get(choice - 1).getName();
            model.removeAutomation(houseId, name);
            System.out.println("  Automation '" + name + "' removed.");
            
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (AutomationDoesntExistException e) {
            System.out.println("  Error: automation not found.");
        }
    }

    // ------------------------------------------------------------------------
    // SCHEDULES (only time‑based conditions allowed)
    // ------------------------------------------------------------------------

    public void manageSchedules(int houseId, String email) {
        Menu menu = new Menu("Schedules", new String[] {
                "List Schedules",
                "Add Schedule",
                "Remove Schedule"
        }, model::getCurrentState);

        menu.setPreCondition(1, () -> hasSchedules(houseId));
        menu.setPreCondition(2, () -> houseHasDevices(houseId));
        menu.setPreCondition(3, () -> hasSchedules(houseId));

        menu.setHandler(1, () -> listSchedules(houseId));
        menu.setHandler(2, () -> addSchedule(houseId));
        menu.setHandler(3, () -> removeSchedule(houseId));
        menu.run();
    }

    private void listSchedules(int houseId) {
        try {
            List<Automation> schedules = model.getSchedules(houseId);
            if (schedules.isEmpty()) {
                System.out.println("  No schedules.");
                return;
            }
            Ansi.listTitle("Schedules");
            for (int i = 0; i < schedules.size(); i++) {
                Ansi.listRow(String.format("%d  %-22s", i + 1, schedules.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Schedule (0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Automation selected = schedules.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                showAutomationInfo(selected);
            } else {
                System.out.println("  Invalid Schedule Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void addSchedule(int houseId) {
        System.out.print(Ansi.prompt("Schedule name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("\n  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            List<Action> actions = readActionsDialog(house);
            List<Condition> conditions = readConditionsDialog(house, true);

            Automation schedule = new Automation(name, AutomationType.SCHEDULE, conditions, actions);
            model.addAutomation(houseId, schedule);
            System.out.println("  Schedule '" + name + "' added.");

            for (Condition c : conditions) {
                if (c instanceof TimeWindowCondition twc) {
                    List<Condition> endConditions = new ArrayList<>();
                    endConditions.add(new TimeCondition(twc.getEndTime()));
                    for (Condition other : conditions) {
                        if (other != c)
                            endConditions.add(other.copy());
                    }
                    Automation endAutomation = new Automation(name + " [END]", AutomationType.SCHEDULE, endConditions,
                            actions);
                    model.addAutomation(houseId, endAutomation);
                    break;
                }
            }

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (NameAlreadyExistsException e) {
            System.out.println("  Error: schedule name already exists.");
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: schedules only accept time conditions.");
        }
    }

    private void removeSchedule(int houseId) {
        try {
            List<Automation> schedules = model.getSchedules(houseId);

            if (schedules.isEmpty()) {
                System.out.println("  No schedules to remove.");
                return;
            }

            Ansi.listTitle("Select Schedule to Remove");
            for (int i = 0; i < schedules.size(); i++) {
                Ansi.listRow(String.format("%d  %s", i + 1, schedules.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Schedule (0 to cancel)"));
            int choice = readInt();

            if (choice < 1 || choice > schedules.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = schedules.get(choice - 1).getName();
            model.removeAutomation(houseId, name);
            System.out.println("  Schedule '" + name + "' removed.");

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (AutomationDoesntExistException e) {
            System.out.println("  Error: schedule not found.");
        }
    }

    // ------------------------------------------------------------------------
    // SCENARIOS (user‑specific, no conditions)
    // ------------------------------------------------------------------------

    public void manageScenarios(int houseId, String email) {
        Menu menu = new Menu("Scenarios", new String[] {
                "List Scenarios",
                "Execute Scenario",
                "Add Scenario",
                "Remove Scenario"
        }, model::getCurrentState);
        
        if (!houseHasDevices(houseId)) {
            System.out.println("  No devices in this house.");
            return;
        }
        Integer userId;
        try {
            userId = model.getUserByEmail(email).getId();
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
            return;
        }

        menu.setPreCondition(1, () -> hasScenarios(houseId, userId));
        menu.setPreCondition(2, () -> hasScenarios(houseId, userId));
        menu.setPreCondition(3, () -> true);
        menu.setPreCondition(4, () -> hasScenarios(houseId, userId));

        menu.setHandler(1, () -> listScenarios(houseId, email));
        menu.setHandler(2, () -> executeScenario(houseId, email));
        menu.setHandler(3, () -> addScenario(houseId, email));
        menu.setHandler(4, () -> removeScenario(houseId, email));
        menu.run();
    }

    private boolean hasScenarios(int houseId, int userId) {
        try {
            return !model.getScenarios(houseId, userId).isEmpty();
        } catch (HouseNotFoundException | UserDoesntHaveScenarios e) {
            return false;
        }
    }

    private boolean hasAutomations(int houseId) {
        try {
            return !model.getAutomations(houseId).isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private boolean hasSchedules(int houseId) {
        try {
            return !model.getSchedules(houseId).isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private boolean houseHasDevices(int houseId) {
        try {
            return !model.getHouseById(houseId).getDevices().isEmpty();
        } catch (HouseNotFoundException e) {
            return false;
        }
    }

    private void listScenarios(int houseId, String email) {
        try {
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = model.getScenarios(houseId, user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios.");
                return;
            }
            Ansi.listTitle("Scenarios");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d  %-22s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Scenario (0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Scenario selected = scenarios.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                showScenarioInfo(selected);
            } else {
                System.out.println("  Invalid Scenario Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (UserDoesntHaveScenarios e) {
            System.out.println("  No scenarios found for this user.");
        }
    }

    private void executeScenario(int houseId, String email) {
        try {
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = model.getScenarios(houseId, user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios.");
                return;
            }
            Ansi.listTitle("Scenarios");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d  %-22s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Scenario (0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Scenario selected = scenarios.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                model.executeScenario(houseId, user.getId(), selected.getName());
                System.out.println("  Scenario '" + selected.getName() + "' executed.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (UserDoesntHaveScenarios e) {
            System.out.println("  Error: scenario not found.");
        } catch (ScenarioDoesntExistException e) {
            System.out.println("  Error: Scenario not found.");
        }
    }

    private void addScenario(int houseId, String email) {
        System.out.print(Ansi.prompt("Scenario name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("\n  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            User user = model.getUserByEmail(email);
            List<Action> actions = readActionsDialog(house);
            Scenario scenario = new Scenario(name, actions);
            model.addScenario(houseId, user.getId(), scenario);
            System.out.println("  Scenario '" + name + "' added.");
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (NameAlreadyExistsException e) {
            System.out.println("  Error: scenario name already exists.");
        }
    }

    private void removeScenario(int houseId, String email) {
        try {
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = model.getScenarios(houseId, user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios to remove.");
                return;
            }

            Ansi.listTitle("Select Scenario to Remove");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d  %s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Scenario (0 to cancel)"));
            int choice = readInt();

            if (choice < 1 || choice > scenarios.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = scenarios.get(choice - 1).getName();
            model.removeScenario(houseId, user.getId(), name);
            System.out.println("  Scenario '" + name + "' removed.");

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (UserNotFoundException e) {
            System.out.println("  Error: user not found.");
        } catch (UserDoesntHaveScenarios e) {
            System.out.println("  No scenarios found for this user.");
        } catch (ScenarioDoesntExistException e) {
            System.out.println("  Error: scenario not found.");
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods for Actions & Conditions
    // ------------------------------------------------------------------------

    private List<Action> readActionsDialog(House house) {
        List<Action> actions = new ArrayList<>();
        boolean adding = true;

        while (adding) {
            Ansi.listTitle("Available Devices");
            int num = 1;
            List<Device> deviceList = new ArrayList<>(house.getDevices().values());

            for (Device d : deviceList) {
                Ansi.listRow(String.format("%d  %-10s %-12s %s",
                    num++, d.getClass().getSimpleName(), d.getBrand(), d.getModel()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Device (0 to finish)"));
            int choice = readInt();

            if (choice == 0) break;
            if (choice < 1 || choice > deviceList.size()) {
                System.out.println(Ansi.DIM + "  Invalid selection." + Ansi.RESET);
                continue;
            }

            Device device = deviceList.get(choice - 1);
            handleDeviceActionSelection(device, actions);
        }
        return actions;
    }

    private void handleDeviceActionSelection(Device device, List<Action> actions) {
        Menu typeMenu = new Menu("Action: " + device.getModel(), 
            new String[] { "Turn On", "Turn Off", "Set Level", "Set Opening", "Set Color Temperature" },
            model::getCurrentState);

        typeMenu.setPreCondition(1, () -> device instanceof SwitchableDevice);
        typeMenu.setPreCondition(2, () -> device instanceof SwitchableDevice);
        typeMenu.setPreCondition(3, () -> device instanceof AdjustableDevice);
        typeMenu.setPreCondition(4, () -> device instanceof OpenableDevice);
        typeMenu.setPreCondition(5, () -> device instanceof ColorAdjustableDevice);

        typeMenu.setHandler(1, () -> {
            actions.add(new TurnOnAction(device.getId()));
            System.out.println("  Action added.");
            typeMenu.stop();
        });
        typeMenu.setHandler(2, () -> {
            actions.add(new TurnOffAction(device.getId()));
            System.out.println("  Action added.");
            typeMenu.stop();
        });
        typeMenu.setHandler(3, () -> {
            System.out.print(Ansi.prompt("Level (0-100)"));
            int level = readIntInRange(0, 100, "Level (0-100)");
            actions.add(new SetLevelAction(device.getId(), level));
            System.out.println("  Action added.");
            typeMenu.stop();
        });
        typeMenu.setHandler(4, () -> {
            System.out.print(Ansi.prompt("Opening percentage (0-100)"));
            int opening = readIntInRange(0, 100, "Opening percentage (0-100)");
            actions.add(new SetOpeningAction(device.getId(), opening));
            System.out.println("  Action added.");
            typeMenu.stop();
        });
        typeMenu.setHandler(5, () -> {
            System.out.print(Ansi.prompt("Color temperature (2700-4000K)"));
            int temperature = readIntInRange(2700, 4000, "Color temperature (2700-4000K)");
            actions.add(new SetColorTemperatureAction(device.getId(), temperature));
            System.out.println("  Action added.");
            typeMenu.stop();
        });

        typeMenu.run();
    }

    /**
     * @param house    the house (for device lookups)
     * @param timeOnly if true, only TimeCondition / TimeWindowCondition can be
     *                 added (for schedules)
     */
    private List<Condition> readConditionsDialog(House house, boolean timeOnly) {
        List<Condition> conditions = new ArrayList<>();
        boolean[] hasTime = { false };
        boolean[] adding = { true };

        if (timeOnly) {
            Ansi.listTitle("Schedule Info");
            Ansi.listRow(Ansi.YELLOW + "Schedules only support time-based triggers." + Ansi.RESET);
            Ansi.listSeparator();
        }

        while (adding[0]) {
            String[] options = {
                "Time Condition",
                "Device Condition",
                "Temperature Condition",
                "Weather Condition"
            };

            Menu menu = new Menu("Add Condition", options, model::getCurrentState);
            menu.setExitLabel("Done");

            menu.setPreCondition(1, () -> !hasTime[0]);
            menu.setPreCondition(2, () -> !timeOnly);
            menu.setPreCondition(3, () -> !timeOnly);
            menu.setPreCondition(4, () -> !timeOnly);

            menu.setHandler(1, () -> handleAddTimeCondition(conditions, hasTime));
            menu.setHandler(2, () -> handleAddDeviceCondition(conditions, house));
            menu.setHandler(3, () -> handleAddTemperatureCondition(conditions));
            menu.setHandler(4, () -> handleAddWeatherCondition(conditions));

            menu.run();
            adding[0] = false;
        }
        return conditions;
    }

    private void handleAddTimeCondition(List<Condition> conditions, boolean[] hasTime) {
        if (hasTime[0]) {
            System.out.println("  Only one time condition allowed per routine.");
            return;
        }

        Menu timeMenu = new Menu("Time Condition Type", new String[] {
                "Single Time", "Time Window"
        }, model::getCurrentState);

        timeMenu.setHandler(1, () -> {
            try {
                System.out.print(Ansi.prompt("Trigger time (HH:MM)"));
                LocalTime trigger = LocalTime.parse(sc.nextLine().trim());
                conditions.add(new TimeCondition(trigger));
                System.out.println("  Time condition added.");
                hasTime[0] = true;
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time format.");
            }
            timeMenu.stop();
        });

        timeMenu.setHandler(2, () -> {
            try {
                System.out.print(Ansi.prompt("Start time (HH:MM)"));
                LocalTime start = LocalTime.parse(sc.nextLine().trim());
                System.out.print(Ansi.prompt("End time (HH:MM)"));
                LocalTime end = LocalTime.parse(sc.nextLine().trim());
                conditions.add(new TimeWindowCondition(start, end));
                System.out.println("  Time window condition added.");
                hasTime[0] = true;
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time format.");
            }
            timeMenu.stop();
        });

        timeMenu.run();
    }

    private void handleAddDeviceCondition(List<Condition> conditions, House house) {
        Menu climateMenu = new Menu("Device Condition Type", new String[] {
                "Device ON/OFF State", "Device Level"
        }, model::getCurrentState);

        climateMenu.setHandler(1, () -> {
            System.out.print(Ansi.prompt("Device ID"));
            int devId = readInt();
            try {
                Device dev = house.getDevice(devId);
                if (dev instanceof SwitchableDevice) {
                    System.out.print(Ansi.prompt("Trigger when ON? (true/false)"));
                    boolean on = Boolean.parseBoolean(sc.nextLine().trim());
                    conditions.add(new DeviceStateCondition(devId, on));
                    System.out.println("  Device condition added.");
                } else {
                    System.out.println("  Incompatible device.");
                }
            } catch (DeviceNotFoundException e) {
                System.out.println("  Device not found.");
            }
            climateMenu.stop();
        });

        climateMenu.setHandler(2, () -> {
            System.out.print(Ansi.prompt("Device ID"));
            int devId = readInt();
            try {
                Device dev = house.getDevice(devId);
                if (dev instanceof AdjustableDevice) {
                    System.out.print(Ansi.prompt("Trigger level"));
                    int lvl = readInt();
                    conditions.add(new DeviceLevelCondition(devId, lvl, Operator.EQUALS));
                    System.out.println("  Device condition added.");
                } else {
                    System.out.println("  Incompatible device.");
                }
            } catch (DeviceNotFoundException e) {
                System.out.println("  Device not found.");
            }
            climateMenu.stop();
        });

        climateMenu.run();
    }

    private void handleAddWeatherCondition(List<Condition> conditions) {
        Simulation.WeatherCondition[] weathers = Simulation.WeatherCondition.values();
        String[] weatherNames = new String[weathers.length];
        for (int i = 0; i < weathers.length; i++) weatherNames[i] = weathers[i].name();

        Menu weatherMenu = new Menu("Select Weather", weatherNames, model::getCurrentState);

        for (int i = 0; i < weathers.length; i++) {
            final int index = i;
            weatherMenu.setHandler(i + 1, () -> {
                conditions.add(new OutsideWeatherCondition(weathers[index]));
                System.out.println("  Weather condition added.");
                weatherMenu.stop();
            });
        }
        weatherMenu.run();
    }

    private void handleAddTemperatureCondition(List<Condition> conditions) {
        System.out.print(Ansi.prompt("Target Temperature (ºC)"));
        int temp = readInt();

        Operator[] op = new Operator[1];
        Menu opMenu = new Menu("Select Operator", new String[] { "EQUALS (==)", "GREATER THAN (>)", "LESS THAN (<)" }, model::getCurrentState);

        opMenu.setHandler(1, () -> { op[0] = Operator.EQUALS; opMenu.stop(); });
        opMenu.setHandler(2, () -> { op[0] = Operator.GREATER_THAN; opMenu.stop(); });
        opMenu.setHandler(3, () -> { op[0] = Operator.LESS_THAN; opMenu.stop(); });
        opMenu.run();

        if (op[0] != null) {
            conditions.add(new TemperatureCondition(temp, op[0]));
            System.out.println("  Temperature condition added.");
        }
    }
}
