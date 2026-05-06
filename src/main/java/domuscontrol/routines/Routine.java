package domuscontrol.routines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.houses.House;

/**
 * Base class for all routine types in the system.
 * A routine has a name and an ordered list of actions that can be executed
 * in a house context.
 */
public abstract class Routine implements Serializable {
    private String name;
    private List<Action> actions;

    /**
     * Creates an unnamed routine with no actions.
     */
    public Routine() {
        this.name = "Unnamed Routine";
        this.actions = new ArrayList<>();
    }

    /**
     * Creates a routine with the given name and actions.
     * The provided actions are copied before being stored.
     *
     * @param name the routine name
     * @param actions the actions associated with this routine
     */
    public Routine(String name, List<Action> actions) {
        this.name = name;
        this.setActions(actions);
    }

    /**
     * Creates a copy of another routine.
     *
     * @param other the routine to copy
     */
    public Routine(Routine other) {
        this.name = other.getName();
        this.setActions(other.getActions());
    }

    /**
     * Gets the routine name.
     *
     * @return the routine name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the routine name.
     *
     * @param name the new routine name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets a copy of the actions associated with this routine.
     *
     * @return a new list containing copies of the current actions
     */
    public List<Action> getActions() {
        return this.actions.stream().map(Action::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Replaces this routine's actions.
     * The provided actions are copied before being stored.
     *
     * @param actions the actions to assign, or null for an empty list
     */
    public void setActions(List<Action> actions) {
        if (actions != null) {
            this.actions = actions.stream().map(Action::copy).collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.actions = new ArrayList<>();
        }
    }

    /**
     * Adds an action to this routine.
     * The provided action is copied before being stored.
     *
     * @param action the action to add
     */
    public void addAction(Action action) {
        if (action != null) {
            this.actions.add(action.copy());
        }
    }

    /**
     * Executes all actions associated with this routine in order.
     *
     * @param house the house where the actions should be applied
     */
    public void executeActions(House house) {
        for (Action action : this.actions) {
            action.execute(house);
        }
    }

    /**
     * Removes an action from this routine.
     *
     * @param action the action to remove
     * @return true if the action was successfully removed; false otherwise.
     */
    public boolean removeAction(Action action) {
        if (action != null) {
            return this.actions.remove(action);
        }
        return false;
    }

    /**
     * Removes all actions that target the given device.
     * Used when a device is removed from the house.
     *
     * @param deviceId the device identifier to remove references to
     */
    public void removeDeviceById(int deviceId) {
        this.actions.removeIf(action -> action.hasDeviceId(deviceId));
    }

    /**
     * Compares this routine with another object for equality.
     *
     * @param o the object to compare with
     * @return true if names and action lists match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Routine routine = (Routine) o;
        return Objects.equals(name, routine.getName()) && Objects.equals(actions, routine.getActions());
    }

    /**
     * Generates a hash code for this routine.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, actions);
    }

    /**
     * Returns a string representation of this routine.
     *
     * @return a formatted string with the name and action count
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(" { ")
          .append("Name: '").append(this.name).append('\'')
          .append(", Actions: ").append(this.actions.size())
          .append(" }");
        return sb.toString();
    }

    /**
     * Creates a copy of this routine.
     *
     * @return a copied routine instance
     */
    public abstract Routine clone();
}
