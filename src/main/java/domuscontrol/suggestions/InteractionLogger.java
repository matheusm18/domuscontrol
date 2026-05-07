package domuscontrol.suggestions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Stores the history of all manual device interactions performed in a house.
 * The logger is stored inside a house and is serialized with it.
 * Used by the SuggestionEngine to detect patterns and generate automation recommendations.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class InteractionLogger implements Serializable {

    /** The ordered list of all recorded interactions, oldest first. */
    private List<DeviceInteraction> interactions;

    /**
     * Creates an empty interaction logger.
     */
    public InteractionLogger() {
        this.interactions = new ArrayList<>();
    }

    /**
     * Creates an interaction logger with the given interactions.
     * The provided interactions are copied before being stored.
     *
     * @param interactions the interactions to store
     */
    public InteractionLogger(List<DeviceInteraction> interactions) {
        this.setInteractions(interactions);
    }

    /**
     * Creates a copy of another interaction logger.
     *
     * @param other the logger to copy
     */
    public InteractionLogger(InteractionLogger other) {
        this.setInteractions(other.getInteractions());
    }

    /**
     * Gets a copied, unmodifiable list of all recorded interactions.
     *
     * @return the copied interaction list
     */
    public List<DeviceInteraction> getInteractions() {
        List<DeviceInteraction> copy = new ArrayList<>();
        for (DeviceInteraction i : this.interactions) {
            copy.add(i.clone());
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * Replaces the interaction list.
     * The provided interactions are copied before being stored.
     *
     * @param interactions the interactions to store
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
     * Records a new interaction.
     *
     * @param interaction the interaction to record
     */
    public void log(DeviceInteraction interaction) {
        if (interaction != null) {
            this.interactions.add(interaction.clone());
        }
    }


    /**
     * Returns the total number of recorded interactions.
     *
     * @return the interaction count
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
     * Creates a copy of this logger.
     *
     * @return a copied InteractionLogger instance
     */
    @Override
    public InteractionLogger clone() {
        return new InteractionLogger(this);
    }

    /**
     * Compares this logger with another object for equality.
     *
     * @return true if the interaction lists are equal; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        InteractionLogger logger = (InteractionLogger) o;
        return Objects.equals(this.getInteractions(), logger.getInteractions());
    }

    /**
     * Generates a hash code for this logger.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getInteractions());
    }

    /**
     * Returns a string representation of this logger.
     *
     * @return a formatted string with the logger information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InteractionLogger { ")
          .append("Total Interactions: ").append(this.size())
          .append(" }");
        return sb.toString();
    }
}
