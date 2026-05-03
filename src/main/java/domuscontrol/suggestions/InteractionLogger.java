package domuscontrol.suggestions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Stores the history of all manual device interactions performed in a house.
 * Lives inside House and is serialized alongside it.
 */
public class InteractionLogger implements Serializable {

    /** The ordered list of all recorded interactions, oldest first. */
    private List<DeviceInteraction> interactions;

    /**
     * Default constructor initializing an empty log.
     */
    public InteractionLogger() {
        this.interactions = new ArrayList<>();
    }

    /**
     * Parameterized constructor.
     *
     * @param interactions The initial list of interactions to store (deep copied).
     */
    public InteractionLogger(List<DeviceInteraction> interactions) {
        this.setInteractions(interactions);
    }

    /**
     * Copy constructor using getters to access the other instance's state.
     *
     * @param other The existing InteractionLogger instance to copy.
     */
    public InteractionLogger(InteractionLogger other) {
        this.setInteractions(other.getInteractions());
    }

    /**
     * Returns an unmodifiable view of all recorded interactions.
     *
     * @return The full interaction list.
     */
    public List<DeviceInteraction> getInteractions() {
        List<DeviceInteraction> copy = new ArrayList<>();
        for (DeviceInteraction i : this.interactions) {
            copy.add(i.clone());
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * Replaces the entire interaction list with a deep copy of the provided one.
     *
     * @param interactions The new list of interactions.
     */
    public void setInteractions(List<DeviceInteraction> interactions) {
        this.interactions = new ArrayList<>();
        if (interactions != null) {
            for (DeviceInteraction i : interactions) {
                this.interactions.add(i.clone());
            }
        }
    }

    /**
     * Records a new interaction in the log.
     *
     * @param interaction The interaction to record.
     */
    public void log(DeviceInteraction interaction) {
        if (interaction != null)
            this.interactions.add(interaction.clone());
    }


    /**
     * Returns the total number of recorded interactions.
     *
     * @return The interaction count.
     */
    public int size() {
        return this.interactions.size();
    }

    /**
     * Removes all recorded interactions from the log.
     */
    public void clear() {
        this.interactions.clear();
    }

    /**
     * Creates a deep copy of this logger.
     *
     * @return A new InteractionLogger instance.
     */
    @Override
    public InteractionLogger clone() {
        return new InteractionLogger(this);
    }

    /**
     * Compares this logger with another object for equality using getters.
     *
     * @param o The object to compare with.
     * @return true if the interaction lists are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        InteractionLogger that = (InteractionLogger) o;
        return Objects.equals(this.getInteractions(), that.getInteractions());
    }

    /**
     * Generates a hash code for this logger.
     *
     * @return The hash code based on the interaction list.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getInteractions());
    }

    /**
     * Returns a string representation of this logger.
     *
     * @return Formatted string with the total number of recorded interactions.
     */
    @Override
    public String toString() {
        return "InteractionLogger { totalInteractions=" + this.size() + " }";
    }
}