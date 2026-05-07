package domuscontrol.routines;

import domuscontrol.houses.House;
import domuscontrol.simulation.SimulationState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionTest {

    @Test
    void defaultHasDeviceIdReturnsFalse() {
        Condition condition = new TestCondition(true);

        assertFalse(condition.hasDeviceId(1));
    }

    @Test
    void conditionContractEvaluatesAndCopies() {
        Condition condition = new TestCondition(true);

        Condition copy = condition.copy();

        assertTrue(condition.evaluate(new House(), null));
        assertNotSame(condition, copy);
        assertTrue(copy.evaluate(new House(), null));
    }

    private static class TestCondition implements Condition {
        private final boolean result;

        TestCondition(boolean result) {
            this.result = result;
        }

        @Override
        public boolean evaluate(House house, SimulationState state) {
            return this.result;
        }

        @Override
        public Condition copy() {
            return new TestCondition(this.result);
        }
    }
}
