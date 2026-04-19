package domuscontrol.model;

import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.model.device.Device;

import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a complete House in the Domus Control automation system.
 * A House aggregates multiple Divisions (rooms), managing the overall state, 
 * simulating the passage of time, and calculating global statistics.
 */
public class House {
    
    /**
     * Static counter used to automatically assign unique, sequential IDs to each new house.
     */
    private static int nextId = 1; 

    /**
     * The name or designation of the house (e.g., "Main Residence", "Beach House").
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
     * Updates the static ID counter. 
     * Useful when loading a saved system state to ensure new houses do not overlap 
     * with previously assigned IDs.
     *
     * @param lastId The highest ID currently loaded in the system.
     */
    public static void setNextId(int lastId){
        nextId = lastId + 1;
    }

    /**
     * Default constructor.
     * Initializes a new house with a unique ID, an empty name, and no divisions or devices.
     */
    public House() {
        this.id = House.nextId++;
        this.name = ""; 
        this.divisions = new HashMap<>();
        this.devices = new HashMap<>(); 
    }

    /**
     * Parameterized constructor.
     * Initializes a new house with a unique ID, a specific name, and a predefined map of divisions and devices.
     *
     * @param divisions A map of divisions to populate the house (lists will be copied).
     * @param devices   A map of devices to populate the house.
     * @param name      The name of the house.
     */
    public House(Map<String, List<Device>> divisions, Map<Integer, Device> devices, String name) {
        this.id = House.nextId++;
        this.name = name; 
        this.setDevices(devices); 
        this.setDivisions(divisions); 
    }

    /**
     * Copy constructor.
     * Creates a new House instance by copying the state and retaining the exact ID 
     * of an existing House.
     *
     * @param h The House object to copy.
     */
    public House(House h) {
        this.id = h.getId(); 
        this.name = h.getName(); 
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
     * Sets the devices for this house.
     *
     * @param devices A map of devices to be added to this house.
     */
    public void setDevices(Map<Integer, Device> devices) {
        Map<Integer, Device> devicesMap = new HashMap<>(); 
        if (devices != null) {
            devices.forEach((k, v) -> devicesMap.put(k, v.clone())); // Deep copy device
        }
        this.devices = devicesMap; 
    }

    /**
     * Sets the divisions for this house by creating a copy of the provided map's lists.
     * Devices inside the divisions map share the same memory addresses as those in the devices map.
     *
     * @param divisions A map of divisions to be added to this house.
     */
    public void setDivisions(Map<String, List<Device>> divisions) {
        Map<String, List<Device>> divisionsMap = new HashMap<>(); 
        if (divisions != null) {
            divisions.forEach((name, list) -> {
                // Find deep copied devices using the device's ID from this.devices
                divisionsMap.put(name, list.stream()
                    .map(dev -> this.devices.get(dev.getId()))
                    .collect(Collectors.toList()));
            });
        }
        this.divisions = divisionsMap; 
    }

    /**
     * Retrieves all devices currently in this house.
     * Returns a map containing the devices.
     *
     * @return A map containing the devices.
     */
    public Map<Integer, Device> getDevices() {
        Map<Integer, Device> newMap = new HashMap<>(); 
        this.devices.forEach((k, v) -> newMap.put(k, v.clone()));
        return newMap; 
    }

    /**
     * Retrieves all divisions currently in this house.
     * Returns a copy of the internal map.
     *
     * @return A map containing the divisions and their devices.
     */
    public Map<String, List<Device>> getDivisions() {
        Map<String, List<Device>> newMap = new HashMap<>(); 
        this.divisions.forEach((name, list) -> {
            newMap.put(name, list.stream().map(Device :: clone).collect(Collectors.toList()));
        });
        return newMap; 
    }

    /**
     * Adds a single division to the house. 
     * The division is cloned before insertion to preserve encapsulation.
     *
     * @param name The name of the division.
     * @param devices The list of devices in the division.
     */
    public void addDivision(String name, List<Device> devices) {
        if (devices == null) {
            this.divisions.put(name, new ArrayList<>());
        } else {
            // Adds devices using the existing references from this.devices (same address)
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
     */
    public void deleteDivision(String name) {
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
     * @return A new House object with identical properties and divisions.
     */
    @Override
    public House clone() {
        return new House(this); 
    }

    /**
     * Returns a formatted string representation of the house and all its divisions.
     *
     * @return A multiline string detailing the house and its contents.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(); 
        str.append("=== House: ").append(this.name).append(" [ID: ").append(this.id).append("] ===\n");
        
        if (this.divisions.isEmpty()) {
            str.append("No divisions in this house yet.\n");
        } else {
            this.divisions.forEach((name, list) -> {
                str.append("Division: ").append(name).append("\n");
                list.forEach(dev -> str.append("  ").append(dev.toString()).append("\n"));
            }); 
        }
        return str.toString(); 
    }

    /**
     * Compares this house to another object for logical equality.
     * Two houses are equal if they share the same ID, name, and exact division configurations.
     *
     * @param o The object to compare with this house.
     * @return true if the objects are logically equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false; 
        
        House h = (House) o; 
        return Objects.equals(this.name, h.getName()) &&
               this.id == h.getId() && 
               this.divisions.equals(h.getDivisions()) &&
               this.devices.equals(h.getDevices());
    }

    /**
     * Generates a hash code for this house based on its properties, divisions, and devices.
     *
     * @return The hash code integer.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.divisions, this.devices); 
    }

    /**
     * Simulates the passing of time for the entire house, cascading the time update
     * down to every division and their respective devices.
     *
     * @param minutes The number of minutes that have elapsed in the simulation.
     */
    public void tick(int minutes){
        this.devices.values().forEach(device -> device.tick(minutes)); 
    }

    /**
     * Identifies the top 3 devices in the entire house based on their total time turned on.
     * Sorts all the devices to find the ones with the highest usage time.
     *
     * @return A list containing the top 3 devices with the highest usage time.
     */
    public List<Device> top3DevicesTimeConsumption(){
        return this.devices.values().stream()
            .sorted(Comparator.comparingInt(Device::getTotalMinutesOn).reversed())
            .limit(3)
            .collect(Collectors.toList());
    }

    /**
     * Identifies the top 3 devices in the entire house based on their total number of activations.
     * Sorts all the devices to find the ones that were turned on the most times.
     *
     * @return A list containing the top 3 devices that were turned on the most times.
     */
    public List<Device> top3DevicesTurnedOnTimes(){
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
    public int devicesNumber(){
        return this.devices.size();
    }

    /**
     * Identifies the top 3 divisions in the house that contain the highest number of devices.
     *
     * @return A list containing the names of the top 3 most populated divisions.
     */
    public List<String> top3DivisionsWithMostDevices(){
        return this.divisions.entrySet().stream()
        .sorted(Comparator.<Map.Entry<String, List<Device>>>comparingInt(e -> e.getValue().size()).reversed())
        .limit(3)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList()); 
    }

    public void addDeviceToDivision(Device device, String division){
        Device device2 = this.devices.computeIfAbsent(device.getId(), k-> device.clone());
        // computeIfAbsent does the proccess of verification if the key exists and return the value or the result of the argument function (and also inserts the value)
        List<Device> list_dev = this.divisions.computeIfAbsent(division, k -> new ArrayList<>());
        if(!list_dev.stream().anyMatch(d -> d.getId() == device.getId())){
            list_dev.add(device2);
        }
    }
}