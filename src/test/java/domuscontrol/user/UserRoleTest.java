package domuscontrol.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleTest {

    @Test
    void hasExpectedRolesInOrder() {
        assertArrayEquals(
            new UserRole[] { UserRole.ADMINISTRATOR, UserRole.USER },
            UserRole.values()
        );
    }

    @Test
    void valueOfReturnsMatchingRole() {
        assertEquals(UserRole.ADMINISTRATOR, UserRole.valueOf("ADMINISTRATOR"));
        assertEquals(UserRole.USER, UserRole.valueOf("USER"));
    }
}
