package domuscontrol.model.houses;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.model.device.Device;
import domuscontrol.model.routines.Automation;
import domuscontrol.model.suggestions.AutomationSuggestion;
import domuscontrol.model.suggestions.DeviceInteraction;

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
    public House createHouse(String name) throws HouseAlreadyExistsException{
        House newHouse = new House();
        newHouse.setName(name);

        this.addHouse(newHouse);
        return newHouse;
    }

    /** Adds a house to the system. Clones the house to ensure encapsulation. */
    private void addHouse(House house) throws HouseAlreadyExistsException{
        if (this.housesById.containsKey(house.getId())) {
            throw new HouseAlreadyExistsException("House ID already exists: " + house.getId());
        }
        this.housesById.put(house.getId(), house);
    }

    /** Retrieves a clone of a house by its unique ID. */
    public House getHouseById(int id) throws HouseNotFoundException{
        House house = this.housesById.get(id);
        if (house == null) {
            throw new HouseNotFoundException("House not found with ID: " + id);
        }
        return house.clone();
    }

    /** Updates an existing house in the system. */
    public void updateHouse(House updatedHouse) throws HouseNotFoundException{
        if (!this.housesById.containsKey(updatedHouse.getId())) {
            throw new HouseNotFoundException("Cannot update: House does not exist.");
        }
        this.housesById.put(updatedHouse.getId(), updatedHouse.clone());
    }

    /**
     * Adds a new division to the specified house.
     * @param houseId The ID of the house to which the division will be added.
     * @param divisionName The name of the new division.
     * @throws HouseNotFoundException if no house with the given ID exists.
     */
    public void addDivision(int houseId, String divisionName) throws HouseNotFoundException {
        House h = getHouseInternal(houseId);
        h.addDivision(divisionName, null);
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
     * Retrieves a device by its ID from a specific house. The search is performed across all divisions of the house.
     * @param houseId The ID of the house from which to retrieve the device.
     * @param deviceId The ID of the device to retrieve.
     * @return The device if found.
     * @throws HouseNotFoundException if no house with the given ID exists.
     * @throws DeviceNotFoundException if no device with the given ID exists in the specified house.
     */
    public Device getDevice(int houseId, int deviceId) throws HouseNotFoundException, DeviceNotFoundException {
        House h = getHouseInternal(houseId);
        Device d = h.getDevices().get(deviceId);
        if (d == null) throw new DeviceNotFoundException("Device not found: " + deviceId);
        return d;
    }

    /** 
     * Updates the information of a device in a specific house. The device is updated in the global devices map and also in any division that contains it.
      * @param houseId The ID of the house in which the device is located.
      * @param device The device with updated information to be stored in the house.
      * @throws HouseNotFoundException if no house with the given ID exists.
      * @throws DeviceNotFoundException if the device is not found in the specified house.
     */
    public void updateDevice(int houseId, Device device) throws HouseNotFoundException, DeviceNotFoundException {
        House h = getHouseInternal(houseId);
        h.updateDevice(device);
    }

    /**
     * Applies an interaction to a live device inside the house without exposing the reference.
     * The device's observer is notified automatically during the interaction.
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

    /**
     * Advances time for all houses in the system.
     * This cascades down to every device in every division.
     */
    public List<String> tick(int minutes) {
        List<String> activated = new ArrayList<>();

        for (House house : this.housesById.values()) {
            List<String> houseActivated = house.tick(minutes);

            for (String automationName : houseActivated) {
                activated.add(house.getName() + ": " + automationName);
            }
        }

        return activated;
    }

    /** Returns the house with the highest energy consumption. */
    public House getMostConsumingHouse() {
        return this.housesById.values().stream()
                .max((h1, h2) -> Double.compare(h1.calculateTotalConsumption(), h2.calculateTotalConsumption()))
                .map(House::clone)
                .orElse(null);
    }

    /** Returns a list of all houses in the system. */
    public List<House> getAllHouses() {
        return this.housesById.values().stream()
                .map(House::clone)
                .collect(Collectors.toList());
    }

    public void logInteraction(int houseId, DeviceInteraction interaction) throws HouseNotFoundException {
        getHouseInternal(houseId).logInteraction(interaction);
    }

    public List<AutomationSuggestion> getSuggestions(int houseId, int userId) throws HouseNotFoundException, ScheduleWithConditionDifferentFromTimeException {
        return getHouseInternal(houseId).getSuggestions(userId);
    }

    public void addAutomation(int houseId, Automation automation) throws HouseNotFoundException, NameAlreadyExistsException {
        getHouseInternal(houseId).addAutomation(automation);
    }

    public void removeAutomation(int houseId, String name) throws HouseNotFoundException, domuscontrol.exceptions.AutomationDoesntExistException {
        getHouseInternal(houseId).removeAutomation(name);
    }

    public void addScenario(int houseId, int userId, domuscontrol.model.routines.Scenario scenario) throws HouseNotFoundException, NameAlreadyExistsException {
        getHouseInternal(houseId).addScenario(userId, scenario);
    }

    public void removeScenario(int houseId, int userId, String name) throws HouseNotFoundException, domuscontrol.exceptions.UserDoesntHaveScenarios, domuscontrol.exceptions.ScenarioDoesntExistException {
        getHouseInternal(houseId).removeScenario(userId, name);
    }

    public void executeScenario(int houseId, int userId, String name) throws HouseNotFoundException, domuscontrol.exceptions.UserDoesntHaveScenarios, domuscontrol.exceptions.ScenarioDoesntExistException {
        getHouseInternal(houseId).executeScenario(userId, name);
    }

    /** Returns the live (non-cloned) House reference for internal mutation. */
    private House getHouseInternal(int houseId) throws HouseNotFoundException {
        House h = this.housesById.get(houseId);

        if (h == null) throw new HouseNotFoundException("House not found with ID: " + houseId);
        return h;
    }
}