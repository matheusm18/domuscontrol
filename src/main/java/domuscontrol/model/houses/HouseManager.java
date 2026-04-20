package domuscontrol.model.houses;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;

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
     * Advances time for all houses in the system.
     * This cascades down to every device in every division.
     */
    public void tick(int minutes) {
        this.housesById.values().forEach(house -> house.tick(minutes));
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
}