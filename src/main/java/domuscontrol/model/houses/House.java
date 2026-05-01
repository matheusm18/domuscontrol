package domuscontrol.model.houses;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.model.device.Device;
import domuscontrol.model.suggestions.AutomationSuggestion;
import domuscontrol.model.suggestions.DeviceInteraction;
import domuscontrol.model.suggestions.InteractionLogger;
import domuscontrol.model.suggestions.SuggestionEngine;
import domuscontrol.user.UserRole;
import domuscontrol.exceptions.UserDoesntHaveScenarios;
import domuscontrol.model.routines.Automation;
import domuscontrol.exceptions.AutomationDoesntExistException;
import domuscontrol.model.routines.RoutineManager;
import domuscontrol.model.routines.Scenario;
import domuscontrol.exceptions.ScenarioDoesntExistException;

import java.util.Map;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a complete House in the Domus Control automation system.
 * A House aggregates multiple Divisions (rooms), managing the overall state,
 * simulating the passage of time, and calculating global statistics.
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
     * The facade managing all automations, schedules, and scenarios for this house.
     */
    private RoutineManager routineFacade;

    /**
     * A map storing the users that have access to this house and their respective roles.
     * The key is the user's id, and the value is their role in the house.
     */
    private Map<Integer, UserRole> usersInHouse;

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
        this.routineFacade = new RoutineManager();
        this.usersInHouse = new HashMap<>();
        this.interactionLogger = new InteractionLogger();
    }

    /**
     * Parameterized constructor.
     * Initializes a new house with a unique ID, a specific name, and predefined divisions.
     *
     * @param divisions    A map of divisions to populate the house.
     * @param devices      A map of devices to populate the house.
     * @param name         The name of the house.
     * @param usersInHouse A map of users and their roles in this house.
     */
    public House(Map<String, List<Device>> divisions, Map<Integer, Device> devices, String name) {
        this.id = House.nextId++;
        this.name = name;
        this.routineFacade = new RoutineManager();
        this.usersInHouse = new HashMap<>(usersInHouse);
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
        this.routineFacade = h.getRoutineFacade();
        this.usersInHouse = new HashMap<>(h.getUserRoles());
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
     * Retrieves a deep copy of the routine facade managing this house's automations.
     *
     * @return A cloned RoutineManager object.
     */
    public RoutineManager getRoutineFacade() {
        return this.routineFacade.clone();
    }

    /**
     * Sets the routine facade for this house, performing a deep copy to preserve encapsulation.
     *
     * @param facade The RoutineManager to assign.
     */
    public void setRoutineFacade(RoutineManager facade) {
        this.routineFacade = facade.clone();
    }

    /**
     * Sets the devices for this house.
     * Each device is cloned and the interactionLogger is registered as observer.
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
     * Sets the divisions for this house by creating a copy of the provided map's lists.
     *
     * @param divisions A map of divisions to be added to this house.
     */
    public void setDivisions(Map<String, List<Device>> divisions) {
        Map<String, List<Device>> divisionsMap = new HashMap<>();
        if (divisions != null) {
            divisions.forEach((name, list) -> {
                divisionsMap.put(name, list.stream()
                    .map(dev -> this.devices.get(dev.getId()))
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
     * Replaces the interaction logger with a deep copy of the provided one.
     *
     * @param interactionLogger The new InteractionLogger to assign.
     */
    public void setInteractionLogger(InteractionLogger interactionLogger) {
        this.interactionLogger = interactionLogger != null ? interactionLogger.clone() : new InteractionLogger();
    }

    /**
     * Records a manual device interaction in the house's interaction history.
     * Should be called from the controller layer after every successful manual device interaction.
     *
     * @param interaction The interaction to record.
     */
    public void logInteraction(DeviceInteraction interaction) {
        this.interactionLogger.log(interaction);
    }

    /**
     * Analyses the interaction history of this house and returns a list of
     * automation and schedule suggestions based on detected patterns.
     * Passes the live internal device map directly to the SuggestionEngine
     * so it can build fully functional Action and Condition objects.
     *
     * @return A list of AutomationSuggestion objects ready to be presented to the user.
     * @throws ScheduleWithConditionDifferentFromTimeException if a suggested schedule
     *         is built with a non-time condition, which should never happen in practice.
     */
    public List<AutomationSuggestion> getSuggestions(int userId) throws ScheduleWithConditionDifferentFromTimeException {
        return SuggestionEngine.suggest(this.interactionLogger, this.devices, userId);
    }

    /**
     * Adds a single division to the house.
     *
     * @param name    The name of the division.
     * @param devices The list of devices in the division.
     */
    public void addDivision(String name, List<Device> devices) {
        if (devices == null) {
            this.divisions.put(name, new ArrayList<>());
        } else {
            this.divisions.put(name, devices.stream()
                .map(dev -> this.devices.get(dev.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        }
    }

    /**
     * Removes a division from the house based on its name.
     *
     * @param name The name of the division to remove.
     * @throws DivisionNotFoundException if no division with the given name exists.
     */
    public void deleteDivision(String name) throws DivisionNotFoundException {
        if (!this.divisions.containsKey(name)) {
            throw new DivisionNotFoundException("Division not found: " + name);
        }
        List<Device> devicesToRemove = this.divisions.get(name);
        if (devicesToRemove != null) {
            for (Device device : devicesToRemove) {
                this.devices.remove(device.getId());
            }
        }
        this.divisions.remove(name);
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
     * Returns a map of users that have access to this house and their respective roles.
     *
     * @return A map where the key is the user's id and the value is their role in the house.
     */
    public Map<Integer, UserRole> getUserRoles() {
        return new HashMap<>(this.usersInHouse);
    }

    /**
     * Assigns a user to this house with a specific role.
     *
     * @param userId The ID of the user to be assigned.
     * @param role   The role to assign to the user in this house.
     * @throws UserNotFoundException      if the user does not exist.
     * @throws UserAlreadyExistsException if the user is already assigned to this house.
     */
    public void assignUser(int userId, UserRole role) throws UserNotFoundException, UserAlreadyExistsException {
        if (this.usersInHouse.containsKey(userId)) {
            throw new UserAlreadyExistsException("User with ID " + userId + " is already in this house.");
        }
        this.usersInHouse.put(userId, role);
    }

    /**
     * Removes a user from this house based on their ID.
     *
     * @param userId The ID of the user to be removed.
     * @throws UserNotFoundException if the user is not found in the house.
     */
    public void removeUser(int userId) throws UserNotFoundException {
        if (!this.usersInHouse.containsKey(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        this.usersInHouse.remove(userId);
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
        return Objects.equals(this.getName(), h.getName()) &&
               this.getId() == h.getId() &&
               this.getDivisions().equals(h.getDivisions()) &&
               this.getDevices().equals(h.getDevices()) &&
               Objects.equals(this.getRoutineFacade(), h.getRoutineFacade()) &&
               Objects.equals(this.getInteractionLogger(), h.getInteractionLogger());
    }

    /**
     * Generates a hash code for this house.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getName(), this.getDivisions(),
                            this.getDevices(), this.getRoutineFacade(), this.getInteractionLogger());
    }

    /**
     * Simulates the passing of time for the house. Updates device logic and checks
     * all automations and schedules.
     *
     * @param minutes The number of minutes elapsed since the last tick.
     */
    public void tick(int minutes) {
        this.devices.values().forEach(device -> device.tick(minutes));
        this.routineFacade.tick();
    }

    /**
     * Identifies the top 3 devices based on total usage time.
     *
     * @return A list containing the top 3 devices by minutes on.
     */
    public List<Device> top3DevicesTimeConsumption() {
        return this.devices.values().stream()
            .sorted(Comparator.comparingInt(Device::getTotalMinutesOn).reversed())
            .limit(3)
            .collect(Collectors.toList());
    }

    /**
     * Identifies the top 3 devices based on activation count.
     *
     * @return A list containing the top 3 devices by number of times turned on.
     */
    public List<Device> top3DevicesTurnedOnTimes() {
        return this.devices.values().stream()
            .sorted(Comparator.comparingInt(Device::getTotalActivations).reversed())
            .limit(3)
            .map(Device::clone)
            .collect(Collectors.toList());
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
     * Identifies the top 3 divisions containing the most devices.
     *
     * @return A list containing the names of the top 3 divisions.
     */
    public List<String> top3DivisionsWithMostDevices() {
        return this.divisions.entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, List<Device>>>comparingInt(e -> e.getValue().size()).reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Adds a device to a specific division. If the device is not in the global map, it is added.
     *
     * @param device   The device to add.
     * @param division The name of the division.
     * @throws DivisionNotFoundException if the division does not exist.
     */
    public void addDeviceToDivision(Device device, String division) throws DivisionNotFoundException {
        if (!this.divisions.containsKey(division)) throw new DivisionNotFoundException("Division not found: " + division);
        Device device2 = this.devices.computeIfAbsent(device.getId(), k -> device.clone());
        List<Device> list_dev = this.divisions.get(division);
        if (list_dev.stream().noneMatch(d -> d.getId() == device.getId())) {
            list_dev.add(device2);
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
        if (!this.devices.containsKey(deviceId)) throw new DeviceNotFoundException("Device not found: " + deviceId);
        this.devices.remove(deviceId);
        this.divisions.values().forEach(list -> list.removeIf(d -> d.getId() == deviceId));
        this.routineFacade.removeDevice(deviceId);
    }

    /**
     * Removes a specific device from a designated division.
     *
     * @param deviceId The unique identifier of the device to remove from the division.
     * @param division The name of the division from which the device should be removed.
     * @throws DivisionNotFoundException if the specified division does not exist in the house.
     */
    public void removeDeviceFromDivision(int deviceId, String division) throws DivisionNotFoundException {
        if (!this.divisions.containsKey(division)) throw new DivisionNotFoundException("Division not found: " + division);
        List<Device> list_dev = this.divisions.get(division);
        list_dev.removeIf(d -> d.getId() == deviceId);
    }

    /**
     * Updates a device's information globally and within its respective division.
     *
     * @param device The device with updated data.
     * @throws DeviceNotFoundException if the device ID is not recognized.
     */
    public void updateDevice(Device device) throws DeviceNotFoundException {
        if (!this.devices.containsKey(device.getId())) throw new DeviceNotFoundException("Device not found: " + device.getId());
        Device updated = device.clone();
        this.devices.put(device.getId(), updated);
        this.divisions.forEach((name, list) -> {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == device.getId()) {
                    list.set(i, updated);
                    break;
                }
            }
        });
    }

    /**
     * Retrieves the live reference to a device by its ID.
     * This returns the actual object pointer, not a clone, and is intended
     * to be used when constructing Actions and Conditions.
     *
     * @param deviceId The unique identifier of the device.
     * @return The live Device object.
     * @throws DeviceNotFoundException if the device ID does not exist in the house.
     */
    public Device getDevice(int deviceId) throws DeviceNotFoundException {
        Device device = this.devices.get(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("Device with ID " + deviceId + " not found in the house.");
        }
        return device;
    }

    /**
     * Safely interacts with a specific device using a functional consumer.
     *
     * @param targetId    The ID of the device to interact with.
     * @param interaction The logic to apply to the device.
     * @throws DeviceNotFoundException if the device is not found.
     */
    public void interactWithDevice(int targetId, Consumer<Device> interaction) throws DeviceNotFoundException {
        Device realDevice = this.devices.get(targetId);
        if (realDevice != null) {
            interaction.accept(realDevice);
        } else {
            throw new DeviceNotFoundException("Device not found: " + targetId);
        }
    }

    /**
     * Reads a specific property or state from a device using a provided function.
     *
     * @param <T>      The return type of the property being read.
     * @param targetId The unique identifier of the target device.
     * @param reader   A function that extracts the desired data from the device.
     * @return The data extracted from the device.
     * @throws DeviceNotFoundException if no device with the given ID exists.
     */
    public <T> T readDevice(int targetId, Function<Device, T> reader) throws DeviceNotFoundException {
        Device realDevice = this.devices.get(targetId);
        if (realDevice == null)
            throw new DeviceNotFoundException("Device not found: " + targetId);
        return reader.apply(realDevice);
    }

    /**
     * Adds a new scenario for a specific user to the routine facade.
     *
     * @param userId The ID of the user owning the scenario.
     * @param s      The scenario to be added.
     * @throws NameAlreadyExistsException if a scenario with the same name already exists for this user.
     */
    public void addScenario(int userId, Scenario s) throws NameAlreadyExistsException {
        this.routineFacade.addScenario(userId, s);
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
        this.routineFacade.executeScenarioByName(userId, name);
    }

    /**
     * Adds a new automation to the routine facade.
     *
     * @param a The automation to be added.
     * @throws NameAlreadyExistsException if an automation with the same name already exists.
     */
    public void addAutomation(Automation a) throws NameAlreadyExistsException {
        this.routineFacade.addAutomation(a);
    }

    /**
     * Removes an automation from the routine facade by its name.
     *
     * @param name The name of the automation to remove.
     * @throws AutomationDoesntExistException if the specified automation does not exist.
     */
    public void removeAutomation(String name) throws AutomationDoesntExistException {
        this.routineFacade.removeAutomation(name);
    }

    /**
     * Removes a scenario from the routine facade for a specific user.
     *
     * @param userId The ID of the user who owns the scenario.
     * @param name   The name of the scenario to remove.
     * @throws UserDoesntHaveScenarios      if the user has no registered scenarios.
     * @throws ScenarioDoesntExistException if the specified scenario does not exist.
     */
    public void removeScenario(int userId, String name) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        this.routineFacade.removeScenario(userId, name);
    }

}