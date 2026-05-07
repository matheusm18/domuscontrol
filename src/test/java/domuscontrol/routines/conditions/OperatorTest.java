package domuscontrol.routines.conditions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorTest {

    @Test
    void hasExpectedOperatorsInOrder() {
        assertArrayEquals(
            new Operator[] {
                Operator.EQUALS,
                Operator.GREATER_THAN,
                Operator.LESS_THAN
            },
            Operator.values()
        );
    }

    @Test
    void valueOfReturnsMatchingOperator() {
        assertEquals(Operator.EQUALS, Operator.valueOf("EQUALS"));
        assertEquals(Operator.GREATER_THAN, Operator.valueOf("GREATER_THAN"));
        assertEquals(Operator.LESS_THAN, Operator.valueOf("LESS_THAN"));
    }
}
