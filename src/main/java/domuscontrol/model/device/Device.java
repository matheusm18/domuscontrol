package domuscontrol.model.device;

import java.util.Objects;

/**
 * Represents a generic device of the DomusControl application.
 */
public abstract class Device {

    private static int nextId = 1;

    /** The unique identifier for the device. */
    private final int id;

    /** The brand of the device. */
    private String brand;

    /** The model of the device. */
    private String model;

    /** The power consumption of the device in Wh/h. */
    private double consumptionPerHour;

    /** The status of the device (true if on, false if off). */
    private boolean on;

    /** The total number of minutes the device has been on. */
    private int totalMinutesOn;

    /** The total number of times the device has been activated. */
    private int totalActivations;

    /**
     * Sets the next id to be assigned to a device. 
     * This method is used when loading devices from a file to ensure that the next id is greater than the last id of the loaded devices.
     * 
     * @param lastId
     */
    public static void setNextId(int lastId) {
        nextId = lastId + 1;
    }

    /** Creates a new empty device. */
    public Device() {
        this.id = nextId++;
        this.brand = "";
        this.model = "";
        this.consumptionPerHour = 0.0;
        this.on = false;
        this.totalMinutesOn = 0;
        this.totalActivations = 0;
    }
    
    /** Creates a device from the value of its fields. 
     * 
     * @param brand the brand of the device.
     * @param model the model of the device.
     * @param consumptionPerHour The power consumption rate of the device in Wh/h.
    */
    public Device(String brand, String model, double consumptionPerHour) {
        this.id = nextId++;
        this.brand = brand;
        this.model = model;
        this.consumptionPerHour = consumptionPerHour;
        this.on = false;
        this.totalMinutesOn = 0;
        this.totalActivations = 0;
    }

    /** Copy constructor of a device.
     * 
     * @param device the device to copy.
     */
    public Device(Device device) {
        this.id = device.getId();
        this.brand = device.getBrand();
        this.model = device.getModel();
        this.consumptionPerHour = device.getConsumptionPerHour();
        this.on = device.isOn();
        this.totalMinutesOn = device.getTotalMinutesOn();
        this.totalActivations = device.getTotalActivations();
    }

    /**
     * Gets the unique identifier of the device.
     * 
     * @return This device's unique identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the brand of the device.
     * 
     * @return This device's brand.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Gets the model of the device.
     * 
     * @return This device's model.
     */
    public String getModel(){
        return model;
    }

    /**
     * Gets the power consumption rate of the device.
     * 
     * @return This device's power consumption in Wh/h.
     */
    public double getConsumptionPerHour(){
        return consumptionPerHour;
    }

    /**
     * Checks if the device is on.
     * 
     * @return true if the device is on, false otherwise.
     */
    public boolean isOn(){
        return on;
    }

    /**
     * Gets the total number of minutes the device has been on.
     * 
     * @return This device's total minutes on.
     */
    public int getTotalMinutesOn() {
        return totalMinutesOn;
    }

    /**
     * Gets the total number of times the device has been activated.
     * 
     * @return This device's total activations.
     */
    public int getTotalActivations() {
        return totalActivations;
    }

    /**
     * Calculates the total accumulated energy consumption of the device.
     * 
     * @return This device's total energy consumption in watt-hours (Wh).
     */
    public double getEnergyConsumption() {
        return (this.totalMinutesOn / 60.0) * this.consumptionPerHour;
    }

    /**
     *  Sets the brand of the device.
     * 
     * @param brand The brand of the device.
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Sets the model of the device.
     * 
     * @param model The model of the device.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Sets the power consumption rate of the device.
     * 
     * @param consumptionPerHour The power consumption rate of the device in Wh/h.
     */
    public void setConsumptionPerHour(double consumptionPerHour) {
        this.consumptionPerHour = consumptionPerHour;
    }

    /**
     * Turns the device on. If the device is already on, this method does nothing. 
     * Otherwise, it sets the status to on and increments the total activations by 1.
     */
    public void turnOn() {
        if (!on) {
            on = true;
            totalActivations++;
        }
    }

    /**
     * Turns the device off. If the device is already off, this method does nothing
     */
    public void turnOff() {
        if (on) {
            on = false;
        }
    }

    /**
     * Updates the total minutes on of the device. If the device is on, it increments the total minutes on by the given number of minutes.
     * 
     * @param minutes The number of minutes to be added to the total minutes on if the device is on.
     */
    public void tick(int minutes) {
        if (on) totalMinutesOn += minutes;
    }

    /**
     * Resets the usage statistics of the device (minutes on and activations).
     * This does not affect the device's ID, brand, or consumption rate.
     */
    public void resetStats() {
        this.totalMinutesOn = 0;
        this.totalActivations = 0;
    }

    /**
     * Checks if this device is equal to another object.
     * 
     * @param o Object to be compared with this device.
     * @return true if the given object is a device with the same id as this device, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || o.getClass() != this.getClass()) return false;

        Device d = (Device) o;
        return d.getId() == this.id;
    }

    /**
     * Calculates the hash code of this device. Only the device's id is used to calculate the hash code.
     * 
     * @return The hash code of this device.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Creates a copy of this device.
     * 
     * @return A copy of this device.
     */
    @Override
    public abstract Device clone();

    /**
     * Creates a string representation of this device. 
     * The string representation includes the class name, id, brand, model, power consumption, and status of the device.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append(" [ ID: ").append(this.getId()).append(" ] ")
            .append("| Brand: ").append(this.getBrand())
            .append(" | Model: ").append(this.getModel())
            .append(" | Consumption: ").append(this.getConsumptionPerHour()).append(" Wh/h")
            .append(" | Status: ").append(this.isOn() ? "ON" : "OFF");
        return sb.toString();
    }
}