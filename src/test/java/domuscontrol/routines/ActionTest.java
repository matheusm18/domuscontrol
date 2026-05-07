package domuscontrol.routines;

import domuscontrol.houses.House;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionTest {

    @Test
    void actionContractExecutesCopiesAndMatchesDeviceId() {
        TestAction action = new TestAction(3);
        House house = new House();

        action.execute(house);
        Action copy = action.copy();

        assertEquals(1, action.getExecutions());
        assertNotSame(action, copy);
        assertTrue(copy.hasDeviceId(3));
        assertFalse(copy.hasDeviceId(4));
    }

    private static class TestAction implements Action {
        private final int deviceId;
        private int executions;

        TestAction(int deviceId) {
            this.deviceId = deviceId;
        }

        TestAction(TestAction other) {
            this.deviceId = other.deviceId;
            this.executions = other.executions;
        }

        int getExecutions() {
            return this.executions;
        }

        @Override
        public void execute(House house) {
            this.executions++;
        }

        @Override
        public Action copy() {
            return new TestAction(this);
        }

        @Override
        public boolean hasDeviceId(int deviceId) {
            return this.deviceId == deviceId;
        }
    }
}
