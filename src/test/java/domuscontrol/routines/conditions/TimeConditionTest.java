package domuscontrol.routines.conditions;

import domuscontrol.routines.Condition;
import domuscontrol.simulation.SimulationState;
import domuscontrol.simulation.SimulationStateStub;
import domuscontrol.simulation.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeConditionTest {

    @Test
    void defaultConstructorUsesMidnight() {
        TimeCondition condition = new TimeCondition();

        assertEquals(LocalTime.MIDNIGHT, condition.getTriggerTime());
    }

    @Test
    void constructorAndSetterDefineTriggerTime() {
        TimeCondition condition = new TimeCondition(LocalTime.of(8, 30));

        assertEquals(LocalTime.of(8, 30), condition.getTriggerTime());

        condition.setTriggerTime(LocalTime.of(9, 45));

        assertEquals(LocalTime.of(9, 45), condition.getTriggerTime());
    }

    @Test
    void evaluateReturnsTrueWhenTriggerWasCrossed() {
        TimeCondition condition = new TimeCondition(LocalTime.of(8, 30));

        assertTrue(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 1, 8, 45),
            LocalDateTime.of(2026, 1, 1, 8, 15)
        )));
    }

    @Test
    void evaluateReturnsFalseWhenTriggerWasNotCrossed() {
        TimeCondition condition = new TimeCondition(LocalTime.of(8, 30));

        assertFalse(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 1, 8, 20),
            LocalDateTime.of(2026, 1, 1, 8, 15)
        )));
    }

    @Test
    void evaluateHandlesMidnightCrossing() {
        TimeCondition condition = new TimeCondition(LocalTime.of(0, 15));

        assertTrue(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 2, 0, 30),
            LocalDateTime.of(2026, 1, 1, 23, 45)
        )));
    }

    @Test
    void evaluateWithNonAdvancingTimeOnlyMatchesExactCurrentTime() {
        TimeCondition condition = new TimeCondition(LocalTime.of(8, 30));

        assertTrue(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 1, 8, 30),
            LocalDateTime.of(2026, 1, 1, 8, 30)
        )));
        assertFalse(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 1, 8, 31),
            LocalDateTime.of(2026, 1, 1, 8, 31)
        )));
    }

    @Test
    void evaluateReturnsFalseWhenDatesOrTriggerAreNull() {
        TimeCondition condition = new TimeCondition((LocalTime) null);

        assertFalse(condition.evaluate(null, state(
            LocalDateTime.of(2026, 1, 1, 8, 30),
            LocalDateTime.of(2026, 1, 1, 8, 0)
        )));
        assertFalse(new TimeCondition(LocalTime.NOON).evaluate(null, state(null, null)));
    }

    @Test
    void copyAndCloneKeepTriggerTime() {
        TimeCondition condition = new TimeCondition(LocalTime.of(8, 30));

        Condition copy = condition.copy();
        TimeCondition clone = condition.clone();

        assertNotSame(condition, copy);
        assertEquals(condition, copy);
        assertEquals(condition.hashCode(), copy.hashCode());
        assertEquals(condition, clone);
    }

    @Test
    void equalsRequiresSameClassAndTriggerTime() {
        assertEquals(new TimeCondition(LocalTime.of(8, 30)), new TimeCondition(LocalTime.of(8, 30)));
        assertNotEquals(new TimeCondition(LocalTime.of(8, 30)), new TimeCondition(LocalTime.of(9, 0)));
        assertNotEquals(new TimeCondition(LocalTime.of(8, 30)), new TimeWindowCondition());
    }

    @Test
    void toStringIncludesTriggerTime() {
        assertEquals(
            "TimeCondition { Trigger Time: 08:30 }",
            new TimeCondition(LocalTime.of(8, 30)).toString()
        );
    }

    private SimulationState state(LocalDateTime now, LocalDateTime previous) {
        return new SimulationStateStub(now, previous, 20.0, 500.0, WeatherCondition.SUNNY);
    }
}
