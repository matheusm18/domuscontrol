package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;


/**
 * Represents the state of the simulation environment, including the current date/time,
 * temperature, and weather condition. Handles time advancement and environmental updates.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Simulation implements Serializable {


    /**
     * The current date and time of the simulation.
     */
    private LocalDateTime currentDateTime;

    /**
     * The previous date and time before the last simulation tick.
     */
    private LocalDateTime previousDateTime;

    /**
     * The current temperature in Celsius.
     */
    private double temperature;

    /**
     * The current weather condition in the simulation.
     */
    private WeatherCondition weather;

    /**
     * Creates a new simulation starting at the current date/time, 20°C, and sunny weather.
     */
    public Simulation() {
        this.currentDateTime = LocalDateTime.now();
        this.previousDateTime = this.currentDateTime;
        this.temperature = 20.0;
        this.weather = WeatherCondition.SUNNY;
    }

    /**
     * Creates a simulation with the specified date/time, temperature, and weather.
     *
     * @param currentDateTime The starting date and time.
     * @param temperature The starting temperature in Celsius.
     * @param weather The starting weather condition.
     */
    public Simulation(LocalDateTime currentDateTime, double temperature, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.previousDateTime = currentDateTime;
        this.temperature = temperature;
        this.weather = weather;
    }

    /**
     * Copy constructor.
     *
     * @param simulation The simulation to copy.
     */
    public Simulation(Simulation simulation) {
        this.currentDateTime = simulation.getCurrentDateTime();
        this.previousDateTime = simulation.getPreviousDateTime();
        this.temperature = simulation.getTemperature();
        this.weather = simulation.getWeather();
    }

    /**
     * Returns the current simulation date and time.
     *
     * @return The current date/time.
     */
    public LocalDateTime getCurrentDateTime() {
        return this.currentDateTime;
    }

    /**
     * Sets the current simulation date and time.
     *
     * @param currentDateTime The new current date/time.
     */
    public void setCurrentDateTime(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    /**
     * Returns the simulation date and time at the start of the last tick.
     *
     * @return The previous date/time.
     */
    public LocalDateTime getPreviousDateTime() {
        return this.previousDateTime;
    }

    /**
     * Sets the previous simulation date and time.
     *
     * @param previousDateTime The previous date/time to set.
     */
    public void setPreviousDateTime(LocalDateTime previousDateTime) {
        this.previousDateTime = previousDateTime;
    }

    /**
     * Returns the number of minutes elapsed since the last tick.
     *
     * @return Minutes elapsed between the previous and current date/time.
     */
    public long getTimeElapsed() {
        return Duration.between(this.previousDateTime, this.currentDateTime).toMinutes();
    }

    /**
     * Returns the current temperature in Celsius.
     *
     * @return The temperature.
     */
    public double getTemperature() {
        return this.temperature;
    }

    /**
     * Sets the temperature.
     *
     * @param temperature The temperature in Celsius.
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns the ambient luminosity in lux, calculated from the time of day and weather.
     *
     * @return The luminosity value.
     */
    public double getLuminosity() {
        int hour = this.currentDateTime.getHour();
        double baseLuminosity = (hour >= 6 && hour < 18) ? 1000.0 : 100.0;
        return baseLuminosity * this.weather.getLuminosityMultiplier();
    }

    /**
     * Returns the current weather condition.
     *
     * @return The weather condition.
     */
    public WeatherCondition getWeather() {
        return this.weather;
    }

    /**
     * Changes the current weather condition.
     *
     * @param weather The new weather condition.
     */
    public void changeWeather(WeatherCondition weather) {
        this.weather = weather;
    }

    /**
     * Returns whether it is currently raining or stormy.
     *
     * @return true if the weather is RAINING or STORMY.
     */
    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING || this.weather == WeatherCondition.STORMY;
    }

    /**
     * Returns whether the weather is WINTER_IS_COMING.
     *
     * @return true if winter is coming.
     */
    public boolean isWinterComing() {
        return this.weather == WeatherCondition.WINTER_IS_COMING;
    }

    /**
     * Advances the simulation by the given number of minutes (ticks), updating temperature
     * and weather stochastically each tick.
     *
     * @param ticks The number of minutes to simulate.
     */
    public void advanceSimulation(int ticks) {
        this.previousDateTime = this.currentDateTime;
        this.currentDateTime = this.currentDateTime.plusMinutes(ticks);

        for (int i = 0; i < ticks; i++) {
            int hour = this.currentDateTime.minusMinutes(ticks - 1 - i).getHour();

            double tempChange = 0.0;

            if (hour >= 7 && hour <= 15) {
                tempChange += 0.01;
            } else if (hour >= 18 || hour <= 5) {
                tempChange -= 0.01;
            }

            switch (this.weather) {
                case SUNNY: tempChange += 0.005; break;
                case SNOWING: tempChange -= 0.015; break;
                case RAINING:
                case STORMY: tempChange -= 0.008; break;
                default: break;
            }

            tempChange += (Math.random() * 0.04) - 0.02;
            this.temperature += tempChange;

            if (this.temperature > 45.0) this.temperature = 45.0;
            if (this.temperature < -15.0) this.temperature = -15.0;

            if (Math.random() < 0.01) {
                double rand = Math.random();
                
                switch (this.weather) {
                    case SUNNY:
                        this.weather = (rand < 0.7) ? WeatherCondition.PARTLY_CLOUDY : WeatherCondition.CLOUDY;
                        break;
                        
                    case PARTLY_CLOUDY:
                        this.weather = (rand < 0.5) ? WeatherCondition.SUNNY : WeatherCondition.CLOUDY;
                        break;
                        
                    case CLOUDY:
                        if (rand < 0.3) {
                            this.weather = WeatherCondition.PARTLY_CLOUDY;
                        } else if (rand < 0.6) {
                            this.weather = WeatherCondition.RAINING;
                        } else if (rand < 0.8 && this.temperature <= 2.0) {
                            this.weather = WeatherCondition.SNOWING;
                        } else {
                            this.weather = WeatherCondition.FOGGY;
                        }
                        break;
                        
                    case RAINING:
                        if (this.temperature <= 0.0) {
                            this.weather = WeatherCondition.SNOWING;
                        } else {
                            this.weather = (rand < 0.7) ? WeatherCondition.CLOUDY : WeatherCondition.STORMY;
                        }
                        break;
                        
                    case STORMY:
                        this.weather = WeatherCondition.RAINING;
                        break;
                        
                    case SNOWING:
                        this.weather = (this.temperature > 2.0) ? WeatherCondition.RAINING : WeatherCondition.CLOUDY;
                        break;
                        
                    case FOGGY:
                        this.weather = WeatherCondition.CLOUDY;
                        break;

                    case WINTER_IS_COMING:
                        this.weather = (rand < 0.5) ? WeatherCondition.SNOWING : WeatherCondition.WINTER_IS_COMING;
                        break;
                        
                }
            }
        }
    }

    /**
     * Checks if this simulation is equal to another object.
     *
     * @param o The object to compare with.
     * @return true if all fields are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Simulation s = (Simulation) o;

        return this.currentDateTime.equals(s.getCurrentDateTime()) &&
               this.previousDateTime.equals(s.getPreviousDateTime()) &&
               Double.compare(this.temperature, s.getTemperature()) == 0 &&
               this.weather == s.getWeather();
    }

    /**
     * Returns the hash code for this simulation.
     *
     * @return the hash code value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.currentDateTime, this.previousDateTime, this.temperature, this.weather);
    }

    /**
     * Creates a deep copy of this simulation.
     *
     * @return a new Simulation object with the same state.
     */
    @Override
    public Simulation clone() {
        return new Simulation(this);
    }
}
