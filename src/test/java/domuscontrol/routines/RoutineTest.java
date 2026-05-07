package domuscontrol.routines;

import domuscontrol.houses.House;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class RoutineTest {

    @Test
    void defaultConstructorCreatesUnnamedRoutineWithoutActions() {
        TestRoutine routine = new TestRoutine();

        assertEquals("Unnamed Routine", routine.getName());
        assertTrue(routine.getActions().isEmpty());
    }

    @Test
    void constructorAndSettersCopyActions() {
        TestAction action = new TestAction(1);
        List<Action> actions = new ArrayList<>(List.of(action));
        TestRoutine routine = new TestRoutine("Morning", actions);
        actions.clear();

        assertEquals("Morning", routine.getName());
        assertEquals(1, routine.getActions().size());

        routine.setName("Evening");
        routine.setActions(null);

        assertEquals("Evening", routine.getName());
        assertTrue(routine.getActions().isEmpty());
    }

    @Test
    void getActionsReturnsCopies() {
        TestRoutine routine = new TestRoutine("Morning", List.of(new TestAction(1)));

        List<Action> actions = routine.getActions();
        actions.clear();

        assertEquals(1, routine.getActions().size());
        assertNotSame(routine.getActions().get(0), routine.getActions().get(0));
    }

    @Test
    void addActionIgnoresNullAndStoresCopy() {
        TestAction action = new TestAction(1);
        TestRoutine routine = new TestRoutine();

        routine.addAction(null);
        routine.addAction(action);

        assertEquals(1, routine.getActions().size());
        assertNotSame(action, routine.getActions().get(0));
    }

    @Test
    void executeActionsExecutesStoredActionsInOrder() {
        List<Integer> executed = new ArrayList<>();
        TestRoutine routine = new TestRoutine(
            "Morning",
            List.of(new TestAction(1, executed), new TestAction(2, executed))
        );

        routine.executeActions(new House());

        assertEquals(List.of(1, 2), executed);
    }

    @Test
    void removeActionAndRemoveDeviceByIdUpdateActions() {
        TestAction first = new TestAction(1);
        TestAction second = new TestAction(2);
        TestRoutine routine = new TestRoutine("Morning", List.of(first, second));

        assertTrue(routine.removeAction(first));
        assertFalse(routine.removeAction(null));
        assertEquals(1, routine.getActions().size());

        routine.removeDeviceById(2);

        assertTrue(routine.getActions().isEmpty());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseRoutineFields() {
        TestRoutine routine = new TestRoutine("Morning", List.of(new TestAction(1)));

        TestRoutine copy = routine.clone();

        assertNotSame(routine, copy);
        assertEquals(routine, copy);
        assertEquals(routine.hashCode(), copy.hashCode());
        assertEquals("TestRoutine { Name: 'Morning', Actions: 1 }", routine.toString());
    }

    private static class TestRoutine extends Routine {
        TestRoutine() {
            super();
        }

        TestRoutine(String name, List<Action> actions) {
            super(name, actions);
        }

        TestRoutine(TestRoutine other) {
            super(other);
        }

        @Override
        public TestRoutine clone() {
            return new TestRoutine(this);
        }
    }

    private static class TestAction implements Action {
        private final int deviceId;
        private final List<Integer> executions;

        TestAction(int deviceId) {
            this(deviceId, new ArrayList<>());
        }

        TestAction(int deviceId, List<Integer> executions) {
            this.deviceId = deviceId;
            this.executions = executions;
        }

        TestAction(TestAction other) {
            this.deviceId = other.deviceId;
            this.executions = other.executions;
        }

        @Override
        public void execute(House house) {
            this.executions.add(this.deviceId);
        }

        @Override
        public Action copy() {
            return new TestAction(this);
        }

        @Override
        public boolean hasDeviceId(int deviceId) {
            return this.deviceId == deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            TestAction that = (TestAction) o;
            return this.deviceId == that.deviceId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.deviceId);
        }
    }
}
