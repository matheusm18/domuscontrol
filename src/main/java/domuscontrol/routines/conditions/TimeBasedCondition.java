package domuscontrol.routines.conditions;

import domuscontrol.routines.Condition;

/**
 * A marker interface used to categorize conditions that are purely based on time.
 * This allows the system to easily verify if a routine qualifies as a SCHEDULE. * Time-based conditions depend only on simulation time and not on device states.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0 */
public interface TimeBasedCondition extends Condition {
}