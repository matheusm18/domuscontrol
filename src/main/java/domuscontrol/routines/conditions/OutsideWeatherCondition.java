package domuscontrol.routines.conditions;

import domuscontrol.simulation.Simulation;
import domuscontrol.simulation.WeatherCondition;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

import java.util.Objects;

/**
 * Condition that checks whether the current simulation weather matches a trigger value.
 */
public class OutsideWeatherCondition implements Condition {

    private WeatherCondition triggerWeather;

    /**
     * Creates a condition that triggers when the weather is sunny.
     */
    public OutsideWeatherCondition() {
        this.triggerWeather = WeatherCondition.SUNNY;
    }

    /**
     * Creates a condition with the given trigger weather.
     *
     * @param triggerWeather the trigger weather
     */
    public OutsideWeatherCondition(WeatherCondition triggerWeather) {
        this.triggerWeather = triggerWeather;
    }

    /**
     * Creates a copy of another outside-weather condition.
     *
     * @param other the condition to copy
     */
    public OutsideWeatherCondition(OutsideWeatherCondition other) {
        this.triggerWeather = other.getTriggerWeather();
    }

    /**
     * Gets the trigger weather.
     *
     * @return the trigger weather
     */
    public WeatherCondition getTriggerWeather() {
        return this.triggerWeather;
    }

    /**
     * Sets the trigger weather.
     *
     * @param triggerWeather the trigger weather
     */
    public void setTriggerWeather(WeatherCondition triggerWeather) {
        this.triggerWeather = triggerWeather;
    }

    /**
     * Evaluates this condition against the current simulation weather.
     *
     * @param house the house context
     * @param simulation the current simulation state
     * @return true if the current weather matches the trigger weather; false otherwise
     */
    @Override
    public boolean evaluate(House house, Simulation simulation) {
        return simulation.getWeather() == this.triggerWeather;
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied OutsideWeatherCondition instance
     */
    @Override
    public Condition copy() {
        return new OutsideWeatherCondition(this);
    }

    /**
     * Compares this condition with another object for equality.
     *
     * @param o the object to compare with
     * @return true if trigger weather values match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        OutsideWeatherCondition condition = (OutsideWeatherCondition) o;
        return this.triggerWeather == condition.getTriggerWeather();
    }

    /**
     * Generates a hash code for this condition.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.triggerWeather);
    }

    /**
     * Creates a copy of this condition.
     *
     * @return a copied OutsideWeatherCondition instance
     */
    @Override
    public OutsideWeatherCondition clone() {
        return new OutsideWeatherCondition(this);
    }

    /**
     * Returns a string representation of this condition.
     *
     * @return a formatted string with the condition information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OutsideWeatherCondition { ")
          .append("Weather IS ").append(this.triggerWeather)
          .append(" }");
        return sb.toString();
    }
}
