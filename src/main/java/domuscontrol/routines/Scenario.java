package domuscontrol.routines;

import java.util.List;

import domuscontrol.houses.House;

/**
 * Represents a routine that is triggered manually by a user.
 * A scenario has actions but no conditions, allowing users to execute
 * a predefined sequence of actions on demand.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Scenario extends Routine {

    /**
     * Creates an unnamed scenario with no actions.
     */
    public Scenario() {
        super();
    }

    /**
     * Creates a scenario with the given name and actions.
     * The provided actions are copied by the superclass.
     *
     * @param name the scenario name
     * @param actions the actions to execute when the scenario is triggered
     */
    public Scenario(String name, List<Action> actions) {
        super(name, actions);
    }

    /**
     * Creates a copy of another scenario.
     *
     * @param other the scenario to copy
     */
    public Scenario(Scenario other) {
        super(other);
    }

    /**
     * Executes all actions contained in this scenario.
     *
     * @param house the house where the scenario actions should be applied
     */
    public void executeScenario(House house) {
        this.executeActions(house);
    }

    /**
     * Creates a copy of this scenario.
     *
     * @return a copied Scenario instance
     */
    @Override
    public Scenario clone() {
        return new Scenario(this);
    }

    /**
     * Compares this scenario with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the superclass fields match; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Generates a hash code for this scenario.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this scenario.
     *
     * @return a formatted string with the scenario information
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
