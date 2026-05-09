package domuscontrol.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @BeforeEach
    void setUp() {
        // Resetting the static ID counter to ensure deterministic behavior across tests
        User.setNextId(0);
    }

    @Test
    void testEmptyConstructor() {
        User user = new User();
        
        assertEquals(1, user.getId());
        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("securePassword123");
        
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("securePassword123", user.getPassword());
    }

    @Test
    void testMultipleUsersIdIncrement() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        assertEquals(1, user1.getId());
        assertEquals(2, user2.getId());
        assertEquals(3, user3.getId());
    }
}
