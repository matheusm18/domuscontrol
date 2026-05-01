package domuscontrol;

import domuscontrol.exceptions.DeviceNotFoundException;
import domuscontrol.exceptions.DivisionNotFoundException;
import domuscontrol.exceptions.HouseAlreadyExistsException;
import domuscontrol.exceptions.HouseNotFoundException;
import domuscontrol.exceptions.LastAdminException;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfAdjustableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfColorAdjustableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfOpenableDeviceException;
import domuscontrol.exceptions.DeviceIsNotInstanceOfSwitchableDeviceException;
import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.model.routines.Automation;
import domuscontrol.model.suggestions.AutomationSuggestion;
import domuscontrol.model.suggestions.DeviceInteraction;
import domuscontrol.model.suggestions.InteractionType;
import domuscontrol.model.device.Device;
import domuscontrol.model.device.types.AdjustableDevice;
import domuscontrol.model.device.types.ColorAdjustableDevice;
import domuscontrol.model.device.types.OpenableDevice;
import domuscontrol.model.device.types.SwitchableDevice;
import domuscontrol.model.houses.House;
import domuscontrol.model.houses.HouseManager;
import domuscontrol.model.houses.DivisionInfo;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Model facade of the DomusControl application.
 */
public class DomusControl implements Serializable {

    private final UserManager userManager;
    private final HouseManager houseManager;
    private static LocalDateTime currentTime = LocalDateTime.of(2026, 1, 1, 12, 0); // Start at January 1, 2026, 12:00 AM
    private static LocalDateTime lastTickTime; 

    public static LocalDateTime getCurrentDateTime(){
        return currentTime; 
    }

    public static LocalDateTime getLastTickDateTime(){
        return lastTickTime; 
    }

    public static void setCurrentTime(LocalDateTime newTime){
        lastTickTime = currentTime; 
        currentTime = newTime; 
    }

    public static LocalTime getCurrentTime(){
        return currentTime.toLocalTime(); 
    }

    public static LocalTime getLastTickTime(){
        return lastTickTime.toLocalTime();
    }

    public DomusControl() {
        this.userManager  = new UserManager();
        this.houseManager = new HouseManager();
    }

    public List<String> tick(int minutes) {
        lastTickTime = currentTime; 
        DomusControl.setCurrentTime(currentTime.plusMinutes(minutes)); 
        return this.houseManager.tick(minutes);
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
    public User validateLogin(String emailName, String password) throws UserNotFoundException, LoginInvalidPasswordException {
        User user;
        try {
            user = this.userManager.getUserByEmail(emailName);
         } // throws UserNotFoundException if email is not registered, which is the expected behavior for invalid login, so we don't
        catch (UserNotFoundException e) {
            user = this.userManager.getUserByName(emailName); // try again with lowercase email, to allow case-insensitive login
        }
        if (!user.getPassword().equals(password)) throw new LoginInvalidPasswordException(emailName);
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
     * Retrieves a clone of the user identified by the given ID.
     * 
     * @param id The user's ID.
     * @return A clone of the user.
     * @throws UserNotFoundException If no user is registered with that ID.
     */
    public User getUserById(int id) throws UserNotFoundException {
        return this.userManager.getUserById(id);
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

    public Map<Integer, UserRole> getUsersInHouse(int houseId) throws HouseNotFoundException {
        this.houseManager.getHouseById(houseId); // validate house exists
        return this.userManager.getAllUsers().stream()
            .filter(u -> u.getHouseIds().contains(houseId))
            .collect(Collectors.toMap(User::getId, u -> u.getRolesByHouseId().get(houseId)));
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
     * Retrieves all houses associated with a user by their email.
     * @param email The email of the user whose houses we want to retrieve.
     * @return A list of all houses associated with the user. 
     * @throws UserNotFoundException If the email does not correspond to a registered user.
     * @throws HouseNotFoundException If a house ID stored in the user's roles does not exist.
     */
    public List<House> getAllHousesByUser(String email) throws UserNotFoundException, HouseNotFoundException {
        User user = this.userManager.getUserByEmail(email);
        return user.getHouseIds().stream()
                .map(this.houseManager::getHouseById)
                .collect(Collectors.toList());
    }

    /**
     * Assigns a user to a house with a specific role, updating both the user's and the house's records.
     * @param houseId The ID of the house to which the user will be assigned.
     * @param userId The ID of the user to assign.
     * @param role The role to assign to the user in the house.
     * @throws UserNotFoundException If the user with the given ID does not exist.
     * @throws HouseNotFoundException If the house with the given ID does not exist.
     */
    public void assignUserToHouse(int houseId, Integer userId, UserRole role) throws UserNotFoundException, HouseNotFoundException {
        this.houseManager.getHouseById(houseId); // validate house exists
        User user = this.userManager.getUserById(userId);
        if (user.getHouseIds().contains(houseId)) {
            throw new UserAlreadyExistsException("");
        }
        user.assignRole(houseId, role);
        this.userManager.updateUser(user);
    }

    /**
     * Removes a user from a house, updating both the user's and the house's records.
     * @param houseId
     * @param userId
     * @throws UserNotFoundException
     * @throws HouseNotFoundException
     */
    public void deleteUserFromHouse(int houseId, int userId) throws UserNotFoundException, HouseNotFoundException {
        User user = this.userManager.getUserById(userId);
        UserRole role = user.getRoleForHouse(houseId);
        if (role == UserRole.ADMINISTRATOR) {
            long adminCount = getUsersInHouse(houseId).values().stream()
                .filter(r -> r == UserRole.ADMINISTRATOR).count();
            if (adminCount <= 1) {
                throw new LastAdminException("");
            }
        }
        user.removeRole(houseId);
        this.userManager.updateUser(user);
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

    /** Toggles a switchable device ON or OFF and logs the interaction for the given user. */
    public void toggleDevice(int houseId, int deviceId, int userId) throws HouseNotFoundException, DeviceNotFoundException, DeviceIsNotInstanceOfSwitchableDeviceException {
        Device clone = this.houseManager.getDevice(houseId, deviceId);
        if (!(clone instanceof SwitchableDevice))
            throw new DeviceIsNotInstanceOfSwitchableDeviceException("Device " + deviceId + " is not switchable.");
        boolean[] turnedOn = {false};
        this.houseManager.interactWithDevice(houseId, deviceId, d -> {
            SwitchableDevice sd = (SwitchableDevice) d;
            if (sd.isOn()) { sd.turnOff(); turnedOn[0] = false; }
            else           { sd.turnOn();  turnedOn[0] = true;  }
        });
        InteractionType type = turnedOn[0] ? InteractionType.TURN_ON : InteractionType.TURN_OFF;
        this.houseManager.logInteraction(houseId, new DeviceInteraction(deviceId, type, userId, getCurrentDateTime()));
    }

    /** Sets the level (0–100) of an adjustable device and logs the interaction. */
    public void setDeviceLevel(int houseId, int deviceId, int level, int userId) throws HouseNotFoundException, DeviceNotFoundException, DeviceIsNotInstanceOfAdjustableDeviceException {
        Device clone = this.houseManager.getDevice(houseId, deviceId);
        if (!(clone instanceof AdjustableDevice))
            throw new DeviceIsNotInstanceOfAdjustableDeviceException("Device " + deviceId + " is not adjustable.");
        this.houseManager.interactWithDevice(houseId, deviceId, d -> ((AdjustableDevice) d).setLevel(level));
        this.houseManager.logInteraction(houseId, new DeviceInteraction(deviceId, InteractionType.SET_LEVEL, (double) level, userId, getCurrentDateTime()));
    }

    /** Sets the opening percentage (0–100) of an openable device and logs the interaction. */
    public void setDeviceOpening(int houseId, int deviceId, int percentage, int userId) throws HouseNotFoundException, DeviceNotFoundException, DeviceIsNotInstanceOfOpenableDeviceException {
        Device clone = this.houseManager.getDevice(houseId, deviceId);
        if (!(clone instanceof OpenableDevice))
            throw new DeviceIsNotInstanceOfOpenableDeviceException("Device " + deviceId + " is not openable.");
        this.houseManager.interactWithDevice(houseId, deviceId, d -> ((OpenableDevice) d).setOpening(percentage));
        this.houseManager.logInteraction(houseId, new DeviceInteraction(deviceId, InteractionType.SET_OPENING, (double) percentage, userId, getCurrentDateTime()));
    }

    /** Sets the color temperature of a color-adjustable device and logs the interaction. */
    public void setDeviceColorTemperature(int houseId, int deviceId, int temperature, int userId) throws HouseNotFoundException, DeviceNotFoundException, DeviceIsNotInstanceOfColorAdjustableDeviceException {
        Device clone = this.houseManager.getDevice(houseId, deviceId);
        if (!(clone instanceof ColorAdjustableDevice))
            throw new DeviceIsNotInstanceOfColorAdjustableDeviceException("Device " + deviceId + " is not color adjustable.");
        this.houseManager.interactWithDevice(houseId, deviceId, d -> ((ColorAdjustableDevice) d).setColorTemperature(temperature));
        this.houseManager.logInteraction(houseId, new DeviceInteraction(deviceId, InteractionType.SET_COLOR_TEMPERATURE, (double) temperature, userId, getCurrentDateTime()));
    }

    /** Returns automation and schedule suggestions based on this user's interaction history in the house. */
    public List<AutomationSuggestion> getSuggestions(int houseId, int userId) throws HouseNotFoundException, ScheduleWithConditionDifferentFromTimeException {
        return this.houseManager.getSuggestions(houseId, userId);
    }

    /** Adds an accepted suggestion's automation to the house. */
    public void addAutomation(int houseId, Automation automation) throws HouseNotFoundException, NameAlreadyExistsException {
        this.houseManager.addAutomation(houseId, automation);
    }

    /**
     * Removes a device from the specified house.
     * @param houseId The ID of the house from which the device will be removed.
     * @param deviceId The ID of the device to be removed.
     * @throws HouseNotFoundException If no house with the given ID exists.
     * @throws DeviceNotFoundException If the device is not found in the specified house.
     */
    public void removeDevice(int houseId, int deviceId) throws HouseNotFoundException, DeviceNotFoundException {
        this.houseManager.removeDevice(houseId, deviceId);
    }

    /**
     * Returns the house with the highest energy consumption across the system.
     *
     * @return The most consuming house, or null if the system is empty.
     */
    public House getMostConsumingHouse() {
        return this.houseManager.getMostConsumingHouse();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(currentTime);
        out.writeObject(lastTickTime);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        currentTime = (LocalDateTime) in.readObject();
        lastTickTime = (LocalDateTime) in.readObject();
    }

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
            DomusControl model = (DomusControl) ois.readObject();

            List<House> houses = model.getAllHouses();
            int maxHouseId = houses.stream().mapToInt(House::getId).max().orElse(0);
            House.setNextId(maxHouseId);

            int maxDeviceId = houses.stream()
                .flatMap(h -> h.getDevices().values().stream())
                .mapToInt(Device::getId)
                .max()
                .orElse(0);
            Device.setNextId(maxDeviceId);

            return model;
        }
    }

    // ---- Queries ----

    /**
     * Helper method to get all devices for a given user by aggregating devices from all their houses.
     * @param email The email of the user whose devices we want to retrieve.
     * @return A list of all devices associated with the user's houses.
     */
    protected List<Device> getAllDevicesForUser(String email) {
        List<Device> devices = new ArrayList<>();
        for (House house : this.getHousesByUser(email)) {
            devices.addAll(house.getDevices().values());
        }
        return devices;
    }

    public List<Device> getTopDevicesByCriterion(String email, int n, ToIntFunction<Device> criterion) {
        return getAllDevicesForUser(email).stream()
            .sorted(Comparator.comparingInt(criterion).reversed())
            .limit(n)
            .map(Device::clone)
            .collect(Collectors.toList());
    }


    protected List<DivisionInfo> getAllDivisionsForUser(String email) {
        List<DivisionInfo> divisions = new ArrayList<>();
        for (House house : this.getHousesByUser(email)) {
            house.getDivisions().forEach((name, devices) -> 
                divisions.add(new DivisionInfo(house, name, 
                            devices.stream().
                                    map(d -> String.valueOf(d.getId())).
                                    collect(Collectors.toList())))
            );
        }
        return divisions;
    }

    public List<DivisionInfo> getTopDivisionsByCriterion(String email, int n, Function<DivisionInfo, Integer> criterion) {
        return getAllDivisionsForUser(email).stream()
            .sorted(Comparator.comparingInt(criterion::apply).reversed())
            .limit(n)
            .collect(Collectors.toList());
    }

    /**
     * Returns the top 3 most consuming houses based on total energy consumption.
     * @return A list of the top 3 most consuming houses.
     */
    public List<House> getTop3MostConsumingHouses(String email) {
        return this.getHousesByUser(email).stream()
                .sorted(Comparator.comparingDouble(House::calculateTotalConsumption).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }


}