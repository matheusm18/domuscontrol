package domuscontrol.routines;

import java.io.Serializable;

import domuscontrol.houses.House;

/**
 * Represents an operation that can be executed as part of a routine.
 * Actions are the executable units used by scenarios, automations, and schedules.
 */
public interface Action extends Serializable {

    /**
     * Executes this action in the provided house context.
     * Implementations should resolve their target devices through the house,
     * usually by device ID, instead of storing live device references.
     *
     * @param house the house where the action should be applied
     */
    void execute(House house);

    /**
     * Creates a copy of this action.
     *
     * @return a new Action with the same configuration
     */
    Action copy();

    /**
     * Checks whether this action targets the given device.
     *
     * @param deviceId the device identifier to check
     * @return true if the action targets the specified device, false otherwise.
     */
    boolean hasDeviceId(int deviceId);
}
