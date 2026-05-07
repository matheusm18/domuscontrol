package domuscontrol.routines;

import domuscontrol.houses.House;
import domuscontrol.simulation.SimulationState;
import java.io.Serializable;

/**
 * Represents a logical requirement that must be satisfied for a routine to trigger.
 * Conditions can depend on device state, simulation time, or environmental values.
 */
public interface Condition extends Serializable {

    /**
     * Evaluates whether this condition is currently satisfied.
     *
     * @param house the house context used to inspect devices, when needed
     * @param state the current simulation state used for time and environment conditions
     * @return true if the condition is currently met, false otherwise.
     */
    boolean evaluate(House house, SimulationState state);

    /**
     * Creates a copy of this condition.
     *
     * @return a new Condition with the same configuration
     */
    Condition copy();

    /**
     * Checks whether this condition depends on the given device.
     * Conditions that do not reference devices should keep the default false result.
     *
     * @param deviceId the device identifier to check
     * @return true if the condition depends on the specified device, false otherwise.
     */
    default boolean hasDeviceId(int deviceId) {
        return false;
    }
}
