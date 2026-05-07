package domuscontrol.user;

import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    @BeforeEach
    void setUp() {
        User.setNextId(0);
    }

    @Test
    void createUserRegistersUserByIdAndEmail() throws Exception {
        UserManager manager = new UserManager();

        User user = manager.createUser("Ada", "ada@example.com", "secret");

        assertEquals("Ada", user.getName());
        assertTrue(manager.existsUserWithId(user.getId()));
        assertTrue(manager.existsUserWithEmail("ADA@example.com"));
        assertEquals(user, manager.getUserById(user.getId()));
        assertEquals(user, manager.getUserByEmail("ada@example.com"));
    }

    @Test
    void createUserRejectsDuplicateEmailCaseInsensitive() throws Exception {
        UserManager manager = new UserManager();
        manager.createUser("Ada", "ada@example.com", "secret");

        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> manager.createUser("Other", "ADA@example.com", "secret")
        );

        assertEquals("ADA@example.com", exception.getMessage());
    }

    @Test
    void gettersThrowWhenUserDoesNotExist() {
        UserManager manager = new UserManager();

        assertThrows(UserNotFoundException.class, () -> manager.getUserById(99));
        assertThrows(UserNotFoundException.class, () -> manager.getUserByEmail("missing@example.com"));
    }

    @Test
    void returnedUsersAreClones() throws Exception {
        UserManager manager = new UserManager();
        User user = manager.createUser("Ada", "ada@example.com", "secret");

        user.setName("Changed");
        manager.getUserById(user.getId()).setName("Also changed");

        assertEquals("Ada", manager.getUserById(user.getId()).getName());
    }

    @Test
    void updateUserChangesStoredDataAndEmailIndex() throws Exception {
        UserManager manager = new UserManager();
        User user = manager.createUser("Ada", "ada@example.com", "secret");
        user.setName("Ada Lovelace");
        user.setEmail("lovelace@example.com");

        manager.updateUser(user);

        assertFalse(manager.existsUserWithEmail("ada@example.com"));
        assertTrue(manager.existsUserWithEmail("LOVELACE@example.com"));
        assertEquals("Ada Lovelace", manager.getUserByEmail("lovelace@example.com").getName());
    }

    @Test
    void updateUserRejectsMissingUserAndDuplicateEmail() throws Exception {
        UserManager manager = new UserManager();
        User first = manager.createUser("Ada", "ada@example.com", "secret");
        User second = manager.createUser("Grace", "grace@example.com", "secret");
        User missing = new User("Missing", "missing@example.com", "secret", java.util.Map.of());

        assertThrows(UserNotFoundException.class, () -> manager.updateUser(missing));

        first.setEmail(second.getEmail());
        assertThrows(UserAlreadyExistsException.class, () -> manager.updateUser(first));
    }

    @Test
    void getAllUsersReturnsClones() throws Exception {
        UserManager manager = new UserManager();
        manager.createUser("Ada", "ada@example.com", "secret");

        manager.getAllUsers().get(0).setName("Changed");

        assertEquals("Ada", manager.getAllUsers().get(0).getName());
    }
}
