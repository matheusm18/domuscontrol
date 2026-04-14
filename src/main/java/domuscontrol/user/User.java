package domuscontrol.user;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user in the system.
 */
public class User {
	private static int nextId = 1;

	private final int id;
	private String name;
	private String email;
	private String password;
	private Map<Integer, UserRole> rolesByHouseId;

    //------------------- Constructors ------------------//

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
	public User(
			String name,
			String email,
			String password,
			Map<Integer, UserRole> rolesByHouseId
	) {
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

    //------------------- Getters and Setters ------------------//

    /**
     * Gets the unique ID of the user.
     * @return The user's ID.
     */
	public int getId() {
		return id;
	}

    /**
     * Gets the name of the user.
     * @return The user's name.
     */
	public String getName() {
		return name;
	}

    /**
     * Gets the email of the user.
     * @return The user's email.
     */
	public String getEmail() {
		return email;
	}

    /**
     * Gets the password of the user.
     * @return The user's password.
     */
	public String getPassword() {
		return password;
	}

	/**
	 * Gets the roles by house ID.
	 * @return The map of roles by house ID.
	 */
	public Map<Integer, UserRole> getRolesByHouseId() {
		return new HashMap<>(rolesByHouseId);
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


    //------------------- Role Management Methods ------------------//
	
	/**
	 * Assigns a role to the user for a specific house.
	 * @param houseId The ID of the house.
	 * @param role The role to assign.
	 */
	public void assignRole(int houseId, UserRole role) {
		if (role == null) return;
		this.rolesByHouseId.put(houseId, role);
	}

	/**
	 * Removes a role from the user for a specific house.
	 * @param houseId The ID of the house.
	 */
	public void removeRole(int houseId) {
		this.rolesByHouseId.remove(houseId);
	}

	/**
	 * Gets the role of the user for a specific house.
	 * @param houseId The ID of the house.
	 * @return The role of the user for the specified house, or null if no role is assigned.
	 */
	public UserRole getRoleForHouse(int houseId) {
		return this.rolesByHouseId.get(houseId);
	}

	/**
	 * Checks if the user is an administrator for a specific house.
	 * @param houseId The ID of the house.
	 * @return true if the user is an administrator for the specified house, false otherwise.
	 */
	public boolean isAdminForHouse(int houseId) {
		return getRoleForHouse(houseId) == UserRole.ADMINISTRATOR;
	}

    //------------------- Override Methods ------------------//

    /**
     * Creates and returns a copy of this User instance. The cloned User will have the same ID and field values as the original.
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
		if (o == this) 
			return true;
		if (o == null || o.getClass() != this.getClass()) 
			return false;
		User user = (User) o;
		return this.id == user.id &&
               this.name.equals(user.name) &&
               this.email.equals(user.email) &&
               this.password.equals(user.password) &&
               this.rolesByHouseId.equals(user.rolesByHouseId);
    }

    /**
     * Computes the hash code for this User instance based on its fields.
     * @return The hash code of this User instance.
     */
	@Override
	public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.id;
        hash = 31 * hash + this.name.hashCode();
        hash = 31 * hash + this.email.hashCode();
        hash = 31 * hash + this.password.hashCode();
        hash = 31 * hash + this.rolesByHouseId.hashCode();
        return hash;
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