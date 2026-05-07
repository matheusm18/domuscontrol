package domuscontrol.routines;

import java.io.Serializable;

import domuscontrol.houses.House;

/**
 * Represents an operation that can be executed as part of a routine.
 * Actions are the executable units used by scenarios, automations, and schedules.
 * Each action targets a specific device and performs a defined operation.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
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
