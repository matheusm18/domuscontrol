package domuscontrol.routines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.houses.House;

/**
 * Abstract base class for all routine types in the system, such as Scenarios and Automations.
 * It provides the fundamental structure for naming a routine and managing a list of actions
 * that should be executed.
 */
public abstract class Routine implements Serializable {
    private String name;
    private List<Action> actions;

    /**
     * Default constructor.
     * Initializes the routine with a placeholder name and an empty list of actions.
     */
    public Routine() {
        this.name = "Unnamed";
        this.actions = new ArrayList<>();
    }

    /**
     * Parameterized constructor.
     * * @param name    The descriptive name of the routine.
     * @param actions The initial list of actions to be associated with this routine.
     */
    public Routine(String name, List<Action> actions) {
        this.name = name;
        this.setActions(actions);
    }

    /**
     * Copy constructor for deep copying.
     * * @param other The existing Routine instance to copy.
     */
    public Routine(Routine other) {
        this.name = other.getName();
        this.setActions(other.getActions());
    }

    /**
     * Retrieves the name of the routine.
     * * @return The routine name.
     */
    public String getName() { return name; }

    /**
     * Updates the name of the routine.
     * * @param name The new name to set.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retrieves a deep copy of the actions list.
     * * @return A new list containing copies of the current actions.
     */
    public List<Action> getActions() {
        return this.actions.stream().map(Action::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Sets the actions for this routine, ensuring a deep copy is stored.
     * * @param actions The list of actions to assign.
     */
    public void setActions(List<Action> actions) {
        if (actions != null) {
            this.actions = actions.stream().map(Action::copy).collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.actions = new ArrayList<>();
        }
    }

    /**
     * Adds a single action to the routine.
     * * @param action The action to add.
     */
    public void addAction(Action action) {
        if (action != null) {
            this.actions.add(action.copy());
        }
    }

    protected void executeActions(House house) {
        for (Action action : this.actions) {
            action.execute(house);
        }
    }

    /**
     * Removes a specific action from the routine.
     * * @param action The action instance to remove.
     * @return true if the action was successfully removed; false otherwise.
     */
    public boolean removeAction(Action action) {
        if (action != null && this.actions != null) {
            return this.actions.remove(action);
        }
        return false;
    }

    /**
     * Compares this routine with another object for equality.
     * * @param o The object to compare with.
     * @return true if names and action lists match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Routine routine = (Routine) o;
        return Objects.equals(name, routine.name) && Objects.equals(actions, routine.actions);
    }

    /**
     * Generates a hash code for this routine.
     * * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, actions);
    }

    /**
     * Returns a string representation of the routine.
     * * @return Formatted string with name and action count.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: '").append(this.name).append('\'')
          .append(", Actions: ").append(this.actions != null ? this.actions.size() : 0);
        return sb.toString();
    }
    
    /**
     * Abstract method to create a deep copy of the specific routine implementation.
     * * @return A cloned instance of the routine.
     */
    public abstract Routine clone();

    /**
     * Safely removes all actions associated with a specific device ID.
     * This is used during a cascade delete when a device is removed from the house.
     * * @param deviceId The unique identifier of the device to remove references for.
     */
    public void removeDeviceById(int deviceId) {
        if (this.actions != null) {
            this.actions.removeIf(action -> action.hasDeviceId(deviceId));
        }
    }
}
