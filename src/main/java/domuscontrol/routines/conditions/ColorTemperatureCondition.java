package domuscontrol.routines.conditions;

import domuscontrol.simulation.SimulationState;
import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

import java.util.Objects;

/**
 * Condition that compares the color temperature of a color-adjustable device with a trigger value.
 * Evaluates whether a device's current color temperature in Kelvin satisfies a comparison
 * with a trigger temperature using the specified operator.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class ColorTemperatureCondition implements Condition {

    /** The identifier of the target color-adjustable device. */
    private int deviceId;
    /** The color temperature threshold in Kelvin used for comparison. */
    private int triggerTemperature;
    /** The comparison operator to apply when evaluating the condition. */
    private Operator operator;

    /**
     * Creates a condition with no target device, trigger temperature 2700K, and EQUALS operator.
     */
    public ColorTemperatureCondition() {
        this.deviceId = -1;
        this.triggerTemperature = 2700;
        this.operator = Operator.EQUALS;
    }

    /**
     * Creates a condition for the given device, trigger temperature, and operator.
     *
     * @param deviceId the target device identifier
     * @param triggerTemperature the trigger temperature in Kelvin
     * @param operator the comparison operator
     */
    public ColorTemperatureCondition(int deviceId, int triggerTemperature, Operator operator) {
        this.deviceId = deviceId;
        this.triggerTemperature = triggerTemperature;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Creates a copy of another color-temperature condition.
     *
     * @param other the condition to copy
     */
    public ColorTemperatureCondition(ColorTemperatureCondition other) {
        this.deviceId = other.getDeviceId();
        this.triggerTemperature = other.getTriggerTemperature();
        this.operator = other.getOperator();
    }

    /**
     * Gets the target device identifier.
     *
     * @return the target device identifier
     */
    public int getDeviceId() {
        return this.deviceId;
    }

    /**
     * Sets the target device identifier.
     *
     * @param deviceId the target device identifier
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the trigger temperature.
     *
     * @return the trigger temperature in Kelvin
     */
    public int getTriggerTemperature() {
        return this.triggerTemperature;
    }

    /**
     * Sets the trigger temperature.
     *
     * @param triggerTemperature the trigger temperature in Kelvin
     */
    public void setTriggerTemperature(int triggerTemperature) {
        this.triggerTemperature = triggerTemperature;
    }

    /**
     * Gets the comparison operator.
     *
     * @return the comparison operator
     */
    public Operator getOperator() {
        return this.operator;
    }

    /**
     * Sets the comparison operator.
     *
     * @param operator the comparison operator
     */
    public void setOperator(Operator operator) {
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Evaluates this condition against the current color temperature of the target device.
     *
     * @param house the house where the target device is stored
     * @param state the current simulation state
     * @return true if the current temperature satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate(House house, SimulationState state) {
        try {
            return house.readDevice(this.deviceId, d -> {
                if (!(d instanceof ColorAdjustableDevice cad)) {
                    return false;
                }
                int current = cad.getColorTemperature();
                switch (this.operator) {
                    case EQUALS:       return current == this.triggerTemperature;
                    case GREATER_THAN: return current > this.triggerTemperature;
                    case LESS_THAN:    return current < this.triggerTemperature;
                    default:           return false;
                }
            });
        } catch (DeviceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied ColorTemperatureCondition instance
     */
    @Override
    public Condition copy() {
        return new ColorTemperatureCondition(this);
    }

    /**
     * Checks whether this condition depends on the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.deviceId == deviceId;
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if device, trigger temperature, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ColorTemperatureCondition condition = (ColorTemperatureCondition) o;
        return this.triggerTemperature == condition.getTriggerTemperature() &&
               this.operator == condition.getOperator() &&
               this.deviceId == condition.getDeviceId();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.deviceId, this.triggerTemperature, this.operator);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied ColorTemperatureCondition instance
     */
    @Override
    public ColorTemperatureCondition clone() {
        return new ColorTemperatureCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorTemperatureCondition { ")
          .append("Device ID: ").append(this.deviceId)
          .append(", Temperature ").append(this.operator).append(" ").append(this.triggerTemperature).append("K")
          .append(" }");
        return sb.toString();
    }
}
