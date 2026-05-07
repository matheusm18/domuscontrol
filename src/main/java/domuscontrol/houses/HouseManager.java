package domuscontrol.houses;

import domuscontrol.simulation.Simulation;
import domuscontrol.suggestions.AutomationSuggestion;
import domuscontrol.suggestions.DeviceInteraction;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.devices.Device;
import domuscontrol.exceptions.AutomationDoesntExistException;
import domuscontrol.exceptions.ScenarioDoesntExistException;
import domuscontrol.exceptions.UserDoesntHaveScenarios;
import domuscontrol.routines.Automation;
import domuscontrol.routines.Scenario;

import java.util.function.Consumer;

/**
 * Manager responsible for handling all houses in the system.
 * Centralizes storage, retrieval, and global simulation logic.
 */
public class HouseManager implements Serializable {

    private final Map<Integer, House> housesById;

    public HouseManager() {
        this.housesById = new HashMap<>();
    }

    /** Creates a new house with the given name and registers it in the system. */
    public House createHouse(String name) throws HouseAlreadyExistsException {
        House newHouse = new House();
        newHouse.setName(name);
        if (this.housesById.containsKey(newHouse.getId())) {
            throw new HouseAlreadyExistsException("" + newHouse.getId());
        }
        this.housesById.put(newHouse.getId(), newHouse);
        return newHouse;
    }

    /**
     * Checks whether a house with the given ID is registered in the system.
     * @param id The house ID to look up.
     * @return true if a house with that ID exists, false otherwise.
     */
    public boolean existsHouseWithId(int id) {
        return this.housesById.containsKey(id);
    }

    /** Retrieves a clone of a house by its unique ID. */
    public House getHouseById(int id) throws HouseNotFoundException {
        House house = this.housesById.get(id);
        if (house == null) {
            throw new HouseNotFoundException("" + id);
        }
        return house.clone();
    }

    /**
     * Adds a new division to the specified house.
     * @param houseId The ID of the house to which the division will be added.
     * @param divisionName The name of the new division.
     * @throws HouseNotFoundException if no house with the given ID exists.
     */
    public void addDivision(int houseId, String divisionName) throws HouseNotFoundException {
        House h = getHouseInternal(houseId);
        h.addDivision(divisionName);
    }

    /**
     * Removes a division from the specified house.
     * @param houseId The ID of the house from which the division will be removed.
     * @param divisionName The name of the division to be removed.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DivisionNotFoundException if no division with the given name exists in the specified house.
     */
    public void removeDivision(int houseId, String divisionName) throws HouseNotFoundException, DivisionNotFoundException {
        House h = getHouseInternal(houseId);
        h.deleteDivision(divisionName);
    }

    /**
     * Adds a device to a specific division within a house.
     * @param houseId The ID of the house to which the device will be added.
     * @param device The device to be added.
     * @param division The name of the division to which the device will be added.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DivisionNotFoundException if no division with the given name exists in the specified house.
     */
    public void addDeviceToDivision(int houseId, Device device, String division) throws HouseNotFoundException, DivisionNotFoundException {
        House h = getHouseInternal(houseId);
        h.addDeviceToDivision(device, division);
    }

    /**
     * Retrieves a device by its ID from a specific house.
     * @param houseId The ID of the house from which to retrieve the device.
     * @param deviceId The ID of the device to retrieve.
     * @return A cloned Device if found.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DeviceNotFoundException if no device with the given ID exists in the specified house.
     */
    public Device getDevice(int houseId, int deviceId) throws HouseNotFoundException, DeviceNotFoundException {
        return getHouseInternal(houseId).getDevice(deviceId);
    }

    public List<Device> getAllDevices() {
        return this.housesById.values().stream()
                .flatMap(house -> house.getDevices().values().stream())
                .collect(Collectors.toList());
    }

    /**
     * Applies a consumer to a live device inside the specified house without exposing the reference.
     * @param houseId The ID of the house containing the device.
     * @param deviceId The ID of the device to interact with.
     * @param interaction The logic to apply to the device.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DeviceNotFoundException if no device with the given ID exists in the specified house.
     */
    public void interactWithDevice(int houseId, int deviceId, Consumer<Device> interaction) throws HouseNotFoundException, DeviceNotFoundException {
        House h = getHouseInternal(houseId);
        h.interactWithDevice(deviceId, interaction);
    }

    /**
     * Removes a device from a specific house. The device is removed from the global devices map and also from any division that contains it.
     * @param houseId The ID of the house from which the device will be removed.
     * @param deviceId The ID of the device to be removed.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DeviceNotFoundException if no device with the given ID exists in the specified house.
     */
    public void removeDevice(int houseId, int deviceId) throws HouseNotFoundException, DeviceNotFoundException {
        House h = getHouseInternal(houseId);
        h.removeDevice(deviceId);
    }

    /** Returns the house with the highest energy consumption. */
    public House getMostConsumingHouse() {
        return this.housesById.values().stream()
                .max(Comparator.comparingDouble(House::calculateTotalConsumption))
                .map(House::clone)
                .orElse(null);
    }

    /** Returns a list of all houses in the system. */
    public List<House> getAllHouses() {
        return this.housesById.values().stream()
                .map(House::clone)
                .collect(Collectors.toList());
    }

    /**
     * Records a device interaction in the specified house's interaction history.
     * @param houseId The ID of the house.
     * @param interaction The interaction to record.
     * @throws HouseNotFoundException if no house with the given ID exists.
     */
    public void logInteraction(int houseId, DeviceInteraction interaction) throws HouseNotFoundException {
        getHouseInternal(houseId).logInteraction(interaction);
    }

    /**
     * Returns automation suggestions based on the interaction history of the specified house and user.
     * @param houseId The ID of the house.
     * @param userId The ID of the user to generate suggestions for.
     * @return A list of automation suggestions.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws ScheduleWithConditionDifferentFromTimeException if a suggested schedule has a non-time condition.
     */
    public List<AutomationSuggestion> getSuggestions(int houseId, int userId) throws HouseNotFoundException, ScheduleWithConditionDifferentFromTimeException {
        return getHouseInternal(houseId).getSuggestions(userId);
    }

    /**
     * Adds an automation to the specified house.
     * @param houseId The ID of the house.
     * @param automation The automation to add.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws NameAlreadyExistsException if an automation with the same name already exists.
     */
    public void addAutomation(int houseId, Automation automation) throws HouseNotFoundException, NameAlreadyExistsException {
        getHouseInternal(houseId).addAutomation(automation);
    }

    /**
     * Removes an automation from the specified house by name.
     * @param houseId The ID of the house.
     * @param name The name of the automation to remove.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws AutomationDoesntExistException if no automation with the given name exists.
     */
    public void removeAutomation(int houseId, String name) throws HouseNotFoundException, AutomationDoesntExistException {
        getHouseInternal(houseId).removeAutomation(name);
    }

    /**
     * Returns all automations registered in the specified house.
     * @param houseId The ID of the house.
     * @return A list of automations.
     * @throws HouseNotFoundException if no house with the given ID exists.
     */
    public List<Automation> getAutomations(int houseId) throws HouseNotFoundException {
        return getHouseInternal(houseId).getRoutineManager().getOnlyAutomations();
    }

    /**
     * Returns all schedules registered in the specified house.
     * @param houseId The ID of the house.
     * @return A list of schedules.
     * @throws HouseNotFoundException if no house with the given ID exists.
     */
    public List<Automation> getSchedules(int houseId) throws HouseNotFoundException {
        return getHouseInternal(houseId).getRoutineManager().getOnlySchedules();
    }

    /**
     * Adds a scenario for a specific user in the specified house.
     * @param houseId The ID of the house.
     * @param userId The ID of the user owning the scenario.
     * @param scenario The scenario to add.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws NameAlreadyExistsException if a scenario with the same name already exists for this user.
     */
    public void addScenario(int houseId, int userId, Scenario scenario) throws HouseNotFoundException, NameAlreadyExistsException {
        getHouseInternal(houseId).addScenario(userId, scenario);
    }

    /**
     * Returns all scenarios for a specific user in the specified house.
     * @param houseId The ID of the house.
     * @param userId The ID of the user.
     * @return A list of scenarios.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws UserDoesntHaveScenarios if the user has no registered scenarios.
     */
    public List<Scenario> getScenarios(int houseId, int userId) throws HouseNotFoundException, UserDoesntHaveScenarios {
        return getHouseInternal(houseId).getRoutineManager().getScenariosForUser(userId);
    }

    /**
     * Removes a scenario for a specific user in the specified house.
     * @param houseId The ID of the house.
     * @param userId The ID of the user owning the scenario.
     * @param name The name of the scenario to remove.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws UserDoesntHaveScenarios if the user has no registered scenarios.
     * @throws ScenarioDoesntExistException if no scenario with the given name exists.
     */
    public void removeScenario(int houseId, int userId, String name) throws HouseNotFoundException, UserDoesntHaveScenarios, ScenarioDoesntExistException {
        getHouseInternal(houseId).removeScenario(userId, name);
    }

    /**
     * Executes a scenario for a specific user in the specified house.
     * @param houseId The ID of the house.
     * @param userId The ID of the user triggering the scenario.
     * @param name The name of the scenario to execute.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws UserDoesntHaveScenarios if the user has no registered scenarios.
     * @throws ScenarioDoesntExistException if no scenario with the given name exists.
     */
    public void executeScenario(int houseId, int userId, String name) throws HouseNotFoundException, UserDoesntHaveScenarios, ScenarioDoesntExistException {
        getHouseInternal(houseId).executeScenario(userId, name);
    }
    
    /**
     * Advances time for all houses in the system.
     * This cascades down to every device in every division.
     */
    public List<String> tick(Simulation simulation) {
        List<String> activated = new ArrayList<>();

        for (House house : this.housesById.values()) {
            List<String> houseActivated = house.tick(simulation);

            for (String automationName : houseActivated) {
                activated.add(house.getName() + ": " + automationName);
            }
        }

        return activated;
    }

    /** Returns the live (non-cloned) House reference for internal mutation. */
    private House getHouseInternal(int houseId) throws HouseNotFoundException {
        House h = this.housesById.get(houseId);

        if (h == null) throw new HouseNotFoundException("" + houseId);
        return h;
    }
}
