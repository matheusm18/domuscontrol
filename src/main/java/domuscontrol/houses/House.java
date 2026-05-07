package domuscontrol.houses;

import domuscontrol.devices.sensors.Sensor;
import domuscontrol.simulation.Simulation;
import domuscontrol.simulation.SimulationState;
import domuscontrol.suggestions.AutomationSuggestion;
import domuscontrol.suggestions.DeviceInteraction;
import domuscontrol.suggestions.InteractionLogger;
import domuscontrol.suggestions.SuggestionEngine;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.routines.Automation;
import domuscontrol.routines.RoutineManager;
import domuscontrol.routines.Scenario;
import domuscontrol.exceptions.UserDoesntHaveScenarios;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.AutomationDoesntExistException;
import domuscontrol.exceptions.ScenarioDoesntExistException;

import java.util.Map;
import java.util.Set;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a complete House in the Domus Control automation system.
 * A House aggregates multiple Divisions (rooms), managing the overall state,
 * simulating the passage of time, and calculating global statistics.
 * Manages devices, user roles, automations, scenarios, and interaction logging for suggestions.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class House implements Serializable {

    /**
     * Static counter used to automatically assign unique, sequential IDs to each new house.
     */
    private static int nextId = 1;

    /**
     * The name or designation of the house.
     */
    private String name;

    /**
     * The unique identifier for this specific house.
     */
    private final int id;

    /**
     * A map storing all devices within this house.
     * The key is the device's unique ID, and the value is the Device object itself.
     */
    private Map<Integer, Device> devices;

    /**
     * A map storing all divisions within this house.
     * The key is the division's name, and the value is a list of devices in that division.
     */
    private Map<String, List<Device>> divisions;

    /**
     * The manager for all automations, schedules, and scenarios for this house.
     */
    private RoutineManager routineManager;

    /**
     * The logger that records all manual device interactions performed in this house.
     * Used by the SuggestionEngine to detect patterns and generate automation suggestions.
     */
    private InteractionLogger interactionLogger;

    /**
     * Updates the static ID counter.
     * Useful when loading a saved system state to ensure new houses do not overlap
     * with previously assigned IDs.
     *
     * @param lastId The highest ID currently loaded in the system.
     */
    public static void setNextId(int lastId) {
        nextId = lastId + 1;
    }

    /**
     * Default constructor.
     * Initializes a new house with a unique ID, an empty name, and an empty routine manager.
     */
    public House() {
        this.id = House.nextId++;
        this.name = "";
        this.divisions = new HashMap<>();
        this.devices = new HashMap<>();
        this.routineManager = new RoutineManager();
        this.interactionLogger = new InteractionLogger();
    }

    /**
     * Parameterized constructor.
     * Initializes a new house with a unique ID, a specific name, and predefined divisions.
     *
     * @param divisions    A map of divisions to populate the house.
     * @param devices      A map of devices to populate the house.
     * @param name         The name of the house.
     */
    public House(Map<String, List<Device>> divisions, Map<Integer, Device> devices, String name) {
        this.id = House.nextId++;
        this.name = name;
        this.routineManager = new RoutineManager();
        this.interactionLogger = new InteractionLogger();
        this.setDevices(devices);
        this.setDivisions(divisions);
    }

    /**
     * Copy constructor.
     * Creates a new House instance by copying the state, retaining the exact ID,
     * and performing deep copies of devices, routines, and the interaction logger.
     *
     * @param h The House object to copy.
     */
    public House(House h) {
        this.id = h.getId();
        this.name = h.getName();
        this.routineManager = h.getRoutineManager();
        this.interactionLogger = h.getInteractionLogger();
        this.setDevices(h.getDevices());
        this.setDivisions(h.getDivisions());
    }

    /**
     * Retrieves the unique identifier of this house.
     *
     * @return The integer ID.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Retrieves the name of this house.
     *
     * @return The name string.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets or changes the name of this house.
     *
     * @param name The new name for the house.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves a deep copy of the routine manager that manages this house's automations.
     *
     * @return A cloned RoutineManager object.
     */
    public RoutineManager getRoutineManager() {
        return this.routineManager.clone();
    }

    /**
     * Sets the devices for this house. Each device is cloned to preserve encapsulation.
     *
     * @param devices A map of devices to be added to this house (will be cloned).
     */
    public void setDevices(Map<Integer, Device> devices) {
        Map<Integer, Device> devicesMap = new HashMap<>();
        if (devices != null) {
            devices.forEach((k, v) -> devicesMap.put(k, v.clone()));
        }
        this.devices = devicesMap;
    }

    /**
     * Sets the divisions for this house by creating a copy of the provided map's lists,
     * resolving each device by ID against {@code this.devices}.
     * Must be called after {@link #setDevices} so that all referenced IDs are present.
     *
     * @param divisions A map of divisions to be added to this house.
     */
    public void setDivisions(Map<String, List<Device>> divisions) {
        Map<String, List<Device>> divisionsMap = new HashMap<>();
        if (divisions != null) {
            divisions.forEach((name, list) -> {
                divisionsMap.put(name, list.stream()
                    .map(dev -> this.devices.get(dev.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            });
        }
        this.divisions = divisionsMap;
    }

    /**
     * Retrieves all devices currently in this house.
     *
     * @return A map containing clones of the internal devices.
     */
    public Map<Integer, Device> getDevices() {
        Map<Integer, Device> newMap = new HashMap<>();
        this.devices.forEach((k, v) -> newMap.put(k, v.clone()));
        return newMap;
    }

    /**
     * Retrieves all divisions currently in this house.
     *
     * @return A map containing the divisions and clones of their devices.
     */
    public Map<String, List<Device>> getDivisions() {
        Map<String, List<Device>> newMap = new HashMap<>();
        this.divisions.forEach((name, list) -> {
            newMap.put(name, list.stream().map(Device::clone).collect(Collectors.toList()));
        });
        return newMap;
    }

    /**
     * Returns the interaction logger of this house.
     * The returned logger is a deep copy to preserve encapsulation.
     *
     * @return A clone of the internal InteractionLogger.
     */
    public InteractionLogger getInteractionLogger() {
        return this.interactionLogger.clone();
    }

    /**
     * Records a manual device interaction in the house's interaction history.
     *
     * @param interaction The interaction to record.
     */
    public void logInteraction(DeviceInteraction interaction) {
        this.interactionLogger.log(interaction);
    }

    /**
     * Analyses the interaction history of this house and returns a list of
     * automation and schedule suggestions based on detected patterns.
     * Passes cloned devices to the SuggestionEngine so suggestions can validate
     * types and descriptions without receiving live device references.
     *
     * @param userId The ID of the user to generate suggestions for.
     * @return A list of AutomationSuggestion objects ready to be presented to the user.
     * @throws ScheduleWithConditionDifferentFromTimeException if a suggested schedule
     *         is built with a non-time condition, which should never happen in practice.
     */
    public List<AutomationSuggestion> getSuggestions(int userId) throws ScheduleWithConditionDifferentFromTimeException {
        List<AutomationSuggestion> all = SuggestionEngine.suggest(this.interactionLogger.clone(), this.getDevices(), userId);
        Set<String> existing = this.routineManager.getAutomationsByName().keySet();
        return all.stream()
            .filter(s -> !existing.contains(s.getAutomation().getName().toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * Adds an empty division to the house.
     *
     * @param name The name of the division.
     */
    public void addDivision(String name) {
        this.divisions.put(name, new ArrayList<>());
    }

    /**
     * Removes a division from the house based on its name.
     *
     * @param name The name of the division to remove.
     * @throws DivisionNotFoundException if no division with the given name exists.
     */
    public void deleteDivision(String name) throws DivisionNotFoundException {
        if (!this.divisions.containsKey(name)) {
            throw new DivisionNotFoundException(name);
        }
        List<Device> devicesToRemove = this.divisions.get(name);
        for (Device device : devicesToRemove) {
            this.devices.remove(device.getId());
            this.routineManager.removeDevice(device.getId());
        }
        this.divisions.remove(name);
    }

    /**
     * Adds a device to a specific division. If the device is not in the global map, it is added.
     *
     * @param device   The device to add.
     * @param division The name of the division.
     * @throws DivisionNotFoundException if the division does not exist.
     */
    public void addDeviceToDivision(Device device, String division) throws DivisionNotFoundException {
        if (!this.divisions.containsKey(division)) throw new DivisionNotFoundException(division);
        Device stored = this.devices.computeIfAbsent(device.getId(), k -> device.clone());
        List<Device> divisionDevices = this.divisions.get(division);                                                                                                                                          
        if (divisionDevices.stream().noneMatch(d -> d.getId() == device.getId())) {
            divisionDevices.add(stored);                                                                                                                                                                      
        }                
    }

    /**
     * Completely removes a device from the house.
     * Deletes the device from the global devices map, cascades the deletion to all
     * divisions, and clears all associated actions and conditions from the routine manager.
     *
     * @param deviceId The unique identifier of the device to be removed.
     * @throws DeviceNotFoundException if no device with the specified ID exists in the house.
     */
    public void removeDevice(int deviceId) throws DeviceNotFoundException {
        if (!this.devices.containsKey(deviceId)) throw new DeviceNotFoundException("" + deviceId);
        this.devices.remove(deviceId);
        this.divisions.values().forEach(list -> list.removeIf(d -> d.getId() == deviceId));
        this.routineManager.removeDevice(deviceId);
    }

    /**
     * Removes a specific device from a designated division.
     *
     * @param deviceId The unique identifier of the device to remove from the division.
     * @param division The name of the division from which the device should be removed.
     * @throws DivisionNotFoundException if the specified division does not exist in the house.
     * @throws DeviceNotFoundException   if the device is not found in the specified division.
     */
    public void removeDeviceFromDivision(int deviceId, String division) throws DivisionNotFoundException, DeviceNotFoundException {
        if (!this.divisions.containsKey(division)) throw new DivisionNotFoundException(division);
        List<Device> divisionDevices = this.divisions.get(division);
        if (divisionDevices.stream().noneMatch(d -> d.getId() == deviceId)) throw new DeviceNotFoundException("" + deviceId);
        divisionDevices.removeIf(d -> d.getId() == deviceId);
    }

    /**
     * Retrieves a copy of a device by its ID.
     *
     * @param deviceId The unique identifier of the device.
     * @return A cloned Device object.
     * @throws DeviceNotFoundException if the device ID does not exist in the house.
     */
    public Device getDevice(int deviceId) throws DeviceNotFoundException {
        Device device = this.devices.get(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("" + deviceId);
        }
        return device.clone();
    }

    /**
     * Applies a consumer to a live device reference without exposing it externally.
     *
     * @param deviceId    The ID of the device to interact with.
     * @param interaction The logic to apply to the device.
     * @throws DeviceNotFoundException if the device is not found.
     */
    public void interactWithDevice(int deviceId, Consumer<Device> interaction) throws DeviceNotFoundException {
        Device realDevice = this.devices.get(deviceId);
        if (realDevice == null) throw new DeviceNotFoundException("" + deviceId);
        interaction.accept(realDevice);
    }

    /**
     * Reads a value from a specific device using a functional reader, without exposing the reference.
     *
     * @param deviceId The ID of the device to read from.
     * @param reader   A function that extracts a value from the device.
     * @param <T>      The return type of the reader.
     * @return The value produced by the reader.
     * @throws DeviceNotFoundException if the device is not found.
     */
    public <T> T readDevice(int deviceId, Function<Device, T> reader) throws DeviceNotFoundException {
        Device realDevice = this.devices.get(deviceId);
        if (realDevice == null) throw new DeviceNotFoundException("" + deviceId);
        return reader.apply(realDevice);
    }


    /**
     * Returns the total number of divisions currently existing in this house.
     *
     * @return The division count.
     */
    public int divisionsNumber() {
        return this.divisions.size();
    }

    /**
     * Adds a new scenario for a specific user to the routine manager.
     *
     * @param userId The ID of the user owning the scenario.
     * @param s      The scenario to be added.
     * @throws NameAlreadyExistsException if a scenario with the same name already exists for this user.
     */
    public void addScenario(int userId, Scenario s) throws NameAlreadyExistsException {
        this.routineManager.addScenario(userId, s);
    }

    /**
     * Executes a specific scenario for a user.
     *
     * @param userId The ID of the user triggering the scenario.
     * @param name   The name of the scenario to execute.
     * @throws UserDoesntHaveScenarios      if the user has no registered scenarios.
     * @throws ScenarioDoesntExistException if the specified scenario does not exist.
     */
    public void executeScenario(int userId, String name) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        this.routineManager.executeScenarioByName(userId, name, this);
    }

    /**
     * Adds a new automation to the routine manager.
     *
     * @param a The automation to be added.
     * @throws NameAlreadyExistsException if an automation with the same name already exists.
     */
    public void addAutomation(Automation a) throws NameAlreadyExistsException {
        this.routineManager.addAutomation(a);
    }

    /**
     * Removes an automation from the routine manager by its name.
     *
     * @param name The name of the automation to remove.
     * @throws AutomationDoesntExistException if the specified automation does not exist.
     */
    public void removeAutomation(String name) throws AutomationDoesntExistException {
        this.routineManager.removeAutomation(name);
    }

    /**
     * Removes a scenario from the routine manager for a specific user.
     *
     * @param userId The ID of the user who owns the scenario.
     * @param name   The name of the scenario to remove.
     * @throws UserDoesntHaveScenarios      if the user has no registered scenarios.
     * @throws ScenarioDoesntExistException if the specified scenario does not exist.
     */
    public void removeScenario(int userId, String name) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        this.routineManager.removeScenario(userId, name);
    }

     /**
     * Returns the total aggregate number of devices in the house.
     *
     * @return The global device count.
     */
    public int devicesNumber() {
        return this.devices.size();
    }

    /**
     * Calculates the total energy consumption of the entire house by aggregating
     * the consumption of all its devices.
     *
     * @return The total consumption in Wh.
     */
    public double calculateTotalConsumption() {
        return this.devices.values().stream()
                   .mapToDouble(Device::getEnergyConsumption)
                   .sum();
    }

    /**
     * Returns the top 3 devices ranked by the given integer criterion.
     *
     * @param f A function that extracts an integer value from a device to rank by.
     * @return A list of up to 3 cloned devices in descending order.
     */
    public List<Device> top3Devices(Function<Device, Integer> f) {
        return this.devices.values().stream()
            .sorted(Comparator.comparing(f).reversed())
            .limit(3)
            .map(Device::clone)
            .collect(Collectors.toList());
    }

    /**
     * Returns the top 3 divisions ranked by the given integer criterion.
     *
     * @param f A function that extracts an integer value from a division's device list to rank by.
     * @return A list of up to 3 division names in descending order.
     */
    public List<String> top3Divisions(Function<List<Device>, Integer> f) {
        return this.divisions.entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, List<Device>>, Integer>comparing(e -> f.apply(e.getValue())).reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Simulates the passing of time for the house. Updates device logic and checks
     * all automations and schedules.
     *
     * @param simulation The current simulation state.
     * @return A list of strings describing the actions taken during this tick, such as
     *         which automations were triggered and which devices were affected.
     */
    public List<String> tick(Simulation simulation) {
        SimulationState state = SimulationState.from(simulation);
        int minutes = (int) simulation.getTimeElapsed();
        this.devices.values().forEach(device -> device.tick(minutes));
        this.updateSensors(state);
        return this.routineManager.tick(this, state);
    }

    private void updateSensors(SimulationState state) {
        this.devices.values().forEach(device -> {
            if (device instanceof Sensor sensor) {
                sensor.updateFromState(state);
            }
        });
    }

    /**
     * Creates and returns a copy of this House instance.
     *
     * @return A new House object.
     */
    @Override
    public House clone() {
        return new House(this);
    }

    /**
     * Returns a formatted string representation of the house.
     *
     * @return A formatted string.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("=== House: ").append(this.getName()).append(" [ID: ").append(this.getId()).append("] ===\n");
        if (this.divisions.isEmpty()) {
            str.append("No divisions yet.\n");
        } else {
            this.divisions.forEach((name, list) -> {
                str.append("\n---   Division: ").append(name).append("    ---\n");
                list.forEach(dev -> str.append("  ").append(dev.toString()).append("\n"));
            });
        }
        return str.toString();
    }

    /**
     * Compares this house to another object for logical equality.
     *
     * @param o The object to compare.
     * @return true if equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        House h = (House) o;
        return this.id == h.getId() &&
               Objects.equals(this.name, h.getName()) &&
               this.divisions.equals(h.getDivisions()) &&
               this.devices.equals(h.getDevices()) &&
               Objects.equals(this.routineManager, h.getRoutineManager()) &&
               Objects.equals(this.interactionLogger, h.getInteractionLogger());
    }

    /**
     * Generates a hash code for this house.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.divisions, this.devices,
                            this.routineManager, this.interactionLogger);
    }
}
