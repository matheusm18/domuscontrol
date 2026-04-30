package domuscontrol.model.routines;

import java.util.List;

/**
 * Represents a predefined set of actions that can be triggered manually by a user.
 * Unlike Automations, Scenarios do not have conditions and must be activated
 * explicitly to execute their sequence of actions.
 */
public class Scenario extends Routine {

    /**
     * Default constructor.
     * Creates an unnamed scenario with an empty action list.
     */
    public Scenario() {
        super();
    }

    /**
     * Parameterized constructor.
     * * @param name    The name assigned to the scenario.
     * @param actions The list of actions to be executed when the scenario is triggered.
     */
    public Scenario(String name, List<Action> actions) {
        super(name, actions);
    }

    /**
     * Copy constructor for deep copying.
     * * @param other The existing Scenario instance to copy.
     */
    public Scenario(Scenario other) {
        super(other);
    }

    /**
     * Executes all actions contained within this scenario in the order they were added.
     * * @param house The house context where the actions should be performed.
     */
    public void executeScenario() {
        for (Action action : this.getActions()) {
            action.execute();
        }
    }

    /**
     * Creates a deep copy of this scenario instance.
     * * @return A cloned Scenario object.
     */
    @Override
    public Scenario clone() {
        return new Scenario(this);
    }

    /**
     * Returns a string representation of the scenario, including its name and action count.
     * * @return A formatted string describing the scenario.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(); 
        sb.append("Scenario: ").append(this.getName()).append("\n");
        sb.append(super.toString()); 
        return sb.toString(); 
    }
}