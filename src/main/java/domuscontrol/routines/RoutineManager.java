package domuscontrol.routines;

import domuscontrol.simulation.Simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import domuscontrol.exceptions.NameAlreadyExistsException;
import domuscontrol.exceptions.ScenarioDoesntExistException;
import domuscontrol.exceptions.UserDoesntHaveScenarios;
import domuscontrol.houses.House;
import domuscontrol.exceptions.AutomationDoesntExistException;

/**
 * Core manager responsible for handling all routine types (Scenarios, Automations, and Schedules).
 * It acts as the central repository for user-defined logic, managing their lifecycle and execution.
 */
public class RoutineManager implements Serializable {
    
    /**
     * Map storing scenarios organized by User ID, then by the scenario's name.
     */
    private Map<Integer, Map<String, Scenario>> scenariosByUser;
    
    /**
     * Map storing all global automations and schedules, indexed by their unique names.
     */
    private Map<String, Automation> automations;

    /**
     * Default constructor initializing empty storage for scenarios and automations.
     */
    public RoutineManager() {
        this.scenariosByUser = new HashMap<>();
        this.automations = new HashMap<>();
    }

    /**
     * Parameterized constructor providing deep copies of existing routine maps.
     * * @param scenariosByUser Initial map of user-specific scenarios.
     * @param automations     Initial map of global automations.
     */
    public RoutineManager(Map<Integer, Map<String, Scenario>> scenariosByUser,
                         Map<String, Automation> automations) {
        this.scenariosByUser = new HashMap<>();
        if (scenariosByUser != null) {
            for (Map.Entry<Integer, Map<String, Scenario>> userEntry : scenariosByUser.entrySet()) {
                Map<String, Scenario> userScenarios = new HashMap<>();
                for (Map.Entry<String, Scenario> e : userEntry.getValue().entrySet()) {
                    userScenarios.put(e.getKey().toLowerCase(), e.getValue().clone());
                }
                this.scenariosByUser.put(userEntry.getKey(), userScenarios);
            }
        }

        this.automations = new HashMap<>();
        if (automations != null) {
            for (Map.Entry<String, Automation> entry : automations.entrySet()) {
                this.automations.put(entry.getKey().toLowerCase(), entry.getValue().clone());
            }
        }
    }

    /**
     * Copy constructor for deep cloning the RoutineManager.
     * * @param other The existing RoutineManager instance to copy.
     */
    public RoutineManager(RoutineManager other) {
        this.scenariosByUser = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Scenario>> userEntry : other.scenariosByUser.entrySet()) {
            Map<String, Scenario> userScenarios = new HashMap<>();
            for (Map.Entry<String, Scenario> e : userEntry.getValue().entrySet()) {
                userScenarios.put(e.getKey(), e.getValue().clone());
            }
            this.scenariosByUser.put(userEntry.getKey(), userScenarios);
        }

        this.automations = new HashMap<>();
        for (Map.Entry<String, Automation> entry : other.automations.entrySet()) {
            this.automations.put(entry.getKey(), entry.getValue().clone());
        }
    }

    /**
     * Adds a new scenario for a specific user.
     * * @param userId   The ID of the user owning the scenario.
     * @param scenario The scenario object to be added.
     * @throws NameAlreadyExistsException if the user already has a scenario with the same name.
     */
    public void addScenario(int userId, Scenario scenario) throws NameAlreadyExistsException {
        Map<String, Scenario> userScenarios = this.scenariosByUser
            .computeIfAbsent(userId, k -> new HashMap<>());
        String key = scenario.getName().toLowerCase();
        if (userScenarios.containsKey(key))
            throw new NameAlreadyExistsException("Scenario with name '" + scenario.getName() + "' already exists.");
        userScenarios.put(key, scenario.clone());
    }

    /**
     * Adds a global automation or schedule.
     * * @param automation The automation object to be added.
     * @throws NameAlreadyExistsException if a routine with the same name already exists globally.
     */
    public void addAutomation(Automation automation) throws NameAlreadyExistsException {
        if (automation == null || automation.getName() == null)
            throw new IllegalArgumentException("Automation and its name must not be null.");
        String key = automation.getName().toLowerCase();
        if (this.automations.containsKey(key))
            throw new NameAlreadyExistsException("Routine with name '" + automation.getName() + "' already exists.");
        this.automations.put(key, automation.clone());
    }

    /**
     * Retrieves all scenarios associated with a specific user.
     * * @param userId The ID of the user.
     * @return A list of cloned scenario instances.
     * @throws UserDoesntHaveScenarios if the user has no scenarios registered.
     */
    public List<Scenario> getScenariosForUser(int userId) throws UserDoesntHaveScenarios {
        Map<String, Scenario> map = this.scenariosByUser.get(userId);
        if (map == null)
            throw new UserDoesntHaveScenarios("User ID " + userId + " has no scenarios.");
        return map.values().stream().map(Scenario::clone).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific scenario by its name for a given user.
     * * @param userId       The ID of the user.
     * @param scenarioName The name of the scenario.
     * @return A cloned instance of the requested scenario.
     * @throws UserDoesntHaveScenarios     if the user has no scenarios.
     * @throws ScenarioDoesntExistException if no scenario with that name is found for the user.
     */
    public Scenario getScenarioByName(int userId, String scenarioName) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> map = this.scenariosByUser.get(userId);
        if (map == null)
            throw new UserDoesntHaveScenarios("User ID " + userId + " has no scenarios.");
        Scenario scenario = map.get(scenarioName.toLowerCase());
        if (scenario == null)
            throw new ScenarioDoesntExistException("Scenario with name '" + scenarioName + "' does not exist for user ID " + userId);
        return scenario.clone();
    }

    /**
     * Retrieves all registered automations and schedules.
     * * @return A list of cloned automation instances.
     */
    public List<Automation> getAutomations() {
        return this.automations.values().stream().map(Automation::clone).collect(Collectors.toList());
    }
    
    /**
     * Filters and retrieves only the routines defined as schedules.
     * * @return A list of cloned schedule-type automations.
     */
    public List<Automation> getOnlySchedules() {
        return this.automations.values().stream()
                .filter(a -> a.getType() == AutomationType.SCHEDULE)
                .map(Automation::clone)
                .collect(Collectors.toList());
    }

    /**
     * Filters and retrieves only the routines defined as standard automations.
     * * @return A list of cloned standard automation instances.
     */
    public List<Automation> getOnlyAutomations() {
        return this.automations.values().stream()
                .filter(a -> a.getType() == AutomationType.AUTOMATION)
                .map(Automation::clone)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a global automation or schedule by its name.
     * * @param name The name of the automation.
     * @return A cloned instance of the automation.
     * @throws AutomationDoesntExistException if no routine with that name exists.
     */
    public Automation getAutomationByName(String name) throws AutomationDoesntExistException {
        Automation automation = this.automations.get(name.toLowerCase());
        if (automation == null)
            throw new AutomationDoesntExistException("Routine '" + name + "' does not exist.");
        return automation.clone();
    }

    /**
     * Permanently removes an automation or schedule from the system.
     * * @param name The name of the routine to remove.
     * @throws AutomationDoesntExistException if the routine is not found.
     */
    public void removeAutomation(String name) throws AutomationDoesntExistException {
        if (this.automations.remove(name.toLowerCase()) == null)
            throw new AutomationDoesntExistException("Routine '" + name + "' does not exist.");
    }

    /**
     * Permanently removes a specific user's scenario.
     * * @param userId The ID of the user.
     * @param name   The name of the scenario.
     * @throws UserDoesntHaveScenarios     if the user has no scenarios.
     * @throws ScenarioDoesntExistException if the scenario is not found.
     */
    public void removeScenario(int userId, String name) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> userScenarios = this.scenariosByUser.get(userId);
        if (userScenarios == null)
            throw new UserDoesntHaveScenarios("User ID " + userId + " has no scenarios.");
        if (userScenarios.remove(name.toLowerCase()) == null)
            throw new ScenarioDoesntExistException("Scenario '" + name + "' does not exist for user ID " + userId);
    }

    /**
     * Batch updates the scenario database for all users.
     * * @param scenariosByUser The new map of user scenarios.
     */
    public void setScenariosByUser(Map<Integer, Map<String, Scenario>> scenariosByUser) {
        this.scenariosByUser = new HashMap<>();
        if (scenariosByUser != null) {
            for (Map.Entry<Integer, Map<String, Scenario>> userEntry : scenariosByUser.entrySet()) {
                Map<String, Scenario> userScenarios = new HashMap<>();
                for (Map.Entry<String, Scenario> e : userEntry.getValue().entrySet()) {
                    userScenarios.put(e.getKey().toLowerCase(), e.getValue().clone());
                }
                this.scenariosByUser.put(userEntry.getKey(), userScenarios);
            }
        }
    }

    /**
     * Batch updates the global automation database.
     * * @param automations The new map of automations.
     */
    public void setAutomations(Map<String, Automation> automations) {
        this.automations = new HashMap<>();
        if (automations != null) {
            for (Map.Entry<String, Automation> entry : automations.entrySet()) {
                this.automations.put(entry.getKey().toLowerCase(), entry.getValue().clone());
            }
        }
    }

    /**
     * Manually triggers the execution of a specific scenario.
     * * @param userId       The ID of the user.
     * @param scenarioName The name of the scenario to execute.
     * @param house        The house context where the actions will take place.
     * @throws UserDoesntHaveScenarios     if the user has no scenarios.
     * @throws ScenarioDoesntExistException if the scenario is not found.
     */
    public void executeScenarioByName(int userId, String scenarioName, House house) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> userScenarios = this.scenariosByUser.get(userId);
        if (userScenarios == null)
            throw new UserDoesntHaveScenarios("User ID " + userId + " has no scenarios.");
        Scenario scenario = userScenarios.get(scenarioName.toLowerCase());
        if (scenario == null)
            throw new ScenarioDoesntExistException("Scenario with name '" + scenarioName + "' does not exist.");
        scenario.executeScenario(house);
    }

    /**
     * Advances the simulation state by checking all automations and schedules
     * against the current house state and time.
     * * @param house The house context to evaluate.
     */
    public List<String> tick(House house, Simulation simulation) {
        List<String> activated = new ArrayList<>();

        for (Automation auto : this.automations.values()) {
            if (auto.checkAndTrigger(house, simulation)) {
                activated.add(auto.getName());
            }
        }

        return activated;
    }

    /**
     * Cleans up orphaned actions and conditions when a device is removed from the house.
     * If a routine becomes empty after removal, it is deleted entirely.
     * * @param deviceId The ID of the removed device.
     */
    public void removeDevice(int deviceId){
        this.automations.values().removeIf(automation -> {
            automation.removeDeviceById(deviceId);
            return automation.getActions().isEmpty() && automation.getConditions().isEmpty();
        });
        this.scenariosByUser.values().forEach(userScenarios -> 
            userScenarios.values().removeIf(scenario -> { 
                scenario.removeDeviceById(deviceId); 
                return scenario.getActions().isEmpty();
            })
        );
    }

    /**
     * Compares this manager with another object for equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        RoutineManager rf = (RoutineManager) o;

        return this.scenariosByUser.equals(rf.scenariosByUser) &&
               this.automations.equals(rf.automations);
    }

    /**
     * Generates a hash code for the manager.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.scenariosByUser, this.automations);
    }

    /**
     * Creates a deep copy of this RoutineManager.
     */
    @Override
    public RoutineManager clone() {
        return new RoutineManager(this);
    }

    /**
     * Returns a summarized string of the manager's current state.
     */
    @Override
    public String toString() {
        return "RoutineFacade Data:\n" +
               " - Scenarios By User: " + this.scenariosByUser.size() + "\n" +
               " - Total Routines (Automations & Schedules): " + this.automations.size() + "\n";
    }
}
