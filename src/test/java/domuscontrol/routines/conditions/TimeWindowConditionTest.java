package domuscontrol.routines.conditions;

import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeWindowConditionTest {

    @Test
    void defaultConstructorUsesOneMinuteWindowAtMidnight() {
        TimeWindowCondition condition = new TimeWindowCondition();

        assertEquals(LocalTime.MIDNIGHT, condition.getStartTime());
        assertEquals(LocalTime.MIDNIGHT.plusMinutes(1), condition.getEndTime());
    }

    @Test
    void constructorAndSettersDefineWindow() {
        TimeWindowCondition condition = new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertEquals(LocalTime.of(8, 0), condition.getStartTime());
        assertEquals(LocalTime.of(10, 0), condition.getEndTime());

        condition.setStartTime(LocalTime.of(9, 0));
        condition.setEndTime(LocalTime.of(11, 0));

        assertEquals(LocalTime.of(9, 0), condition.getStartTime());
        assertEquals(LocalTime.of(11, 0), condition.getEndTime());
    }

    @Test
    void evaluateMatchesInclusiveRegularWindow() {
        TimeWindowCondition condition = new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertTrue(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 8, 0))));
        assertTrue(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 9, 0))));
        assertTrue(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 10, 0))));
        assertFalse(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 10, 1))));
    }

    @Test
    void evaluateMatchesWindowThatCrossesMidnight() {
        TimeWindowCondition condition = new TimeWindowCondition(LocalTime.of(22, 0), LocalTime.of(4, 0));

        assertTrue(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 23, 0))));
        assertTrue(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 2, 3, 0))));
        assertFalse(condition.evaluate(null, state(LocalDateTime.of(2026, 1, 1, 12, 0))));
    }

    @Test
    void evaluateReturnsFalseWhenDateOrWindowIsNull() {
        assertFalse(new TimeWindowCondition(null, LocalTime.NOON).evaluate(null, state(LocalDateTime.now())));
        assertFalse(new TimeWindowCondition(LocalTime.NOON, null).evaluate(null, state(LocalDateTime.now())));
        assertFalse(new TimeWindowCondition().evaluate(null, state(null)));
    }

    @Test
    void copyAndCloneKeepWindow() {
        TimeWindowCondition condition = new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0));

        Condition copy = condition.copy();
        TimeWindowCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassAndWindow() {
        assertEquals(
            new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0)),
            new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0))
        );
        assertNotEquals(
            new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0)),
            new TimeWindowCondition(LocalTime.of(9, 0), LocalTime.of(10, 0))
        );
        assertNotEquals(new TimeWindowCondition(), new TimeCondition());
    }

    @Test
    void toStringIncludesWindow() {
        assertEquals(
            "TimeWindowCondition { Start Time: 08:00, End Time: 10:00 }",
            new TimeWindowCondition(LocalTime.of(8, 0), LocalTime.of(10, 0)).toString()
        );
    }

    private SimulationState state(LocalDateTime now) {
        return new SimulationState(now, null, 20.0, 500.0, WeatherCondition.SUNNY);
    }
}
