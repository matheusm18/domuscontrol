package domuscontrol.devices.sensors;

import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.simulation.SimulationState;

/**
 * Abstract base class for environmental sensor devices.
 * Sensors are always initialised in the ON state and do not consume energy.
 * Each tick, the house calls {@link #updateFromState(SimulationState)} to push
 * the latest environmental readings into the sensor.
 */
public abstract class Sensor extends SwitchableDevice {

    /**
     * Creates a sensor with default values, turned ON immediately.
     */
    public Sensor() {
        super();
        this.turnOn();
    }

    /**
     * Creates a sensor with the given hardware details, turned ON immediately.
     *
     * @param brand              the sensor brand
     * @param model              the sensor model
     * @param consumptionPerHour the standby power consumption in Wh (usually near zero)
     */
    public Sensor(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
        this.turnOn();
    }

    /**
     * Copy constructor.
     *
     * @param s the sensor to copy
     */
    public Sensor(Sensor s) {
        super(s);
    }

    /**
     * Updates the sensor's internal reading from the current simulation state.
     * Implementations should only update if the sensor is ON.
     *
     * @param state the current simulation state snapshot
     */
    public abstract void updateFromState(SimulationState state);

    /**
     * Sensors do not count toward energy consumption.
     *
     * @return always false
     */
    @Override
    public boolean isConsuming() {
        return false;
    }
}
