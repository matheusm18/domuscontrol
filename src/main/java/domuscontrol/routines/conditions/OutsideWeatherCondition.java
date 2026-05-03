package domuscontrol.routines.conditions;

import domuscontrol.simulation.Simulation;
import domuscontrol.houses.House;
import domuscontrol.routines.Condition;

import java.util.Objects;

/**
 * Condition that evaluates whether the current environmental weather
 * equals a specific weather state.
 */
public class OutsideWeatherCondition implements Condition {

    private Simulation.WeatherCondition triggerWeather;

    public OutsideWeatherCondition() {
        this.triggerWeather = Simulation.WeatherCondition.SUNNY;
    }

    public OutsideWeatherCondition(Simulation.WeatherCondition triggerWeather) {
        this.triggerWeather = triggerWeather;
    }

    public OutsideWeatherCondition(OutsideWeatherCondition other) {
        this.triggerWeather = other.triggerWeather;
    }

    public Simulation.WeatherCondition getTriggerWeather() {
        return triggerWeather;
    }

    public void setTriggerWeather(Simulation.WeatherCondition triggerWeather) {
        this.triggerWeather = triggerWeather;
    }

    @Override
    public boolean evaluate(House house, Simulation simulation) {
        return simulation.getWeather() == this.triggerWeather;
    }

    @Override
    public Condition copy() {
        return new OutsideWeatherCondition(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        OutsideWeatherCondition that = (OutsideWeatherCondition) o;
        return this.triggerWeather == that.triggerWeather;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.triggerWeather);
    }

    @Override
    public OutsideWeatherCondition clone() {
        return new OutsideWeatherCondition(this);
    }

    @Override
    public String toString() {
        return "OutsideWeatherCondition { Weather IS " + this.triggerWeather + " }";
    }
}
