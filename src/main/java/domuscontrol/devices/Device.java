package domuscontrol.devices;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a generic device in the DomusControl home automation system.
 * This is the base class for all smart devices, providing core functionality for
 * device management, status tracking, and energy consumption monitoring.
 * Devices can be either in ON/OFF or OPEN/CLOSED states depending on their type.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public abstract class Device implements Serializable{

    /** Counter for automatically assigning unique device IDs. */
    private static int nextId = 1;

    /** The unique identifier for the device. */
    private final int id;

    /** The brand of the device. */
    private String brand;

    /** The model of the device. */
    private String model;

    /** The power consumption of the device in Wh/h. */
    private double consumptionPerHour;

    /** The status of the device */
    private DeviceStatus status;

    /** The total number of minutes the device has been on. */
    private int totalMinutesOn;

    /** The total number of times the device has been activated. */
    private int totalActivations;


    /**
     * Sets the next id to be assigned to a device. 
     * This method is used when loading devices from a file to ensure that the next id is greater than the last id of the loaded devices.
     * 
     * @param lastId The last id of the loaded devices.
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
        this.status = DeviceStatus.OFF;
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
        this.status = DeviceStatus.OFF;
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
        this.status = device.getStatus();
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
     * Gets the status of the device.
     * 
     * @return This device's status (ON or OFF).
     */
    public DeviceStatus getStatus(){
        return status;
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
     * Updates the device status and safely increments activations 
     * if changing from an idle state to an active state.
     * 
     * @param newStatus The new status to apply.
     */
    public void updateStatus(DeviceStatus newStatus) {
        boolean wasIdle = (this.status == DeviceStatus.OFF || this.status == DeviceStatus.CLOSED);
        boolean willBeActive = (newStatus == DeviceStatus.ON || newStatus == DeviceStatus.OPEN);

        if (wasIdle && willBeActive) {
            this.totalActivations++;
        }
        this.status = newStatus;
    }

    /**
     * Abstract method that forces subclasses to define if they are 
     * currently in a state that counts as "active" or "consuming".
     * 
     * @return true if the device is actively consuming time/energy.
     */
    public abstract boolean isConsuming();

    /**
     * Updates the total minutes on of the device. If the device is consuming, it increments the total minutes on by the given number of minutes.
     * 
     * @param minutes The number of minutes to be added to the total minutes on if the device is consuming.
     */
    public void tick(int minutes) {
        if (this.isConsuming()) {
            totalMinutesOn += minutes;
        }
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
     * @return true if the given object is a device with the same fields as this device, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || o.getClass() != this.getClass()) return false;

        Device d = (Device) o;
        return d.getId() == this.id && d.getBrand().equals(this.brand) && d.getModel().equals(this.model)
            && Double.compare(d.getConsumptionPerHour(), this.consumptionPerHour) == 0 && d.getStatus() == this.status
            && d.getTotalActivations() == this.totalActivations && d.getTotalMinutesOn() == this.totalMinutesOn;
    }

    /**
     * Calculates the hash code of this device.
     * 
     * @return The hash code of this device.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.brand, this.model, this.consumptionPerHour, this.status, this.totalMinutesOn, this.totalActivations);
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
     * The string representation includes the class name, id, brand, model, consumption rate, status, total minutes on, total activations and total energy consumption of the device.
     * 
     * @return A string representation of this device.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("\n")
            .append("ID: ").append(this.id).append("\n")
            .append("Brand: ").append(this.brand).append("\n")
            .append("Model: ").append(this.model).append("\n")
            .append("Consumption: ").append(String.format(Locale.US, "%.2f", this.consumptionPerHour)).append(" Wh/h\n")
            .append("Status: ").append(this.status).append("\n")
            .append("Minutes on: ").append(this.totalMinutesOn).append("\n")
            .append("Activations: ").append(this.totalActivations).append("\n")
            .append("Energy used: ").append(String.format(Locale.US, "%.2f", this.getEnergyConsumption())).append(" Wh\n");
        return sb.toString();
    }
}
