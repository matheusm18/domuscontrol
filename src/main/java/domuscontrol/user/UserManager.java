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

    /**
     * Creates a new user with the given details and registers it in the system.
     *
     * @param name     the full name of the new user
     * @param email    the email address of the new user
     * @param password the password for the new user
     * @return a clone of the newly created {@link User}
     * @throws UserAlreadyExistsException if a user with the given email is already registered
     */
    public User createUser(String name, String email, String password) throws UserAlreadyExistsException {
        if (existsUserWithEmail(email)) {
            throw new UserAlreadyExistsException("This email is already registered: " + email);
        }
        User newUser = new User(name, email, password, new HashMap<>());
        this.registerUser(newUser);
        return newUser.clone();
    }

    /**
     * Registers an existing user object into the internal data structures.
     *
     * @param user the {@link User} object to register
     * @throws UserAlreadyExistsException if a user with the same ID or email already exists
     */
    private void registerUser(User user) throws UserAlreadyExistsException {
        if (existsUserWithId(user.getId())) {
            throw new UserAlreadyExistsException("User ID already exists: " + user.getId());
        }
        if (existsUserWithEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("This email is already registered: " + user.getEmail());
        }
        this.usersById.put(user.getId(), user);
        this.usersByEmail.put(user.getEmail().toLowerCase(), user);
    }

    /**
     * Checks whether a user with the given email address is registered in the system.
     *
     * @param email the email address to look up (case-insensitive)
     * @return {@code true} if a user with that email exists, {@code false} otherwise
     */
    public boolean existsUserWithEmail(String email) {
        return this.usersByEmail.containsKey(email.toLowerCase());
    }

    /**
     * Checks whether a user with the given ID is registered in the system.
     *
     * @param id the user ID to look up
     * @return {@code true} if a user with that ID exists, {@code false} otherwise
     */
    public boolean existsUserWithId(int id) {
        return this.usersById.containsKey(id);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the ID of the user to retrieve
     * @return a clone of the {@link User} associated with the given ID
     * @throws UserNotFoundException if no user with the given ID exists
     */
    public User getUserById(int userId) throws UserNotFoundException {
        User user = this.usersById.get(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return user.clone();
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to retrieve (case-insensitive)
     * @return a clone of the {@link User} associated with the given email
     * @throws UserNotFoundException if no user with the given email exists
     */
    public User getUserByEmail(String email) throws UserNotFoundException {
        User user = this.usersByEmail.get(email.toLowerCase());
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return user.clone();
    }

    /**
     * Updates the stored data for an existing user.
     * If the user's email has changed, the email index is updated accordingly.
     *
     * @param updatedUser a {@link User} object containing the updated data; must have an existing ID
     * @throws UserNotFoundException      if no user with the given ID exists
     * @throws UserAlreadyExistsException if the new email address is already in use by another user
     */
    public void updateUser(User updatedUser) throws UserNotFoundException, UserAlreadyExistsException {
        User oldUser = this.usersById.get(updatedUser.getId());
        if (oldUser == null) {
            throw new UserNotFoundException("Cannot update: User ID " + updatedUser.getId() + " does not exist.");
        }
        String oldEmail = oldUser.getEmail().toLowerCase();
        String newEmail = updatedUser.getEmail().toLowerCase();
        if (!oldEmail.equals(newEmail)) {
            if (existsUserWithEmail(newEmail)) {
                throw new UserAlreadyExistsException("Cannot change email. New email already in use: " + newEmail);
            }
            this.usersByEmail.remove(oldEmail);
        }
        User user = updatedUser.clone();
        this.usersById.put(user.getId(), user);
        this.usersByEmail.put(newEmail, user);
    }

    /**
     * Returns a list of all users currently registered in the system.
     *
     * @return a {@link List} of cloned {@link User} objects representing all registered users
     */
    public List<User> getAllUsers() {
        return this.usersById.values().stream()
                .map(User::clone)
                .collect(Collectors.toList());
    }
}