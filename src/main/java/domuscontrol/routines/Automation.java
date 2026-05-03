package domuscontrol.routines;

import domuscontrol.simulation.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.houses.House;
import domuscontrol.routines.conditions.TimeBasedCondition;

/**
 * Represents an automation or schedule routine within the system.
 * This class extends {@link Routine} by adding a list of conditions that must be met
 * for the associated actions to be triggered automatically.
 */
public class Automation extends Routine {
    
    private AutomationType type;
    private List<Condition> conditions;
    private boolean wasConditionMetPreviously;

    /**
     * Default constructor.
     * Initializes an empty automation of type AUTOMATION with no conditions.
     */
    public Automation() {
        super();
        this.type = AutomationType.AUTOMATION;
        this.conditions = new ArrayList<>();
        this.wasConditionMetPreviously = false;
    }

    /**
     * Parameterized constructor.
     * @param name       The name of the automation.
     * @param type       The type of routine (AUTOMATION or SCHEDULE).
     * @param conditions The list of conditions required to trigger the actions.
     * @param actions    The list of actions to execute when conditions are met.
     * @throws ScheduleWithConditionDifferentFromTimeException if a SCHEDULE type is assigned non-time conditions.
     */
    public Automation(String name, AutomationType type, List<Condition> conditions, List<Action> actions) throws ScheduleWithConditionDifferentFromTimeException {
        super(name, actions);
        this.type = type != null ? type : AutomationType.AUTOMATION;
        this.setConditions(conditions);
        this.wasConditionMetPreviously = false;
    }

    /**
     * Copy constructor for deep copying.
     * Notice we are strictly using getters to access the 'other' object's data!
     * @param other The existing Automation instance to copy.
     */
    public Automation(Automation other){
        super(other);
        this.type = other.getType();
        this.conditions = other.getConditions();
        this.wasConditionMetPreviously = other.isWasConditionMetPreviously();
    }

    /**
     * Retrieves the routine type.
     * @return The current AutomationType.
     */
    public AutomationType getType() { return type; }
    
    /**
     * Sets the routine type and validates existing conditions.
     * @param type The new AutomationType to set.
     * @throws ScheduleWithConditionDifferentFromTimeException if the type is set to SCHEDULE while containing non-time conditions.
     */
    public void setType(AutomationType type) throws ScheduleWithConditionDifferentFromTimeException { 
        if (type == AutomationType.SCHEDULE) {
            for (Condition c : this.conditions) {
                if (!(c instanceof TimeBasedCondition)) {
                    throw new ScheduleWithConditionDifferentFromTimeException("Cannot change type to SCHEDULE because non-time conditions already exist.");
                }
            }
        }
        this.type = type != null ? type : AutomationType.AUTOMATION; 
    }

    /**
     * Retrieves a deep copy of the conditions list.
     * @return A new list containing copies of the current conditions.
     */
    public List<Condition> getConditions() {
        return this.conditions.stream().map(Condition::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Sets the conditions for this automation.
     * @param conditions The list of conditions to be applied.
     * @throws ScheduleWithConditionDifferentFromTimeException if the type is SCHEDULE and a non-time condition is provided.
     */
    public void setConditions(List<Condition> conditions) throws ScheduleWithConditionDifferentFromTimeException {
        if (conditions != null) {
            if (this.type == AutomationType.SCHEDULE) {
                for (Condition c : conditions) {
                    if (!(c instanceof TimeBasedCondition)) {
                        throw new ScheduleWithConditionDifferentFromTimeException("Cannot change type to SCHEDULE because non-time conditions already exist.");
                    }
                }
            }
            this.conditions = conditions.stream().map(Condition::copy).collect(Collectors.toCollection(ArrayList::new));
        } else {
            this.conditions = new ArrayList<>();
        }
    }

    /**
     * Retrieves the state of the previous condition evaluation.
     * @return true if the conditions were met during the last check, false otherwise.
     */
    public boolean isWasConditionMetPreviously() {
        return this.wasConditionMetPreviously;
    }

    /**
     * Sets the state of the previous condition evaluation.
     * @param wasConditionMetPreviously The state to set.
     */
    public void setWasConditionMetPreviously(boolean wasConditionMetPreviously) {
        this.wasConditionMetPreviously = wasConditionMetPreviously;
    }

    /**
     * Adds a single condition to the routine.
     * @param condition The condition to add.
     * @throws ScheduleWithConditionDifferentFromTimeException if adding a non-time condition to a SCHEDULE.
     */
    public void addCondition(Condition condition) throws ScheduleWithConditionDifferentFromTimeException {
        if (condition != null) {
            if (this.type == AutomationType.SCHEDULE && !(condition instanceof TimeBasedCondition)) {
                throw new ScheduleWithConditionDifferentFromTimeException("Cannot change type to SCHEDULE because non-time conditions already exist.");
            }
            this.conditions.add(condition.copy());
        }
    }

    /**
     * Removes a specific condition from the routine.
     * @param condition The condition to remove.
     * @return true if the condition was found and removed; false otherwise.
     */
    public boolean removeCondition(Condition condition) {
        if (condition != null && this.conditions != null) {
            return this.conditions.remove(condition);
        }
        return false;
    }

    /**
     * Evaluates all conditions and executes actions if the state transitions from false to true.
     * This prevents actions from firing repeatedly while conditions remain met (edge-triggering).
     * * Notice: No parameters needed! It just tells the actions and conditions to do their job.
     */
    public boolean checkAndTrigger(House house, Simulation simulation) {
        if (this.conditions.isEmpty()) {
            return false;
        }

        boolean allConditionsMet = true;

        for (Condition condition : this.conditions) {
            if (!condition.evaluate(house, simulation)) {
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
     * Compares this automation with another object for equality.
     * Notice we are strictly using getters to access the 'that' object's data!
     * @param o The object to compare with.
     * @return true if the type, conditions, and superclass fields match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Automation that = (Automation) o;
        
        return this.type == that.getType() && 
               Objects.equals(this.conditions, that.getConditions());
    }

    /**
     * Generates a hash code for this automation.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.type, this.conditions);
    }

    /**
     * Creates a deep copy of this automation instance.
     * @return A cloned Automation object.
     */
    @Override
    public Automation clone() {
        return new Automation(this);
    }

    /**
     * Returns a string representation of the automation.
     * @return A formatted string with routine details.
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

    /**
     * Safely removes all actions and conditions associated with a specific device ID.
     * This is used during a cascade delete when a device is removed from the house.
     * @param deviceId The unique identifier of the device to remove references for.
     */
    @Override
    public void removeDeviceById(int deviceId) {
        super.removeDeviceById(deviceId);
        if (this.conditions != null) {
            this.conditions.removeIf(condition -> condition.hasDeviceId(deviceId));
        }
    }
}
