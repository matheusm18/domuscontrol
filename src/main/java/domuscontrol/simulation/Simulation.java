package domuscontrol.simulation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;


public class Simulation implements Serializable {

    private static final long serialVersionUID = 1L;
    public enum WeatherCondition {
        SUNNY(1.0, "SUNNY"),
        PARTLY_CLOUDY(0.8, "PARTLY_CLOUDY"),
        CLOUDY(0.5, "CLOUDY"),
        FOGGY(0.4, "FOGGY"),
        RAINING(0.3, "RAINING"),
        STORMY(0.15, "STORMY"),
        SNOWING(0.6, "SNOWING"),
        WINTER_IS_COMING(0.2, "WINTER"),
        APOCALYPSE(0.05, "APOCALYPSE");

        private final double luminosityMultiplier;
        private final String displayName;

        WeatherCondition(double luminosityMultiplier, String displayName) {
            this.luminosityMultiplier = luminosityMultiplier;
            this.displayName = displayName;
        }

        public double getLuminosityMultiplier() {
            return luminosityMultiplier;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }

    private LocalDateTime currentDateTime;
    private LocalDateTime previousDateTime;

    private double temperature; // Graus Celsius
    private WeatherCondition weather;

    public Simulation() {
        this.currentDateTime = LocalDateTime.now();
        this.previousDateTime = this.currentDateTime;
        this.temperature = 20.0;
        this.weather = WeatherCondition.SUNNY;
    }

    public Simulation(LocalDateTime currentDateTime, double temperature, double luminosity, WeatherCondition weather) {
        this.currentDateTime = currentDateTime;
        this.previousDateTime = currentDateTime;
        this.temperature = temperature;
        this.weather = weather;
    }

    public Simulation(Simulation simulation) {
        this.currentDateTime = simulation.getCurrentDateTime();
        this.previousDateTime = simulation.getPreviousDateTime();
        this.temperature = simulation.getTemperature();
        this.weather = simulation.getWeather();
    }

    public LocalDateTime getCurrentDateTime() {
        return this.currentDateTime;
    }
    public void setCurrentDateTime(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public LocalDateTime getPreviousDateTime() {
        return this.previousDateTime;
    }
    public void setPreviousDateTime(LocalDateTime previousDateTime) {
        this.previousDateTime = previousDateTime;
    }

    public Long getTimeElapsed() {
        return Duration.between(this.previousDateTime, this.currentDateTime).toMinutes();
    }

    public double getTemperature() {
        return this.temperature;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getLuminosity() {
        int hour = currentDateTime.getHour();

        double baseLuminosity = (hour >= 6 && hour < 18) ? 1000.0 : 100.0;

        return baseLuminosity * this.weather.getLuminosityMultiplier();
    }

    public WeatherCondition getWeather() {
        return this.weather;
    }

    public void changeWeather(WeatherCondition weather) {
        this.weather = weather;
    }

    public boolean isRaining() {
        return this.weather == WeatherCondition.RAINING;
    }

    public void advanceTime(int days, int hours, int minutes) {
        this.previousDateTime = this.currentDateTime;
        this.currentDateTime = this.currentDateTime.plusDays(days).plusHours(hours).plusMinutes(minutes);
    }

    public void advanceDays(int days) {
        this.previousDateTime = this.currentDateTime;
        this.currentDateTime = this.currentDateTime.plusDays(days);
    }

    public void advanceHours(int hours) {
        this.previousDateTime = this.currentDateTime;
        this.currentDateTime = this.currentDateTime.plusHours(hours);
    }

    public void advanceMinutes(int minutes) {
        this.previousDateTime = this.currentDateTime;
        this.currentDateTime = this.currentDateTime.plusMinutes(minutes);
    }

    public boolean isWinterComing() {
        return this.weather == WeatherCondition.WINTER_IS_COMING;
    }

    public boolean isApocalypse() {
        return this.weather == WeatherCondition.APOCALYPSE;
    }

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

            if (Math.random() < 0.001) {
                this.weather = WeatherCondition.APOCALYPSE;
            } else if (Math.random() < 0.01) { 
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
                        
                    case APOCALYPSE:
                        this.weather = (rand < 0.1) ? WeatherCondition.CLOUDY : WeatherCondition.APOCALYPSE;
                        break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == null || this.getClass() != o.getClass()) return false;

        Simulation Simulation = (Simulation) o;

        return this.currentDateTime.equals(Simulation.getCurrentDateTime()) &&
               this.previousDateTime.equals(Simulation.getPreviousDateTime()) &&
               Double.compare(this.temperature, Simulation.getTemperature()) == 0 &&
               this.weather == Simulation.getWeather();
    }

    @Override
    public Simulation clone() {
        return new Simulation(this);
    }
}
