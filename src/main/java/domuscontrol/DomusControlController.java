package domuscontrol;

import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.exceptions.UserNotLoggedInException;
import domuscontrol.model.device.Device;
import domuscontrol.model.houses.House;
import domuscontrol.user.UserRole;

import java.io.IOException;
import java.util.List;

/** The class DomusControlController represents the bridge between the views and the model. */
public class DomusControlController {

    /** The DomusControl model. Replaced when a saved state is loaded. */
    private DomusControl model;

    /** The email of the currently logged-in user, or null if nobody is. */
    private String currentUserEmail;

    /** Creates a controller wrapping an empty model. */
    public DomusControlController() {
        this.model = new DomusControl();
        this.currentUserEmail = null;
    }

    /**
     * Attempts to log in with the supplied credentials.
     * On success, the session is established.
     *
     * @param email The email.
     * @param password The password.
     * @throws UserNotFoundException If the email is not registered.
     * @throws LoginInvalidPasswordException If the password is wrong.
     */
    public void login(String email, String password) throws UserNotFoundException, LoginInvalidPasswordException {
        this.model.validateLogin(email, password);
        this.currentUserEmail = email;
    }

    /** Logs out the current user. */
    public void logout() {
        this.currentUserEmail = null;
    }

    /**
     * Registers a new user.
     *
     * @param name The user's name.
     * @param email The user's email.
     * @param password The user's password.
     * @throws UserAlreadyExistsException If the email is already registered.
     */
    public void register(String name, String email, String password) throws UserAlreadyExistsException {
        this.model.registerUser(name, email, password);
    }

    /**
     * Checks if there is a user currently logged in.
     *
     * @return true if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return this.currentUserEmail != null;
    }

    /**
     * Returns the email of the currently logged-in user.
     *
     * @return the email of the currently logged-in user.
     * @throws UserNotLoggedInException If no user is logged in.
     */
    public String getSessionEmail() throws UserNotLoggedInException {
        requireLogin();
        return this.currentUserEmail;
    }

    /**
     * Returns the name of the currently logged-in user.
     *
     * @return the name of the currently logged-in user.
     * @throws UserNotLoggedInException If no user is logged in.
     */
    public String getSessionName() throws UserNotLoggedInException {
        requireLogin();
        return this.model.getUserByEmail(this.currentUserEmail).getName();
    }

    /**
     * Updates the logged-in user's name.
     *
     * @param newName The new name.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     */
    public void updateName(String newName) throws UserNotLoggedInException, UserNotFoundException {
        requireLogin();
        this.model.updateUserName(this.currentUserEmail, newName);
    }

    /**
     * Updates the logged-in user's password.
     *
     * @param newPassword The new password.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     */
    public void updatePassword(String newPassword) throws UserNotLoggedInException, UserNotFoundException {
        requireLogin();
        this.model.updateUserPassword(this.currentUserEmail, newPassword);
    }

    /**
     * Updates the logged-in user's email. The session is updated so the
     * user stays logged in under the new email.
     *
     * @param newEmail The new email.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     * @throws UserAlreadyExistsException If the new email is already registered.
     */
    public void updateEmail(String newEmail) throws UserNotLoggedInException, UserNotFoundException, UserAlreadyExistsException {
        requireLogin();
        this.model.updateUserEmail(this.currentUserEmail, newEmail);
        this.currentUserEmail = newEmail;
    }

    /**
     * Creates a new house owned by the logged-in user (as administrator).
     *
     * @param houseName The name of the new house.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     * @throws HouseAlreadyExistsException If a house with the same ID already exists.
     */
    public void createHouse(String houseName) throws UserNotLoggedInException, UserNotFoundException, HouseAlreadyExistsException {
        requireLogin();
        this.model.createHouse(this.currentUserEmail, houseName);
    }

    /**
     * Lists all houses the logged-in user has a role in.
     *
     * @return The list of houses.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     * @throws HouseNotFoundException If a house ID stored in the user's roles does not exist.
     */
    public List<House> getMyHouses() throws UserNotLoggedInException, UserNotFoundException, HouseNotFoundException {
        requireLogin();
        return this.model.getHousesByUser(this.currentUserEmail);
    }

    /**
     * Retrieves a specific house. The session user must have a role in it.
     *
     * @param houseId The house's ID.
     * @return The house.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws UserNotFoundException If the session user is not found.
     * @throws HouseNotFoundException If the house is not found for the given ID, or if the session user has no role in it.
     */
    public House getHouse(int houseId) throws UserNotLoggedInException, UserNotFoundException, HouseNotFoundException {
        requireLogin();
        this.model.getUserRoleInHouse(this.currentUserEmail, houseId);
        return this.model.getHouseById(houseId);
    }

    /**
     * Checks whether the session user is administrator of the given house.
     *
     * @param houseId The house's ID.
     * @return true if the session user is admin of that house.
     * @throws UserNotLoggedInException If no user is logged in.
     */
    public boolean isAdminOfHouse(int houseId) throws UserNotLoggedInException {
        requireLogin();
        try {
            return this.model.getUserRoleInHouse(this.currentUserEmail, houseId) == UserRole.ADMINISTRATOR;
        } catch (HouseNotFoundException | UserNotFoundException e) {
            return false;
        }
    }

    /**
     * Adds a new division to the given house. The session user must be administrator of that house.
     *
     * @param houseId The house's ID.
     * @param divisionName The division's name.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws SecurityException If the session user is not administrator of that house.
     */
    public void addDivision(int houseId, String divisionName) throws UserNotLoggedInException, HouseNotFoundException, SecurityException {
        requireLogin();
        if (!isAdminOfHouse(houseId)) throw new SecurityException("Only house administrators can add divisions.");
        this.model.addDivision(houseId, divisionName);
    }

    /**
     * Removes a division from the given house. The session user must be administrator of that house.
     *
     * @param houseId The house's ID.
     * @param divisionName The division's name.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DivisionNotFoundException If the division does not exist in the house.
     * @throws SecurityException If the session user is not administrator of that house.
     */
    public void removeDivision(int houseId, String divisionName) throws UserNotLoggedInException, HouseNotFoundException, DivisionNotFoundException, SecurityException {
        requireLogin();
        if (!isAdminOfHouse(houseId)) throw new SecurityException("Only house administrators can remove divisions.");
        this.model.removeDivision(houseId, divisionName);
    }

    /**
     * Adds a device to a division in the given house. The session user must be administrator of that house.
     *
     * @param houseId The house's ID.
     * @param device The device to add.
     * @param division The division to add the device to.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DivisionNotFoundException If the division does not exist in the house.
     * @throws SecurityException If the session user is not administrator of that house.
     */
    public void addDeviceToDivision(int houseId, Device device, String division) throws UserNotLoggedInException, HouseNotFoundException, DivisionNotFoundException, SecurityException {
        requireLogin();
        if (!isAdminOfHouse(houseId)) throw new SecurityException("Only house administrators can add devices.");
        this.model.addDeviceToDivision(houseId, device, division);
    }

    /**
     * Retrieves a device from the given house.
     *
     * @param houseId The house's ID.
     * @param deviceId The device's ID.
     * @return The device.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws HouseNotFoundException If no house with the given ID exists, or the device is not found.
     */
    public Device getDevice(int houseId, int deviceId) throws UserNotLoggedInException, HouseNotFoundException {
        requireLogin();
        return this.model.getDevice(houseId, deviceId);
    }

    /**
     * Updates a device in the given house.
     *
     * @param houseId The house's ID.
     * @param device The device with updated information. The device ID must match the device to update.
     * @throws UserNotLoggedInException If no user is logged in.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DeviceNotFoundException If the device is not found in the house.
     */
    public void updateDevice(int houseId, Device device) throws UserNotLoggedInException, HouseNotFoundException, DeviceNotFoundException {
        requireLogin();
        this.model.updateDevice(houseId, device);
    }

    /**
     * Advances the simulation clock.
     *
     * @param minutes The number of minutes to advance.
     */
    public void advanceTime(int minutes) {
        this.model.tick(minutes);
    }

    /**
     * Returns the house in the system with the highest consumption.
     *
     * @return The most consuming house, or null.
     */
    public House getMostConsumingHouse() {
        return this.model.getMostConsumingHouse();
    }

    /**
     * Saves the current model state to a file.
     *
     * @param fileName The file name.
     * @throws IOException If writing fails.
     */
    public void saveState(String fileName) throws IOException {
        this.model.saveState(fileName);
    }

    /**
     * Loads a previously saved model state from a file, replacing the current model.
     *
     * @param fileName The file name.
     * @throws IOException If reading fails.
     * @throws ClassNotFoundException If deserialization fails.
     */
    public void loadState(String fileName) throws IOException, ClassNotFoundException {
        this.model = DomusControl.loadState(fileName);
        this.currentUserEmail = null;
    }

    /**
     * Checks if there is a user currently logged in. If not, throws an exception.
     *
     * @throws UserNotLoggedInException If no user is currently logged in.
     */
    private void requireLogin() throws UserNotLoggedInException {
        if (this.currentUserEmail == null) {
            throw new UserNotLoggedInException("No user is currently logged in.");
        }
    }
}
