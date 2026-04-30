package domuscontrol.model.routines;

import java.io.Serializable;

/**
 * Interface representing a generic action that can be performed within the home automation system.
 * Actions are the fundamental units of execution for scenarios and automations.
 */
public interface Action extends Serializable {

    /**
     * Executes the logic associated with this action on the provided house.
     */
    void execute();

    /**
     * Creates a deep copy of this action instance.
     * * @return A new Action object that is a copy of the current one.
     */
    Action copy();

    /**
     * Determines if this action is linked to a specific device.
     * * @param deviceId The unique identifier of the device to check.
     * @return true if the action targets the specified device, false otherwise.
     */
    boolean hasDeviceId(int deviceId);
}