package domuscontrol.model.routines;

import java.io.Serializable;

/**
 * Interface representing a logical requirement that must be satisfied for a routine to trigger.
 * Conditions can be based on device states (e.g., "if the light is on") or environmental 
 * factors (e.g., "if it is 8:00 AM").
 */
public interface Condition extends Serializable {
    
    /**
     * @return true if the condition is currently met, false otherwise.
     */
    boolean evaluate();

    /**
     * Creates a deep copy of this condition instance.
     * * @return A new Condition instance that is a copy of this one.
     */
    Condition copy();

    /**
     * Checks if this condition is linked to a specific device identifier.
     * By default, this returns false as some conditions (like time-based ones) 
     * do not belong to a specific device.
     * * @param deviceId The unique identifier of the device to check.
     * @return true if the condition depends on the specified device, false otherwise.
     */
    default boolean hasDeviceId(int deviceId) {
        return false;
    }
}