package domuscontrol.routines.conditions;

import domuscontrol.routines.Condition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimeBasedConditionTest {

    @Test
    void timeConditionIsTimeBasedCondition() {
        TimeCondition condition = new TimeCondition();

        assertInstanceOf(TimeBasedCondition.class, condition);
        assertInstanceOf(Condition.class, condition);
    }

    @Test
    void timeWindowConditionIsTimeBasedCondition() {
        TimeWindowCondition condition = new TimeWindowCondition();

        assertInstanceOf(TimeBasedCondition.class, condition);
        assertInstanceOf(Condition.class, condition);
    }

    @Test
    void timeBasedConditionsDoNotDependOnDeviceByDefault() {
        Condition condition = new TimeCondition();

        assertFalse(condition.hasDeviceId(1));
    }
}
