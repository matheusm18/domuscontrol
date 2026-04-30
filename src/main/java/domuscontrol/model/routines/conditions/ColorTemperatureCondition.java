package domuscontrol.model.routines.conditions;

import domuscontrol.model.device.types.ColorAdjustableDevice;
import domuscontrol.model.routines.Condition;
import java.util.Objects;

/**
 * Condition that evaluates whether the color temperature of a color-adjustable device
 * satisfies a specific comparison against a trigger value.
 */
public class ColorTemperatureCondition implements Condition {

    private ColorAdjustableDevice device;
    private int triggerTemperature;
    private Operator operator;

    /**
     * Default constructor initializing device to null and operator to EQUALS.
     */
    public ColorTemperatureCondition() {
        this.device = null;
        this.triggerTemperature = 2700;
        this.operator = Operator.EQUALS;
    }

    /**
     * Parameterized constructor.
     *
     * @param device             The live reference to the color-adjustable device to monitor.
     * @param triggerTemperature The threshold temperature in Kelvin for comparison.
     * @param operator           The comparison operator (EQUALS, GREATER_THAN, LESS_THAN).
     */
    public ColorTemperatureCondition(ColorAdjustableDevice device, int triggerTemperature, Operator operator) {
        this.device = device;
        this.triggerTemperature = triggerTemperature;
        this.operator = operator != null ? operator : Operator.EQUALS;
    }

    /**
     * Copy constructor. The device pointer remains shared.
     *
     * @param other The existing ColorTemperatureCondition instance to copy.
     */
    public ColorTemperatureCondition(ColorTemperatureCondition other) {
        this.device = other.device;
        this.triggerTemperature = other.triggerTemperature;
        this.operator = other.operator;
    }

    /**
     * Returns the target device.
     *
     * @return The color-adjustable device reference.
     */
    public ColorAdjustableDevice getDevice() { return device; }

    /**
     * Sets a new target device.
     *
     * @param device The new color-adjustable device reference.
     */
    public void setDevice(ColorAdjustableDevice device) { this.device = device; }

    /**
     * Returns the trigger temperature.
     *
     * @return The threshold temperature in Kelvin.
     */
    public int getTriggerTemperature() { return triggerTemperature; }

    /**
     * Sets a new trigger temperature.
     *
     * @param triggerTemperature The new threshold temperature in Kelvin.
     */
    public void setTriggerTemperature(int triggerTemperature) { this.triggerTemperature = triggerTemperature; }

    /**
     * Returns the comparison operator.
     *
     * @return The Operator enum value.
     */
    public Operator getOperator() { return operator; }

    /**
     * Sets a new comparison operator.
     *
     * @param operator The new Operator to be used.
     */
    public void setOperator(Operator operator) { this.operator = operator != null ? operator : Operator.EQUALS; }

    /**
     * Evaluates the condition by reading the current color temperature of the live device reference.
     *
     * @return true if the current temperature satisfies the operator comparison; false otherwise.
     */
    @Override
    public boolean evaluate() {
        if (this.device == null) return false;
        int current = this.device.getColorTemperature();
        switch (this.operator) {
            case EQUALS:       return current == this.triggerTemperature;
            case GREATER_THAN: return current > this.triggerTemperature;
            case LESS_THAN:    return current < this.triggerTemperature;
            default:           return false;
        }
    }

    /**
     * Creates a copy of this condition.
     *
     * @return A new instance of ColorTemperatureCondition.
     */
    @Override
    public Condition copy() {
        return new ColorTemperatureCondition(this);
    }

    /**
     * Checks if this condition is associated with a specific device ID.
     *
     * @param deviceId The ID to check.
     * @return true if the stored device's ID matches; false otherwise.
     */
    @Override
    public boolean hasDeviceId(int deviceId) {
        return this.device != null && this.device.getId() == deviceId;
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o The object to compare with.
     * @return true if device, trigger temperature, and operator match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ColorTemperatureCondition that = (ColorTemperatureCondition) o;
        return this.triggerTemperature == that.triggerTemperature &&
               this.operator == that.operator &&
               Objects.equals(this.device, that.device);
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return The hash code based on the device, trigger temperature, and operator.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.device, this.triggerTemperature, this.operator);
    }

    /**
     * Clones this condition instance.
     *
     * @return A cloned ColorTemperatureCondition.
     */
    @Override
    public ColorTemperatureCondition clone() {
        return new ColorTemperatureCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return Formatted string containing device info and comparison logic.
     */
    @Override
    public String toString() {
        return "ColorTemperatureCondition { Device: " +
               (this.device != null ? this.device.getId() : "null") +
               ", Temperature " + this.operator + " " + this.triggerTemperature + "K }";
    }
}