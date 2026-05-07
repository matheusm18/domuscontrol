package domuscontrol.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import domuscontrol.exceptions.HouseNotFoundException;

/**
 * Represents a user in the system.
 * Stores user details, credentials, and roles for house access.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class User implements Serializable {

    /** The next ID to be assigned to a user. */
    private static int nextId = 1;

    /** The unique ID of the user. */
    private final int id;
    /** The name of the user. */
    private String name;
    /** The email of the user. */
    private String email;
    /** The password of the user. */
    private String password;
    /** A map of roles by house ID. */
    private Map<Integer, UserRole> rolesByHouseId;

    /**
     * Updates the static ID counter.
     * Useful when loading a saved system state to ensure new users do not overlap
     * with previously assigned IDs.
	 * 
     * @param lastId The last assigned ID to set the counter to.
	 */
    public static void setNextId(int lastId) {
        nextId = lastId + 1;
    }

    /** Creates a new empty user. */
    public User() {
        this.id = nextId++;
        this.name = "";
        this.email = "";
        this.password = "";
        this.rolesByHouseId = new HashMap<>();
    }

    /**
     * Constructor for User class.
     * @param name The user's name.
     * @param email The user's email.
     * @param password The user's password.
     * @param rolesByHouseId The map of roles by house ID.
     */
    public User(String name, String email, String password, Map<Integer, UserRole> rolesByHouseId) {
        this.id = nextId++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rolesByHouseId = new HashMap<>(rolesByHouseId);
    }

    /**
     * Creates a new User instance as a copy of the provided User instance.
     * @param other The User instance to copy.
     */
    public User(User other) {
        this.id = other.getId();
        this.name = other.getName();
        this.email = other.getEmail();
        this.password = other.getPassword();
        this.rolesByHouseId = other.getRolesByHouseId();
    }

    /**
     * Gets the unique ID of the user.
	 * 
     * @return The user's ID.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the name of the user.
	 * 
     * @return The user's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the email of the user.
	 * 
     * @return The user's email.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Gets the password of the user.
	 * 
     * @return The user's password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the IDs of all houses where the user has a role.
     *
     * @return A set of house IDs where the user has a role.
     */
    public Set<Integer> getHouseIds() {
        return new HashSet<>(this.rolesByHouseId.keySet());
    }

    /**
     * Gets the roles by house ID.
     *
     * @return A map where the key is the house ID and the value is the user's role in that house.
     */
    public Map<Integer, UserRole> getRolesByHouseId() {
        return new HashMap<>(this.rolesByHouseId);
    }

    /**
     * Sets the name of the user.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the email of the user.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the password of the user.
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the roles by house ID.
     * @param rolesByHouseId The map of roles by house ID.
     */
    public void setRolesByHouseId(Map<Integer, UserRole> rolesByHouseId) {
        this.rolesByHouseId = new HashMap<>(rolesByHouseId);
    }

    /**
     * Checks if the user has a role in the specified house.
     * @param houseId The ID of the house.
     * @return true if the user has a role in the specified house, false otherwise.
     */
    public boolean hasRoleInHouse(int houseId) {
        return this.rolesByHouseId.containsKey(houseId);
    }

    /**
     * Assigns a role to the user for a specific house.
     * @param houseId The ID of the house.
     * @param role The role to assign.
     */
    public void assignRole(int houseId, UserRole role) {
        this.rolesByHouseId.put(houseId, role);
    }

    /**
     * Removes the user's role for a specific house.
     * @param houseId The ID of the house.
     * @throws HouseNotFoundException if the user has no role in the specified house.
     */
    public void removeRole(int houseId) throws HouseNotFoundException {
        if (!this.rolesByHouseId.containsKey(houseId)) throw new HouseNotFoundException("" + houseId);
        this.rolesByHouseId.remove(houseId);
    }

    /**
     * Gets the role of the user for a specific house.
     * @param houseId The ID of the house.
     * @return The role of the user for the specified house.
     * @throws HouseNotFoundException if the user has no role in the specified house.
     */
    public UserRole getRoleForHouse(int houseId) throws HouseNotFoundException {
        UserRole role = this.rolesByHouseId.get(houseId);
        if (role == null) throw new HouseNotFoundException("" + houseId);
        return role;
    }

    /**
     * Checks if the user is an administrator for a specific house.
     * @param houseId The ID of the house.
     * @return true if the user is an administrator for the specified house, false otherwise.
     * @throws HouseNotFoundException if the user has no role in the specified house.
     */
    public boolean isAdminForHouse(int houseId) throws HouseNotFoundException {
        return getRoleForHouse(houseId) == UserRole.ADMINISTRATOR;
    }

    /**
     * Creates and returns a copy of this User instance.
     * @return A new User instance that is a clone of this instance.
     */
    @Override
    public User clone() {
        return new User(this);
    }

    /**
     * Compares this User instance with another object for equality.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        User user = (User) o;
        return this.id == user.id &&
               this.name.equals(user.name) &&
               this.email.equals(user.email) &&
               this.password.equals(user.password) &&
               this.rolesByHouseId.equals(user.rolesByHouseId);
    }

    /**
     * Computes the hash code for this User instance.
     * @return The hash code of this User instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.email, this.password, this.rolesByHouseId);
    }

    /**
     * Returns a string representation of this User instance.
     * @return A string representing the User instance.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User").append("\n")
          .append("ID: ").append(this.id).append("\n")
          .append("Name: ").append(this.name).append("\n")
          .append("Email: ").append(this.email).append("\n")
          .append("Roles: ").append(this.rolesByHouseId).append("\n");
        return sb.toString();
    }
}
