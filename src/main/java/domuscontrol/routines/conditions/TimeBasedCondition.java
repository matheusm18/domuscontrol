package domuscontrol.routines.conditions;

import domuscontrol.routines.Condition;

/**
 * A marker interface used to categorize conditions that are purely based on time.
 * This allows the system to easily verify if a routine qualifies as a SCHEDULE.
 */
public interface TimeBasedCondition extends Condition {
}