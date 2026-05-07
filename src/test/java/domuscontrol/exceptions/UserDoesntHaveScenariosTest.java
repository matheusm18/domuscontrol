package domuscontrol.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDoesntHaveScenariosTest {

    @Test
    void constructorStoresMessage() {
        UserDoesntHaveScenarios exception = new UserDoesntHaveScenarios("User does not have scenarios");

        assertEquals("User does not have scenarios", exception.getMessage());
        assertInstanceOf(Exception.class, exception);
        assertNull(exception.getCause());
    }
}
