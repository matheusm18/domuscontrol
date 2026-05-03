package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.*;
import domuscontrol.menu.Menu;
import domuscontrol.model.device.Device;
import domuscontrol.model.device.types.AdjustableDevice;
import domuscontrol.model.device.types.OpenableDevice;
import domuscontrol.model.device.types.SwitchableDevice;
import domuscontrol.model.houses.House;
import domuscontrol.model.routines.*;
import domuscontrol.model.routines.actions.*;
import domuscontrol.model.routines.conditions.*;
import domuscontrol.user.User;
import domuscontrol.utils.Ansi;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ActionsUI {

    private final DomusControl model;
    private final Scanner sc;

    public ActionsUI(DomusControl model, Scanner sc) {
        this.model = model;
        this.sc = sc;
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

    // ------------------------------------------------------------------------
    // AUTOMATIONS (may have any conditions: time, climate, temperature)
    // ------------------------------------------------------------------------

    public void manageAutomations(int houseId, String email) {
        Menu menu = new Menu("Automations", new String[] {
                "List Automations",
                "Add Automation",
                "Remove Automation"
        }, model::getCurrentState);

        House house = model.getHouseById(houseId);
        RoutineManager rm = house.getRoutineFacade();

        menu.setPreCondition(1, () -> !rm.getOnlySchedules().isEmpty());
        menu.setPreCondition(2, () -> house.getDevices().size() > 0);
        menu.setPreCondition(3, () -> !rm.getOnlySchedules().isEmpty());
        
        menu.setHandler(1, () -> listAutomations(houseId));
        menu.setHandler(2, () -> addAutomation(houseId));
        menu.setHandler(3, () -> removeAutomation(houseId));
        menu.run();
    }

    private void listAutomations(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();
            List<Automation> automations = rm.getOnlyAutomations();
            if (automations.isEmpty()) {
                System.out.println("  No automations.");
                return;
            }
            Ansi.listTitle("Automations");
            for (int i = 0; i < automations.size(); i++) {
                Ansi.listRow(String.format("%d | Name: %-15s", i + 1, automations.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter Automation Number for details (or 0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Automation selected = automations.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                System.out.println("\n" + Ansi.CYAN + "--- Automation Info ---" + Ansi.RESET);
                System.out.println(selected);
                System.out.println(Ansi.CYAN + "-----------------------" + Ansi.RESET);
            } else {
                System.out.println("  Invalid Automation Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addAutomation(int houseId) {
        System.out.print(Ansi.prompt("Automation name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();

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
            rm.addAutomation(automation);
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
                    rm.addAutomation(endAutomation);
                    break;
                }
            }

        } catch (HouseNotFoundException | NameAlreadyExistsException
                | ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void removeAutomation(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();
            List<Automation> automations = rm.getOnlyAutomations();
            
            if (automations.isEmpty()) {
                System.out.println("  No automations to remove.");
                return;
            }

            Ansi.listTitle("Select Automation to Remove");
            for (int i = 0; i < automations.size(); i++) {
                Ansi.listRow(String.format("%d | %s", i + 1, automations.get(i).getName()));
            }
            Ansi.listSeparator();
            
            System.out.print(Ansi.prompt("Enter number (0 to cancel)"));
            int choice = readInt();
            
            if (choice < 1 || choice > automations.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = automations.get(choice - 1).getName();
            rm.removeAutomation(name);
            System.out.println("  Automation '" + name + "' removed.");
            
        } catch (HouseNotFoundException | AutomationDoesntExistException e) {
            System.out.println("  Error: " + e.getMessage());
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

        House house = model.getHouseById(houseId);
        RoutineManager rm = house.getRoutineFacade();

        menu.setPreCondition(1, () -> !rm.getOnlySchedules().isEmpty());
        menu.setPreCondition(2, () -> house.getDevices().size() > 0);
        menu.setPreCondition(3, () -> !rm.getOnlySchedules().isEmpty());

        menu.setHandler(1, () -> listSchedules(houseId));
        menu.setHandler(2, () -> addSchedule(houseId));
        menu.setHandler(3, () -> removeSchedule(houseId));
        menu.run();
    }

    private void listSchedules(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();
            List<Automation> schedules = rm.getOnlySchedules();
            if (schedules.isEmpty()) {
                System.out.println("  No schedules.");
                return;
            }
            Ansi.listTitle("Schedules");
            for (int i = 0; i < schedules.size(); i++) {
                Ansi.listRow(String.format("%d | Name: %-15s", i + 1, schedules.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter Schedule Number for details (or 0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Automation selected = schedules.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                System.out.println("\n" + Ansi.CYAN + "--- Schedule Info ---" + Ansi.RESET);
                System.out.println(selected);
                System.out.println(Ansi.CYAN + "---------------------" + Ansi.RESET);
            } else {
                System.out.println("  Invalid Schedule Number.");
            }
        } catch (HouseNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void addSchedule(int houseId) {
        System.out.print(Ansi.prompt("Schedule name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();

            List<Action> actions = readActionsDialog(house);
            List<Condition> conditions = readConditionsDialog(house, true);

            Automation schedule = new Automation(name, AutomationType.SCHEDULE, conditions, actions);
            rm.addAutomation(schedule);
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
                    rm.addAutomation(endAutomation);
                    break;
                }
            }

        } catch (HouseNotFoundException | NameAlreadyExistsException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (ScheduleWithConditionDifferentFromTimeException e) {
            System.out.println("  Error: Schedules only accept time conditions. " + e.getMessage());
        }
    }

    private void removeSchedule(int houseId) {
        try {
            House house = model.getHouseById(houseId);
            RoutineManager rm = house.getRoutineFacade();
            List<Automation> schedules = rm.getOnlySchedules();

            if (schedules.isEmpty()) {
                System.out.println("  No schedules to remove.");
                return;
            }

            Ansi.listTitle("Select Schedule to Remove");
            for (int i = 0; i < schedules.size(); i++) {
                Ansi.listRow(String.format("%d | %s", i + 1, schedules.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter number (0 to cancel)"));
            int choice = readInt();

            if (choice < 1 || choice > schedules.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = schedules.get(choice - 1).getName();
            rm.removeAutomation(name);
            System.out.println("  Schedule '" + name + "' removed.");

        } catch (HouseNotFoundException | AutomationDoesntExistException e) {
            System.out.println("  Error: " + e.getMessage());
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
        
        House house = model.getHouseById(houseId);
        User user = model.getUserByEmail(email);

        menu.setPreCondition(1, () -> !house.getRoutineFacade().getScenariosForUser(user.getId()).isEmpty());
        menu.setPreCondition(2, () -> !house.getRoutineFacade().getScenariosForUser(user.getId()).isEmpty());
        menu.setPreCondition(3, () -> house.getDevices().size() > 0);
        menu.setPreCondition(4, () -> !house.getRoutineFacade().getScenariosForUser(user.getId()).isEmpty());

        menu.setHandler(1, () -> listScenarios(houseId, email));
        menu.setHandler(2, () -> executeScenario(houseId, email));
        menu.setHandler(3, () -> addScenario(houseId, email));
        menu.setHandler(4, () -> removeScenario(houseId, email));
        menu.run();
    }

    private void listScenarios(int houseId, String email) {
        try {
            House house = model.getHouseById(houseId);
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = house.getRoutineFacade().getScenariosForUser(user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios.");
                return;
            }
            Ansi.listTitle("Scenarios");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d | Name: %-15s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter Scenario Number for details (or 0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Scenario selected = scenarios.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                System.out.println("\n" + Ansi.CYAN + "--- Scenario Info ---" + Ansi.RESET);
                System.out.println(selected);
                System.out.println(Ansi.CYAN + "---------------------" + Ansi.RESET);
            } else {
                System.out.println("  Invalid Scenario Number.");
            }
        } catch (HouseNotFoundException | UserNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (UserDoesntHaveScenarios e) {
            System.out.println("  No scenarios found for this user.");
        }
    }

    private void executeScenario(int houseId, String email) {
        try {
            House house = model.getHouseById(houseId);
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = house.getRoutineFacade().getScenariosForUser(user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios.");
                return;
            }
            Ansi.listTitle("Scenarios");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d | Name: %-15s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter Scenario Number for details (or 0 to back)"));
            int choice = readInt();
            if (choice == 0)
                return;

            Scenario selected = scenarios.stream()
                    .skip(choice - 1)
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                house.getRoutineFacade().executeScenarioByName(user.getId(), selected.getName());
                System.out.print(String.format("  Scenario %s executed.\n", selected.getName()));
            }
        } catch (HouseNotFoundException | UserNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (UserDoesntHaveScenarios | ScenarioDoesntExistException e) {
            System.out.println("  Error: Scenario not found.");
        }
    }

    private void addScenario(int houseId, String email) {
        System.out.print(Ansi.prompt("Scenario name"));
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty.");
            return;
        }

        try {
            House house = model.getHouseById(houseId);
            User user = model.getUserByEmail(email);
            List<Action> actions = readActionsDialog(house);
            Scenario scenario = new Scenario(name, actions);
            house.getRoutineFacade().addScenario(user.getId(), scenario);
            System.out.println("  Scenario '" + name + "' added.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void removeScenario(int houseId, String email) {
        try {
            House house = model.getHouseById(houseId);
            User user = model.getUserByEmail(email);
            List<Scenario> scenarios = house.getRoutineFacade().getScenariosForUser(user.getId());

            if (scenarios.isEmpty()) {
                System.out.println("  No scenarios to remove.");
                return;
            }

            Ansi.listTitle("Select Scenario to Remove");
            for (int i = 0; i < scenarios.size(); i++) {
                Ansi.listRow(String.format("%d | %s", i + 1, scenarios.get(i).getName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Enter number (0 to cancel)"));
            int choice = readInt();

            if (choice < 1 || choice > scenarios.size()) {
                System.out.println("  Cancelled.");
                return;
            }

            String name = scenarios.get(choice - 1).getName();
            house.getRoutineFacade().removeScenario(user.getId(), name);
            System.out.println("  Scenario '" + name + "' removed.");

        } catch (HouseNotFoundException | UserNotFoundException e) {
            System.out.println("  Error: " + e.getMessage());
        } catch (UserDoesntHaveScenarios e) {
            System.out.println("  No scenarios found for this user.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
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
            // Criamos uma lista para mapear a seleção numérica ao dispositivo real
            List<Device> deviceList = new ArrayList<>(house.getDevices().values());

            for (Device d : deviceList) {
                Ansi.listRow(String.format("%d | %-15s (%s)", num++, d.getModel(), d.getClass().getSimpleName()));
            }
            Ansi.listSeparator();

            System.out.print(Ansi.prompt("Select Device Number (0 to finish)"));
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
            new String[] { "Turn On", "Turn Off", "Set Level (0-100)", "Set Opening (0-100)" }, 
            model::getCurrentState);

        typeMenu.setPreCondition(1, () -> device instanceof SwitchableDevice);
        typeMenu.setPreCondition(2, () -> device instanceof SwitchableDevice);
        typeMenu.setPreCondition(3, () -> device instanceof AdjustableDevice);
        typeMenu.setPreCondition(4, () -> device instanceof OpenableDevice);

        typeMenu.setHandler(1, () -> {
            actions.add(new TurnOnAction((SwitchableDevice) device));
            System.out.println(Ansi.GREEN + "  Action added." + Ansi.RESET);
            typeMenu.stop();
        });
        // ... (repetir lógica para Turn Off, Set Level e Set Opening com a mesma estética)

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
                "Time Condition" + (hasTime[0] ? Ansi.GREEN + " [Added]" + Ansi.RESET : ""),
                "Device Condition",
                "Temperature Condition",
                "Weather Condition",
                "Done"
            };

            Menu menu = new Menu("Add Condition", options, model::getCurrentState);

            // PreConditions baseadas no contexto (Automation vs Schedule)[cite: 2, 3]
            menu.setPreCondition(1, () -> !hasTime[0]); // Só permite adicionar condição de tempo se ainda não houver uma
            menu.setPreCondition(2, () -> !timeOnly);
            menu.setPreCondition(3, () -> !timeOnly);
            menu.setPreCondition(4, () -> !timeOnly);

            menu.setHandler(1, () -> handleAddTimeCondition(conditions, hasTime));
            menu.setHandler(2, () -> handleAddDeviceCondition(conditions, house));
            menu.setHandler(3, () -> handleAddTemperatureCondition(conditions));
            menu.setHandler(4, () -> handleAddWeatherCondition(conditions));
            menu.setHandler(5, () -> { adding[0] = false; menu.stop(); });

            menu.run();
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
                if (dev instanceof SwitchableDevice d) {
                    System.out.print(Ansi.prompt("Trigger when ON? (true/false)"));
                    boolean on = Boolean.parseBoolean(sc.nextLine().trim());
                    conditions.add(new DeviceStateCondition(d, on));
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
                if (dev instanceof AdjustableDevice d) {
                    System.out.print(Ansi.prompt("Trigger level"));
                    int lvl = readInt();
                    conditions.add(new DeviceLevelCondition(d, lvl, Operator.EQUALS));
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
        domuscontrol.Simulation.WeatherCondition[] weathers = domuscontrol.Simulation.WeatherCondition.values();
        String[] weatherNames = new String[weathers.length];
        for (int i = 0; i < weathers.length; i++) weatherNames[i] = weathers[i].name();

        Menu weatherMenu = new Menu("Select Weather", weatherNames, model::getCurrentState);

        for (int i = 0; i < weathers.length; i++) {
            final int index = i;
            weatherMenu.setHandler(i + 1, () -> {
                conditions.add(new OutsideWeatherCondition(weathers[index]));
                System.out.println(Ansi.GREEN + "  Weather condition added." + Ansi.RESET);
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
            System.out.println(Ansi.GREEN + "  Temperature condition added." + Ansi.RESET);
        }
    }
}