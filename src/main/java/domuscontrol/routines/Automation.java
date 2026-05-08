package domuscontrol.routines;

import domuscontrol.simulation.SimulationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.houses.House;
import domuscontrol.routines.conditions.TimeBasedCondition;

/**
 * Represents a routine that can be triggered automatically.
 * An automation has conditions in addition to the actions inherited from Routine.
 * When all conditions are met and were not previously met, the automation's actions
 * are executed. Schedules are represented as automations whose conditions are time-based.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Automation extends Routine {
    
    /** The type of the automation. */
    private AutomationType type;
    /** The conditions required to trigger the actions. */
    private List<Condition> conditions;
    /** Indicates whether the conditions were met in a previous simulation step. */
    private boolean wasConditionMetPreviously;

    /**
     * Creates an unnamed automation with no actions and no conditions.
     */
    public Automation() {
        super();
        this.type = AutomationType.AUTOMATION;
        this.conditions = new ArrayList<>();
        this.wasConditionMetPreviously = false;
    }

    /**
     * Creates an automation with the given type, conditions, and actions.
     * The provided actions and conditions are copied before being stored.
     *
     * @param name the automation name
     * @param type the automation type
     * @param conditions the conditions required to trigger the actions
     * @param actions the actions to execute when the conditions are met
     * @throws ScheduleWithConditionDifferentFromTimeException if a schedule receives non-time conditions
     */
    public Automation(String name, AutomationType type, List<Condition> conditions, List<Action> actions) throws ScheduleWithConditionDifferentFromTimeException {
        super(name, actions);
        this.type = type != null ? type : AutomationType.AUTOMATION;
        this.setConditions(conditions);
        this.wasConditionMetPreviously = false;
    }

    /**
     * Creates a copy of another automation.
     *
     * @param other the automation to copy
     */
    public Automation(Automation other) {
        super(other);
        this.type = other.getType();
        this.conditions = other.getConditions();
        this.wasConditionMetPreviously = other.isWasConditionMetPreviously();
    }

    /**
     * Gets the automation type.
     *
     * @return the current automation type
     */
    public AutomationType getType() {
        return this.type;
    }
    
    /**
     * Sets the automation type and validates the current conditions.
     *
     * @param type the new automation type
     * @throws ScheduleWithConditionDifferentFromTimeException if a schedule would contain non-time conditions
     */
    public void setType(AutomationType type) throws ScheduleWithConditionDifferentFromTimeException { 
        if (type == AutomationType.SCHEDULE) {
            for (Condition c : this.conditions) {
                if (!(c instanceof TimeBasedCondition)) {
                    throw new ScheduleWithConditionDifferentFromTimeException("");
                }
            }
        }
        this.type = type != null ? type : AutomationType.AUTOMATION; 
    }

    /**
     * Gets a copy of the conditions associated with this automation.
     *
     * @return a new list containing copies of the current conditions
     */
    public List<Condition> getConditions() {
        return this.conditions.stream().map(Condition::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Replaces this automation's conditions.
     * The provided conditions are copied before being stored.
     *
     * @param conditions the conditions to assign, or null for an empty list
     * @throws ScheduleWithConditionDifferentFromTimeException if a schedule receives non-time conditions
     */
    public void setConditions(List<Condition> conditions) throws ScheduleWithConditionDifferentFromTimeException {
        if (conditions != null) {
            if (this.type == AutomationType.SCHEDULE) {
                for (Condition c : conditions) {
                    if (!(c instanceof TimeBasedCondition)) {
                        throw new ScheduleWithConditionDifferentFromTimeException("");
                    }
                }
            }
            this.conditions = conditions.stream().map(Condition::copy).collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.conditions = new ArrayList<>();
        }
    }

    /**
     * Checks whether this automation's conditions were met in the previous evaluation.
     *
     * @return true if the conditions were met during the last check, false otherwise.
     */
    public boolean isWasConditionMetPreviously() {
        return this.wasConditionMetPreviously;
    }

    /**
     * Sets whether this automation's conditions were met in the previous evaluation.
     *
     * @param wasConditionMetPreviously the previous condition state
     */
    public void setWasConditionMetPreviously(boolean wasConditionMetPreviously) {
        this.wasConditionMetPreviously = wasConditionMetPreviously;
    }

    /**
     * Adds a condition to this automation.
     * The provided condition is copied before being stored.
     *
     * @param condition the condition to add
     * @throws ScheduleWithConditionDifferentFromTimeException if a schedule receives a non-time condition
     */
    public void addCondition(Condition condition) throws ScheduleWithConditionDifferentFromTimeException {
        if (condition != null) {
            if (this.type == AutomationType.SCHEDULE && !(condition instanceof TimeBasedCondition)) {
                throw new ScheduleWithConditionDifferentFromTimeException("");
            }
            this.conditions.add(condition.copy());
        }
    }

    /**
     * Removes a condition from this automation.
     *
     * @param condition the condition to remove
     * @return true if the condition was found and removed; false otherwise.
     */
    public boolean removeCondition(Condition condition) {
        if (condition != null && this.conditions != null) {
            return this.conditions.remove(condition);
        }
        return false;
    }

    /**
     * Evaluates all conditions and executes the actions if the conditions have just become true.
     * This prevents actions from firing repeatedly while the conditions remain true.
     *
     * @param house the house where device conditions are evaluated and actions are executed
     * @param state the current simulation state
     * @return true if the automation was triggered, false otherwise
     */
    public boolean checkAndTrigger(House house, SimulationState state) {
        if (this.conditions.isEmpty()) {
            return false;
        }

        boolean allConditionsMet = true;

        for (Condition condition : this.conditions) {
            if (!condition.evaluate(house, state)) {
                allConditionsMet = false;
                break;
            }
        }

        boolean triggered = false;

        if (allConditionsMet && !this.wasConditionMetPreviously) {
            this.executeActions(house);
            triggered = true;
        }

        this.wasConditionMetPreviously = allConditionsMet;

        return triggered;
    }

    /**
     * Removes all actions and conditions that target the given device.
     * Used when a device is removed from the house.
     *
     * @param deviceId the device identifier to remove references to
     */
    @Override
    public void removeDeviceById(int deviceId) {
        super.removeDeviceById(deviceId);
        if (this.conditions != null) {
            this.conditions.removeIf(condition -> condition.hasDeviceId(deviceId));
        }
    }

    /**
     * Compares this automation with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the type, conditions, and superclass fields match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Automation automation = (Automation) o;
        
        return super.equals(automation) &&
               this.type == automation.getType() &&
               Objects.equals(this.conditions, automation.getConditions());
    }

    /**
     * Generates a hash code for this automation.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.type, this.conditions);
    }

    /**
     * Creates a copy of this automation.
     *
     * @return a copied Automation instance
     */
    @Override
    public Automation clone() {
        return new Automation(this);
    }

    /**
     * Returns a string representation of the automation.
     *
     * @return a formatted string with the automation information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Automation { ")
          .append("Name: '").append(this.getName()).append('\'')
          .append(", Type: ").append(this.type)
          .append(", Conditions: ").append(this.conditions != null ? this.conditions.size() : 0)
          .append(", Actions: ").append(this.getActions() != null ? this.getActions().size() : 0)
          .append(" }");
        return sb.toString();
    }
}
