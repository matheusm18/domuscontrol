package domuscontrol.routines;

import domuscontrol.simulation.SimulationState;

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
 * Manages the routines defined for a house.
 * Scenarios are stored per user, while automations and schedules are stored by name.
 * Provides the lifecycle management for all routine objects in the system.
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class RoutineManager implements Serializable {

    /** The scenarios stored by user. */
    private Map<Integer, Map<String, Scenario>> scenariosByUser;
    /** The automations and schedules stored by name. */
    private Map<String, Automation> automations;

    /**
     * Creates an empty routine manager.
     */
    public RoutineManager() {
        this.scenariosByUser = new HashMap<>();
        this.automations = new HashMap<>();
    }

    /**
     * Creates a routine manager with the given scenarios and automations.
     * The provided maps and routines are copied before being stored.
     *
     * @param scenariosByUser the user-specific scenarios
     * @param automations the automations and schedules
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
     * Creates a copy of another routine manager.
     *
     * @param other the routine manager to copy
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
     * Gets a copy of the scenarios stored by user.
     *
     * @return a copied map of user IDs to scenario maps
     */
    public Map<Integer, Map<String, Scenario>> getScenariosByUser() {
        Map<Integer, Map<String, Scenario>> copy = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Scenario>> userEntry : this.scenariosByUser.entrySet()) {
            Map<String, Scenario> userScenarios = new HashMap<>();
            for (Map.Entry<String, Scenario> scenarioEntry : userEntry.getValue().entrySet()) {
                userScenarios.put(scenarioEntry.getKey(), scenarioEntry.getValue().clone());
            }
            copy.put(userEntry.getKey(), userScenarios);
        }
        return copy;
    }

    /**
     * Gets a copy of the automations and schedules stored by name.
     *
     * @return a copied map of routine names to automations
     */
    public Map<String, Automation> getAutomationsByName() {
        Map<String, Automation> copy = new HashMap<>();
        for (Map.Entry<String, Automation> entry : this.automations.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().clone());
        }
        return copy;
    }

    /**
     * Adds a new scenario for a specific user.
     *
     * @param userId the ID of the user owning the scenario
     * @param scenario the scenario to add
     * @throws NameAlreadyExistsException if the user already has a scenario with the same name.
     */
    public void addScenario(int userId, Scenario scenario) throws NameAlreadyExistsException {
        Map<String, Scenario> userScenarios = this.scenariosByUser
            .computeIfAbsent(userId, k -> new HashMap<>());
        String key = scenario.getName().toLowerCase();
        if (userScenarios.containsKey(key)) {
            throw new NameAlreadyExistsException("" + scenario.getName());
        }
        userScenarios.put(key, scenario.clone());
    }

    /**
     * Adds a global automation or schedule.
     *
     * @param automation the automation or schedule to add
     * @throws NameAlreadyExistsException if a routine with the same name already exists globally.
     */
    public void addAutomation(Automation automation) throws NameAlreadyExistsException {
        String key = automation.getName().toLowerCase();
        if (this.automations.containsKey(key)) {
            throw new NameAlreadyExistsException("" + automation.getName());
        }
        this.automations.put(key, automation.clone());
    }

    /**
     * Retrieves all scenarios associated with a specific user.
     *
     * @param userId the ID of the user
     * @return a list of copied scenarios
     * @throws UserDoesntHaveScenarios if the user has no scenarios registered.
     */
    public List<Scenario> getScenariosForUser(int userId) throws UserDoesntHaveScenarios {
        Map<String, Scenario> map = this.scenariosByUser.get(userId);
        if (map == null) {
            throw new UserDoesntHaveScenarios("" + userId);
        }
        return map.values().stream().map(Scenario::clone).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific scenario by its name for a given user.
     *
     * @param userId the ID of the user
     * @param scenarioName the name of the scenario
     * @return a copied scenario
     * @throws UserDoesntHaveScenarios if the user has no scenarios.
     * @throws ScenarioDoesntExistException if no scenario with that name is found for the user.
     */
    public Scenario getScenarioByName(int userId, String scenarioName) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> map = this.scenariosByUser.get(userId);
        if (map == null) {
            throw new UserDoesntHaveScenarios("" + userId);
        }
        Scenario scenario = map.get(scenarioName.toLowerCase());
        if (scenario == null) {
            throw new ScenarioDoesntExistException("" + scenarioName);
        }
        return scenario.clone();
    }

    /**
     * Retrieves all registered automations and schedules.
     *
     * @return a list of copied automations and schedules
     */
    public List<Automation> getAutomations() {
        return this.automations.values().stream().map(Automation::clone).collect(Collectors.toList());
    }

    /**
     * Filters and retrieves only the routines defined as schedules.
     *
     * @return a list of copied schedules
     */
    public List<Automation> getOnlySchedules() {
        return this.automations.values().stream()
                .filter(a -> a.getType() == AutomationType.SCHEDULE)
                .map(Automation::clone)
                .collect(Collectors.toList());
    }

    /**
     * Filters and retrieves only the routines defined as standard automations.
     *
     * @return a list of copied automations
     */
    public List<Automation> getOnlyAutomations() {
        return this.automations.values().stream()
                .filter(a -> a.getType() == AutomationType.AUTOMATION)
                .map(Automation::clone)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a global automation or schedule by its name.
     *
     * @param name the name of the automation or schedule
     * @return a copied automation or schedule
     * @throws AutomationDoesntExistException if no routine with that name exists.
     */
    public Automation getAutomationByName(String name) throws AutomationDoesntExistException {
        Automation automation = this.automations.get(name.toLowerCase());
        if (automation == null) {
            throw new AutomationDoesntExistException("" + name);
        }
        return automation.clone();
    }

    /**
     * Permanently removes an automation or schedule from the system.
     *
     * @param name the name of the routine to remove
     * @throws AutomationDoesntExistException if the routine is not found.
     */
    public void removeAutomation(String name) throws AutomationDoesntExistException {
        if (this.automations.remove(name.toLowerCase()) == null) {
            throw new AutomationDoesntExistException("" + name);
        }
    }

    /**
     * Permanently removes a specific user's scenario.
     *
     * @param userId the ID of the user
     * @param name the name of the scenario
     * @throws UserDoesntHaveScenarios      if the user has no scenarios.
     * @throws ScenarioDoesntExistException if the scenario is not found.
     */
    public void removeScenario(int userId, String name) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> userScenarios = this.scenariosByUser.get(userId);
        if (userScenarios == null) {
            throw new UserDoesntHaveScenarios("" + userId);
        }
        if (userScenarios.remove(name.toLowerCase()) == null) {
            throw new ScenarioDoesntExistException("" + name);
        }
    }

    /**
     * Batch updates the scenario database for all users.
     * The provided scenarios are copied before being stored.
     *
     * @param scenariosByUser the new map of user scenarios
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
     * The provided automations are copied before being stored.
     *
     * @param automations the new map of automations and schedules
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
     *
     * @param userId the ID of the user
     * @param scenarioName the name of the scenario to execute
     * @param house the house where the scenario actions should be applied
     * @throws UserDoesntHaveScenarios      if the user has no scenarios.
     * @throws ScenarioDoesntExistException if the scenario is not found.
     */
    public void executeScenarioByName(int userId, String scenarioName, House house) throws UserDoesntHaveScenarios, ScenarioDoesntExistException {
        Map<String, Scenario> userScenarios = this.scenariosByUser.get(userId);
        if (userScenarios == null) {
            throw new UserDoesntHaveScenarios("" + userId);
        }
        Scenario scenario = userScenarios.get(scenarioName.toLowerCase());
        if (scenario == null) {
            throw new ScenarioDoesntExistException("" + scenarioName);
        }
        scenario.executeScenario(house);
    }

    /**
     * Checks all automations and schedules against the current house and simulation state,
     * triggering any whose conditions are met.
     *
     * @param house the house context to evaluate
     * @param state the current simulation state
     * @return a list of automation names that were triggered this tick
     */
    public List<String> tick(House house, SimulationState state) {
        List<String> activated = new ArrayList<>();

        for (Automation auto : this.automations.values()) {
            if (auto.checkAndTrigger(house, state)) {
                activated.add(auto.getName());
            }
        }

        return activated;
    }

    /**
     * Cleans up orphaned actions and conditions when a device is removed from the house.
     * If a routine becomes empty after removal, it is deleted entirely.
     *
     * @param deviceId the ID of the removed device
     */
    public void removeDevice(int deviceId) {
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
     *
     * @param o the object to compare with
     * @return true if scenarios and automations match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        RoutineManager routineManager = (RoutineManager) o;

        return this.scenariosByUser.equals(routineManager.getScenariosByUser()) &&
               this.automations.equals(routineManager.getAutomationsByName());
    }

    /**
     * Generates a hash code for the manager.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.scenariosByUser, this.automations);
    }

    /**
     * Creates a copy of this routine manager.
     *
     * @return a copied RoutineManager instance
     */
    @Override
    public RoutineManager clone() {
        return new RoutineManager(this);
    }

    /**
     * Returns a summarized string of the manager's current state.
     *
     * @return a formatted string with scenario and automation counts
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RoutineManager { ")
          .append("Users With Scenarios: ").append(this.scenariosByUser.size())
          .append(", Routines: ").append(this.automations.size())
          .append(" }");
        return sb.toString();
    }
}
