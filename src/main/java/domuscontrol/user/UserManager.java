package domuscontrol.user;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;

/**
 * User Manager class responsible for handling user-related operations such as registration, retrieval, and updates.
 * This class maintains two maps for efficient user management: one for users by ID and another for users by email.
 */
public class UserManager implements Serializable {

    private final Map<Integer, User> usersById;
    private final Map<String, User> usersByEmail;

    public UserManager() {
        this.usersById = new HashMap<>();
        this.usersByEmail = new HashMap<>();
    }

    /** Creates a new user with the given details and registers it in the system. */
    public User createUser(String name, String email, String password) {
        // reuse the exists check
        if (existsUserWithEmail(email)) {
            throw new UserAlreadyExistsException("This email is already registered: " + email);
        }

        User newUser = new User(name, email, password, new HashMap<>());
        this.registerUser(newUser);
        return newUser;
    }

    /** Registers an existing user object into the data structures. */
    public void registerUser(User user) {

        if (existsUserWithId(user.getId())) {
            throw new UserAlreadyExistsException("User ID already exists: " + user.getId());
        }

        if (existsUserWithEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("This email is already registered: " + user.getEmail());
        }

        User userClone = user.clone();

        this.usersById.put(userClone.getId(), userClone);
        this.usersByEmail.put(userClone.getEmail().toLowerCase(), userClone);
    }

    public boolean existsUserWithEmail(String email) {
        return this.usersByEmail.containsKey(email.toLowerCase());
    }

    public boolean existsUserWithId(int id) {
        return this.usersById.containsKey(id);
    }

    /** Returns a clone of the user by ID. */
    public User getUserById(int userId) {
        User user = this.usersById.get(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return user.clone();
    }

    /** Returns a clone of the user by email. */
    public User getUserByEmail(String email) {
        User user = this.usersByEmail.get(email.toLowerCase());
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return user.clone();
    }

    /** Updates the user data. */
    public void updateUser(User updatedUser) {

        User oldUser = this.usersById.get(updatedUser.getId());
            if (oldUser == null) {
                throw new UserNotFoundException("Cannot update: User ID " + updatedUser.getId() + " does not exist.");
            }
            
            // Handle email changes
            String oldEmail = oldUser.getEmail().toLowerCase();
            String newEmail = updatedUser.getEmail().toLowerCase();

            if (!oldEmail.equals(newEmail)) {
                if (existsUserWithEmail(newEmail)) {
                    throw new UserAlreadyExistsException("Cannot change email. New email already in use: " + newEmail);
                }
                this.usersByEmail.remove(oldEmail);
            }

            this.usersById.put(updatedUser.getId(), updatedUser.clone());
            this.usersByEmail.put(newEmail, updatedUser.clone());
    }
     
    /** Returns a list of all users. */
    public List<User> getAllUsers() {
        return this.usersById.values().stream()
                .map(User::clone)
                .collect(Collectors.toList());
    }
}