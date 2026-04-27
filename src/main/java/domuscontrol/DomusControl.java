package domuscontrol;

import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.model.device.Device;
import domuscontrol.model.houses.House;
import domuscontrol.model.houses.HouseManager;
import domuscontrol.user.User;
import domuscontrol.user.UserManager;
import domuscontrol.user.UserRole;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model facade of the DomusControl application.
 */
public class DomusControl implements Serializable {

    private final UserManager userManager;
    private final HouseManager houseManager;

    public DomusControl() {
        this.userManager  = new UserManager();
        this.houseManager = new HouseManager();
    }

    /**
     * Registers a new user in the system.
     *
     * @param name The user's name.
     * @param email The user's email (unique identifier for login).
     * @param password The user's password.
     * @throws UserAlreadyExistsException If the email is already registered.
     */
    public void registerUser(String name, String email, String password) throws UserAlreadyExistsException {
        this.userManager.createUser(name, email, password);
    }

    /**
     * Validates the supplied credentials against the system.
     *
     * @param email The email of the user attempting to login.
     * @param password The password provided.
     * @return A clone of the matched user.
     * @throws UserNotFoundException If no user is registered with that email.
     * @throws LoginInvalidPasswordException If the email exists but the password is wrong.
     */
    public User validateLogin(String email, String password) throws UserNotFoundException, LoginInvalidPasswordException {
        User user = this.userManager.getUserByEmail(email);
        if (!user.getPassword().equals(password)) throw new LoginInvalidPasswordException(email);
        return user;
    }

    /**
     * Retrieves a clone of the user identified by the given email.
     *
     * @param email The user's email.
     * @return A clone of the user.
     * @throws UserNotFoundException If no user is registered with that email.
     */
    public User getUserByEmail(String email) throws UserNotFoundException {
        return this.userManager.getUserByEmail(email);
    }

    /**
     * Checks whether a user with the specified email is registered.
     *
     * @param email The email to check.
     * @return true if the email is registered, false otherwise.
     */
    public boolean existsUserWithEmail(String email) {
        return this.userManager.existsUserWithEmail(email);
    }

    /**
     * Updates the name of the user with the given email.
     *
     * @param email The email of the user to update.
     * @param newName The new name.
     * @throws UserNotFoundException If no user is registered with that email.
     */
    public void updateUserName(String email, String newName) throws UserNotFoundException {
        User user = this.userManager.getUserByEmail(email);
        user.setName(newName);
        this.userManager.updateUser(user);
    }

    /**
     * Updates the password of the user with the given email.
     *
     * @param email The email of the user to update.
     * @param newPassword The new password.
     * @throws UserNotFoundException If no user is registered with that email.
     */
    public void updateUserPassword(String email, String newPassword) throws UserNotFoundException {
        User user = this.userManager.getUserByEmail(email);
        user.setPassword(newPassword);
        this.userManager.updateUser(user);
    }

    /**
     * Updates the email of the user with the given current email.
     *
     * @param currentEmail The current email of the user.
     * @param newEmail The new email.
     * @throws UserNotFoundException If no user is registered with that email.
     * @throws UserAlreadyExistsException If the new email is already registered.
     */
    public void updateUserEmail(String currentEmail, String newEmail) throws UserNotFoundException, UserAlreadyExistsException {
        User user = this.userManager.getUserByEmail(currentEmail);
        user.setEmail(newEmail);
        this.userManager.updateUser(user);
    }

    /**
     * Retrieves all users in the system (clones).
     *
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        return this.userManager.getAllUsers();
    }

    /**
     * Creates a new house and assigns the requesting user as its administrator.
     *
     * @param ownerEmail The email of the user creating the house.
     * @param houseName The name of the new house.
     * @return A clone of the newly created house.
     * @throws UserNotFoundException If the owner email does not correspond to a registered user.
     * @throws HouseAlreadyExistsException If a house with the same ID already exists.
     */
    public House createHouse(String ownerEmail, String houseName) throws UserNotFoundException, HouseAlreadyExistsException {
        User owner = this.userManager.getUserByEmail(ownerEmail);
        House newHouse = this.houseManager.createHouse(houseName);

        owner.assignRole(newHouse.getId(), UserRole.ADMINISTRATOR);
        this.userManager.updateUser(owner);

        return newHouse;
    }

    /**
     * Retrieves all houses the given user has a role in.
     *
     * @param email The email of the user.
     * @return The list of houses (clones) associated with the user.
     * @throws UserNotFoundException If the email does not correspond to a registered user.
     * @throws HouseNotFoundException If a house ID stored in the user's roles does not exist.
     */
    public List<House> getHousesByUser(String email) throws UserNotFoundException, HouseNotFoundException {
        User user = this.userManager.getUserByEmail(email);
        return user.getHouseIds().stream()
                .map(this.houseManager::getHouseById)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific house by its ID.
     *
     * @param houseId The ID of the house.
     * @return A clone of the house.
     * @throws HouseNotFoundException If no house with the given ID exists.
     */
    public House getHouseById(int houseId) throws HouseNotFoundException {
        return this.houseManager.getHouseById(houseId);
    }

    /**
     * Returns the role the given user has in the given house.
     *
     * @param email The user's email.
     * @param houseId The house's ID.
     * @return The user's role in that house.
     * @throws UserNotFoundException If the email does not correspond to a registered user.
     * @throws HouseNotFoundException If the user does not have an entry for the given house ID.
     */
    public UserRole getUserRoleInHouse(String email, int houseId) throws UserNotFoundException, HouseNotFoundException {
        User user = this.userManager.getUserByEmail(email);
        return user.getRoleForHouse(houseId);
    }

    /**
     * Retrieves all houses in the system (clones).
     *
     * @return A list of all houses.
     */
    public List<House> getAllHouses() {
        return this.houseManager.getAllHouses();
    }

    /**
     * Adds a new division to the specified house.
     *
     * @param houseId The ID of the house to which the division will be added.
     * @param divisionName The name of the new division.
     * @throws HouseNotFoundException If no house with the given ID exists.
     */
    public void addDivision(int houseId, String divisionName) throws HouseNotFoundException {
        this.houseManager.addDivision(houseId, divisionName);
    }

    /**
     * Removes a division from the specified house.
     *
     * @param houseId The ID of the house from which the division will be removed.
     * @param divisionName The name of the division to remove.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DivisionNotFoundException If the division does not exist in the specified house.
     */
    public void removeDivision(int houseId, String divisionName) throws HouseNotFoundException, DivisionNotFoundException {
        this.houseManager.removeDivision(houseId, divisionName);
    }

    /**
     * Adds a device to a division in the specified house.
     *
     * @param houseId The ID of the house.
     * @param device The device to add.
     * @param division The name of the division.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DivisionNotFoundException If the division does not exist in the specified house.
     */
    public void addDeviceToDivision(int houseId, Device device, String division) throws HouseNotFoundException, DivisionNotFoundException {
        this.houseManager.addDeviceToDivision(houseId, device, division);
    }

    /**
     * Retrieves a device by its ID from the specified house.
     *
     * @param houseId The ID of the house.
     * @param deviceId The ID of the device.
     * @return The device.
     * @throws HouseNotFoundException If no house with the given ID exists, or the device is not found.
     */
    public Device getDevice(int houseId, int deviceId) throws HouseNotFoundException {
        return this.houseManager.getDevice(houseId, deviceId);
    }

    /**
     * Updates a device in the specified house.
     *
     * @param houseId The ID of the house.
     * @param device The device with updated information.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DeviceNotFoundException If the device is not found in the specified house.
     */
    public void updateDevice(int houseId, Device device) throws HouseNotFoundException, DeviceNotFoundException {
        this.houseManager.updateDevice(houseId, device);
    }

    /**
     * Advances the simulation clock, updating all houses and devices.
     *
     * @param minutes The number of minutes to advance.
     */
    public void tick(int minutes) {
        this.houseManager.tick(minutes);
    }

    /**
     * Returns the house with the highest energy consumption across the system.
     *
     * @return The most consuming house, or null if the system is empty.
     */
    public House getMostConsumingHouse() {
        return this.houseManager.getMostConsumingHouse();
    }


    // TODO: Falta toda a parte de automacoes, cenários e escalonamentos (metodos add, get, toggle, execute e undo)


    /**
     * Saves the full state of the model to a binary file.
     *
     * @param fileName The file to write to.
     * @throws IOException If writing fails.
     */
    public void saveState(String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
            oos.flush();
        }
    }

    /**
     * Loads a previously saved DomusControl state from a binary file.
     *
     * @param fileName The file to read.
     * @return The reconstructed DomusControl model.
     * @throws IOException If reading fails.
     * @throws ClassNotFoundException If deserialization fails.
     */
    public static DomusControl loadState(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (DomusControl) ois.readObject();
        }
    }
}