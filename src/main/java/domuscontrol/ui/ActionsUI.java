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
import domuscontrol.routines.conditions.DeviceOpenCondition;
import domuscontrol.routines.conditions.LuminositySensorCondition;
import domuscontrol.routines.conditions.RainfallSensorCondition;
import domuscontrol.routines.conditions.TemperatureSensorCondition;
import domuscontrol.user.User;
import domuscontrol.utils.Ansi;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    // AUTOMATIONS (may have any conditions: time, climate, temperature, luminosity)
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
            Map<Integer, Device> devices = model.getHouseById(houseId).getDevices();
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
                showAutomationInfo(selected, devices);
            } else {
                System.out.println("  Invalid Automation Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        }
    }

    private void showAutomationInfo(Automation automation, Map<Integer, Device> devices) {
        Ansi.listTitle(automation.getType().toString());
        Ansi.listRow(String.format("%-12s %s", "Name", automation.getName()));
        Ansi.listSeparator();
        Ansi.listRow("Conditions:");
        for (Condition c : automation.getConditions())
            Ansi.listRow("    " + describeCondition(c, devices));
        Ansi.listSeparator();
        Ansi.listRow("Actions:");
        for (Action a : automation.getActions())
            Ansi.listRow("    " + describeAction(a, devices));
        Ansi.listSeparator();
    }

    private void showScenarioInfo(Scenario scenario, Map<Integer, Device> devices) {
        Ansi.listTitle("Scenario");
        Ansi.listRow(String.format("%-12s %s", "Name", scenario.getName()));
        Ansi.listSeparator();
        Ansi.listRow("Actions:");
        for (Action a : scenario.getActions())
            Ansi.listRow("    " + describeAction(a, devices));
        Ansi.listSeparator();
    }

    private String describeAction(Action a, Map<Integer, Device> devices) {
        if (a instanceof TurnOnAction ta)
            return "Turn ON   " + devLabel(ta.getDeviceId(), devices) + " [#" + ta.getDeviceId() + "]";
        if (a instanceof TurnOffAction ta)
            return "Turn OFF  " + devLabel(ta.getDeviceId(), devices) + " [#" + ta.getDeviceId() + "]";
        if (a instanceof SetLevelAction sa)
            return "Set level " + sa.getTargetLevel() + "%  " + devLabel(sa.getDeviceId(), devices) + " [#" + sa.getDeviceId() + "]";
        if (a instanceof SetOpeningAction sa)
            return "Set opening " + sa.getTargetPercentage() + "%  " + devLabel(sa.getDeviceId(), devices) + " [#" + sa.getDeviceId() + "]";
        if (a instanceof SetColorTemperatureAction sa)
            return "Set color " + sa.getTargetTemperature() + "K  " + devLabel(sa.getDeviceId(), devices) + " [#" + sa.getDeviceId() + "]";
        return a.toString();
    }

    private String describeCondition(Condition c, Map<Integer, Device> devices) {
        if (c instanceof TimeCondition tc)
            return "At " + tc.getTriggerTime();
        if (c instanceof TimeWindowCondition tw)
            return "Between " + tw.getStartTime() + " and " + tw.getEndTime();
        if (c instanceof DeviceStateCondition dc2)
            return devLabel(dc2.getDeviceId(), devices) + " is " + (dc2.getTriggerWhenOn() ? "ON" : "OFF");
        if (c instanceof DeviceLevelCondition dlc)
            return devLabel(dlc.getDeviceId(), devices) + " level " + opSymbol(dlc.getOperator()) + " " + dlc.getTriggerLevel() + "%";
        if (c instanceof DeviceOpenCondition doc)
            return devLabel(doc.getDeviceId(), devices) + " opening " + opSymbol(doc.getOperator()) + " " + doc.getTriggerLevel() + "%";
        if (c instanceof ColorTemperatureCondition ctc)
            return devLabel(ctc.getDeviceId(), devices) + " color " + opSymbol(ctc.getOperator()) + " " + ctc.getTriggerTemperature() + "K";
        if (c instanceof TemperatureSensorCondition tsc)
            return "Temp (" + devLabel(tsc.getSensorId(), devices) + ") " + opSymbol(tsc.getOperator()) + " " + tsc.getTriggerTemperature() + "ºC";
        if (c instanceof LuminositySensorCondition lsc)
            return "Luminosity (" + devLabel(lsc.getSensorId(), devices) + ") " + opSymbol(lsc.getOperator()) + " " + lsc.getTriggerLuminosity() + " lx";
        if (c instanceof RainfallSensorCondition rsc)
            return "Rainfall (" + devLabel(rsc.getSensorId(), devices) + ") " + opSymbol(rsc.getOperator()) + " " + String.format("%.1f", rsc.getTriggerRainfall()) + " mm/h";
        return c.toString();
    }

    private String devLabel(int id, Map<Integer, Device> devices) {
        Device d = devices.get(id);
        return d != null ? d.getBrand() + " " + d.getModel() : "#" + id;
    }

    private String opSymbol(Operator op) {
        return switch (op) {
            case GREATER_THAN -> ">";
            case LESS_THAN    -> "<";
            case EQUALS       -> "=";
        };
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
            if (actions.isEmpty()) {
                System.out.println("  Error: automation must have at least one action.");
                return;
            }

            Ansi.listTitle("Selected Actions");
            for (Action a : actions) {
                Ansi.listRow(" - " + a);
            }
            Ansi.listSeparator();

            List<Condition> conditions = readConditionsDialog(house, false);
            if (conditions.isEmpty()) {
                System.out.println("  Error: automation must have at least one condition.");
                return;
            }

            Ansi.listTitle("Selected Conditions");
            for (Condition c : conditions) {
                Ansi.listRow(" - " + c);
            }
            Ansi.listSeparator();

            TimeWindowCondition timeWindow = findTimeWindowCondition(conditions);
            List<Action> endActions = readEndActionsIfNeeded(house, timeWindow);
            if (timeWindow != null && endActions.isEmpty()) {
                System.out.println("  Error: time window routines must have at least one end action.");
                return;
            }

            Automation automation = new Automation(name, AutomationType.AUTOMATION, conditions, actions);
            model.addAutomation(houseId, automation);
            System.out.println("  Automation '" + name + "' added.");

            addTimeWindowEndRoutine(houseId, name, AutomationType.AUTOMATION, conditions, timeWindow, endActions);

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
            Map<Integer, Device> devices = model.getHouseById(houseId).getDevices();
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
                showAutomationInfo(selected, devices);
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
            if (actions.isEmpty()) {
                System.out.println("  Error: schedule must have at least one action.");
                return;
            }

            List<Condition> conditions = readConditionsDialog(house, true);
            if (conditions.isEmpty()) {
                System.out.println("  Error: schedule must have at least one condition.");
                return;
            }

            TimeWindowCondition timeWindow = findTimeWindowCondition(conditions);
            List<Action> endActions = readEndActionsIfNeeded(house, timeWindow);
            if (timeWindow != null && endActions.isEmpty()) {
                System.out.println("  Error: time window schedules must have at least one end action.");
                return;
            }

            Automation schedule = new Automation(name, AutomationType.SCHEDULE, conditions, actions);
            model.addAutomation(houseId, schedule);
            System.out.println("  Schedule '" + name + "' added.");

            addTimeWindowEndRoutine(houseId, name, AutomationType.SCHEDULE, conditions, timeWindow, endActions);

        } catch (HouseNotFoundException e) {
            System.out.println("  Error: house not found.");
        } catch (NameAlreadyExistsException e) {
            System.out.println("  Error: schedule name already exists.");
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: schedules only accept time conditions.");
        }
    }

    private TimeWindowCondition findTimeWindowCondition(List<Condition> conditions) {
        for (Condition c : conditions) {
            if (c instanceof TimeWindowCondition twc) {
                return twc;
            }
        }
        return null;
    }

    private List<Action> readEndActionsIfNeeded(House house, TimeWindowCondition timeWindow) {
        if (timeWindow == null) {
            return new ArrayList<>();
        }

        Ansi.listTitle("End Actions");
        Ansi.listRow("Select what should happen when the time window ends.");
        Ansi.listSeparator();

        return readActionsDialog(house);
    }

    private void addTimeWindowEndRoutine(int houseId, String name, AutomationType type, List<Condition> conditions,
                                         TimeWindowCondition timeWindow, List<Action> endActions)
            throws HouseNotFoundException, NameAlreadyExistsException, ScheduleWithConditionDifferentFromTimeException {
        if (timeWindow == null || endActions.isEmpty()) {
            return;
        }

        List<Condition> endConditions = new ArrayList<>();
        endConditions.add(new TimeCondition(timeWindow.getEndTime()));
        for (Condition condition : conditions) {
            if (condition != timeWindow) {
                endConditions.add(condition.copy());
            }
        }

        Automation endAutomation = new Automation(name + " [END]", type, endConditions, endActions);
        model.addAutomation(houseId, endAutomation);
        System.out.println("  End routine '" + name + " [END]' added.");
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
            Map<Integer, Device> devices = model.getHouseById(houseId).getDevices();

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
                showScenarioInfo(selected, devices);
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
            if (actions.isEmpty()) {
                System.out.println("  Error: scenario must have at least one action.");
                return;
            }
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
                Ansi.listRow(String.format("%d  %-15s %-12s %s",
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

        if (timeOnly) {
            Ansi.listTitle("Schedule Info");
            Ansi.listRow(Ansi.YELLOW + "Schedules only support time-based triggers." + Ansi.RESET);
            Ansi.listSeparator();
        }

        String[] options = {
            "Device ON/OFF State",
            "Device Opening State",
            "Device Level",
            "Sensor Value",
            "Time Condition"
        };

        Menu menu = new Menu("Add Condition", options, model::getCurrentState);
        menu.setExitLabel("Done");

        menu.setPreCondition(1, () -> !timeOnly);
        menu.setPreCondition(2, () -> !timeOnly);
        menu.setPreCondition(3, () -> !timeOnly);
        menu.setPreCondition(4, () -> !timeOnly);
        menu.setPreCondition(5, () -> !hasTime[0]);

        menu.setHandler(1, () -> handleAddDeviceOnOffCondition(conditions, house));
        menu.setHandler(2, () -> handleAddDeviceOpeningCondition(conditions, house));
        menu.setHandler(3, () -> handleAddDeviceLevelCondition(conditions, house));
        menu.setHandler(4, () -> handleAddSensorCondition(conditions, house));
        menu.setHandler(5, () -> handleAddTimeCondition(conditions, hasTime));

        menu.run();
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

    private void handleAddDeviceOnOffCondition(List<Condition> conditions, House house) {
        List<Device> compatible = house.getDevices().values().stream()
            .filter(d -> d instanceof SwitchableDevice && !(d instanceof domuscontrol.devices.sensors.Sensor))
            .collect(java.util.stream.Collectors.toList());
        if (compatible.isEmpty()) { System.out.println("  No switchable devices available."); return; }
        Device picked = pickFromList(compatible, "switchable device");
        if (picked == null) return;
        System.out.print(Ansi.prompt("Trigger when ON? (true/false)"));
        boolean on = Boolean.parseBoolean(sc.nextLine().trim());
        conditions.add(new DeviceStateCondition(picked.getId(), on));
        System.out.println("  Condition added.");
    }

    private void handleAddDeviceOpeningCondition(List<Condition> conditions, House house) {
        List<Device> compatible = house.getDevices().values().stream()
            .filter(d -> d instanceof OpenableDevice)
            .collect(java.util.stream.Collectors.toList());
        if (compatible.isEmpty()) { System.out.println("  No openable devices available."); return; }
        Device picked = pickFromList(compatible, "openable device");
        if (picked == null) return;
        System.out.print(Ansi.prompt("Trigger opening (0-100)"));
        int opening = readInt();
        Operator op = pickOperator();
        if (op == null) return;
        conditions.add(new DeviceOpenCondition(picked.getId(), opening, op));
        System.out.println("  Condition added.");
    }

    private void handleAddDeviceLevelCondition(List<Condition> conditions, House house) {
        List<Device> compatible = house.getDevices().values().stream()
            .filter(d -> d instanceof AdjustableDevice)
            .collect(java.util.stream.Collectors.toList());
        if (compatible.isEmpty()) { System.out.println("  No adjustable devices available."); return; }
        Device picked = pickFromList(compatible, "adjustable device");
        if (picked == null) return;
        System.out.print(Ansi.prompt("Trigger level (0-100)"));
        int level = readInt();
        Operator op = pickOperator();
        if (op == null) return;
        conditions.add(new DeviceLevelCondition(picked.getId(), level, op));
        System.out.println("  Condition added.");
    }

    private void handleAddSensorCondition(List<Condition> conditions, House house) {
        List<Device> sensors = house.getDevices().values().stream()
            .filter(d -> d instanceof domuscontrol.devices.sensors.Sensor)
            .collect(java.util.stream.Collectors.toList());
        if (sensors.isEmpty()) { System.out.println("  No sensors available. Add a sensor device first."); return; }
        Device picked = pickFromList(sensors, "sensor");
        if (picked == null) return;

        if (picked instanceof domuscontrol.devices.sensors.TemperatureSensor) {
            System.out.print(Ansi.prompt("Trigger temperature (ºC)"));
            int temp = readInt();
            Operator op = pickOperator();
            if (op == null) return;
            conditions.add(new TemperatureSensorCondition(picked.getId(), temp, op));
            System.out.println("  Sensor condition added.");
        } else if (picked instanceof domuscontrol.devices.sensors.LuminositySensor) {
            System.out.print(Ansi.prompt("Trigger luminosity (lx)"));
            int lux = readInt();
            Operator op = pickOperator();
            if (op == null) return;
            conditions.add(new LuminositySensorCondition(picked.getId(), lux, op));
            System.out.println("  Sensor condition added.");
        } else if (picked instanceof domuscontrol.devices.sensors.RainfallSensor) {
            System.out.print(Ansi.prompt("Trigger rainfall (mm/h)"));
            double rainfall = readDouble();
            Operator op = pickOperator();
            if (op == null) return;
            conditions.add(new RainfallSensorCondition(picked.getId(), rainfall, op));
            System.out.println("  Sensor condition added.");
        }
    }

    private Device pickFromList(List<Device> devices, String label) {
        Ansi.listTitle("Select " + label);
        for (int i = 0; i < devices.size(); i++) {
            Device d = devices.get(i);
            Ansi.listRow(String.format("%d  %-20s %-12s %s",
                i + 1, d.getClass().getSimpleName(), d.getBrand(), d.getModel()));
        }
        Ansi.listSeparator();
        System.out.print(Ansi.prompt("Select (0 to cancel)"));
        int choice = readInt();
        if (choice < 1 || choice > devices.size()) return null;
        return devices.get(choice - 1);
    }

    private Operator pickOperator() {
        Operator[] op = { null };
        Menu opMenu = new Menu("Select Operator", new String[] {
            "EQUALS (==)", "GREATER THAN (>)", "LESS THAN (<)"
        }, model::getCurrentState);
        opMenu.setHandler(1, () -> { op[0] = Operator.EQUALS;       opMenu.stop(); });
        opMenu.setHandler(2, () -> { op[0] = Operator.GREATER_THAN; opMenu.stop(); });
        opMenu.setHandler(3, () -> { op[0] = Operator.LESS_THAN;    opMenu.stop(); });
        opMenu.run();
        return op[0];
    }

    private double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
