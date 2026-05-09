package domuscontrol.suggestions;

import domuscontrol.devices.Device;
import domuscontrol.devices.types.AdjustableDevice;
import domuscontrol.devices.types.ColorAdjustableDevice;
import domuscontrol.devices.types.OpenableDevice;
import domuscontrol.devices.types.SwitchableDevice;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.routines.Action;
import domuscontrol.routines.Automation;
import domuscontrol.routines.AutomationType;
import domuscontrol.routines.Condition;
import domuscontrol.routines.actions.SetColorTemperatureAction;
import domuscontrol.routines.actions.SetLevelAction;
import domuscontrol.routines.actions.SetOpeningAction;
import domuscontrol.routines.actions.TurnOffAction;
import domuscontrol.routines.actions.TurnOnAction;
import domuscontrol.devices.sensors.LuminositySensor;
import domuscontrol.devices.sensors.RainfallSensor;
import domuscontrol.devices.sensors.TemperatureSensor;
import domuscontrol.routines.conditions.ColorTemperatureCondition;
import domuscontrol.routines.conditions.DeviceLevelCondition;
import domuscontrol.routines.conditions.DeviceOpenCondition;
import domuscontrol.routines.conditions.DeviceStateCondition;
import domuscontrol.routines.conditions.LuminositySensorCondition;
import domuscontrol.routines.conditions.Operator;
import domuscontrol.routines.conditions.RainfallSensorCondition;
import domuscontrol.routines.conditions.TemperatureSensorCondition;
import domuscontrol.routines.conditions.TimeCondition;
import domuscontrol.simulation.WeatherCondition;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.Map;

/**
 * Stateless engine that analyses the interaction history of a house and
 * produces a list of automation or schedule suggestions based on detected patterns.
 *
 * Detects five types of patterns:
 * - Time-based (Schedule): same device action at consistent times of day
 * - Sequence-based (Automation): one device interaction followed by another within time window
 * - Temperature-based: same action when temperature is consistently hot or cold
 * - Luminosity-based: same action when luminosity is consistently bright or dark
 * - Rainfall-based: same action when rainfall conditions occur
 * 
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class SuggestionEngine {

    /** Minimum number of occurrences required to consider a pattern valid. */
    private static final int MIN_OCCURRENCES = 3;

    /** Maximum deviation in minutes from the cluster center for a schedule pattern. */
    private static final int SCHEDULE_WINDOW_MINUTES = 15;

    /** Maximum gap in minutes between two interactions to be considered a sequence. */
    private static final int SEQUENCE_WINDOW_MINUTES = 2;

    /** Temperature in °C below which the environment is considered cold. */
    private static final int COLD_TEMPERATURE_THRESHOLD = 16;

    /** Temperature in °C above which the environment is considered hot. */
    private static final int HOT_TEMPERATURE_THRESHOLD = 24;

    /** Luminosity in lux below which the environment is considered dark. */
    private static final int DARK_LUMINOSITY_THRESHOLD = 300;

    /** Luminosity in lux above which the environment is considered bright. */
    private static final int BRIGHT_LUMINOSITY_THRESHOLD = 700;

    /**
     * Private constructor because this utility class is not meant to be instantiated.
     */
    private SuggestionEngine() {
    }

    /**
     * Analyzes interaction logs and suggests automations based on detected patterns.
     *
     * @param logger The interaction logger containing user interaction history.
     * @param devices A map of available devices indexed by their IDs.
     * @param userId The ID of the user whose interactions should be analyzed.
     * @return A list of suggested automations based on detected patterns.
     * @throws ScheduleWithConditionDifferentFromTimeException If a schedule suggestion requires a TimeCondition.
     */
    public static List<AutomationSuggestion> suggest(InteractionLogger logger, Map<Integer, Device> devices, int userId) throws ScheduleWithConditionDifferentFromTimeException {
        List<AutomationSuggestion> suggestions = new ArrayList<>();
        List<DeviceInteraction> interactions = new ArrayList<>();
        for (DeviceInteraction i : logger.getInteractions()) {
            if (i.getUserId() == userId) interactions.add(i);
        }

        suggestions.addAll(detectSchedulePatterns(interactions, devices));
        suggestions.addAll(detectSequencePatterns(interactions, devices));
        suggestions.addAll(detectTemperaturePatterns(interactions, devices));
        suggestions.addAll(detectLuminosityPatterns(interactions, devices));
        suggestions.addAll(detectRainPatterns(interactions, devices));

        return suggestions;
    }

    /**
     * Groups interactions by (deviceId, type, value) and checks whether any group
     * contains MIN_OCCURRENCES or more entries whose time-of-day falls within
     * SCHEDULE_WINDOW_MINUTES of a common center. If so, builds a Schedule suggestion.
     *
     * @param interactions the full interaction list
     * @param devices the device map used to validate compatibility
     * @return a list of schedule-based suggestions
     */
    private static List<AutomationSuggestion> detectSchedulePatterns(
            List<DeviceInteraction> interactions, Map<Integer, Device> devices) throws ScheduleWithConditionDifferentFromTimeException {

        List<AutomationSuggestion> suggestions = new ArrayList<>();

        Map<String, List<DeviceInteraction>> groups = new HashMap<>();
        for (DeviceInteraction i : interactions) {
            String key = i.getDeviceId() + "_" + i.getType() + "_" + roundedValue(i.getValue());
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<String, List<DeviceInteraction>> entry : groups.entrySet()) {
            List<DeviceInteraction> group = entry.getValue();
            if (group.size() < MIN_OCCURRENCES) continue;

            List<LocalTime> times = new ArrayList<>();
            for (DeviceInteraction i : group) {
                times.add(i.getTimestamp().toLocalTime());
            }

            LocalTime clusterCenter = findClusterCenter(times);
            if (clusterCenter == null) continue;
            clusterCenter = clusterCenter.truncatedTo(ChronoUnit.MINUTES);

            DeviceInteraction sample = group.get(0);
            Device device = devices.get(sample.getDeviceId());
            if (device == null) continue;

            Action action = buildAction(sample, device);
            if (action == null) continue;

            List<Condition> conditions = new ArrayList<>();
            conditions.add(new TimeCondition(clusterCenter));

            List<Action> actions = new ArrayList<>();
            actions.add(action);

            String name = "Suggested Schedule: " + sample.getType()
                    + valueDescription(sample)
                    + " device " + sample.getDeviceId() + " at " + clusterCenter;
            String description = deviceLabel(device) + " was manually "
                    + sample.getType().toString().toLowerCase().replace("_", " ")
                    + valueDescription(sample)
                    + " at least " + MIN_OCCURRENCES + " times around " + clusterCenter + ".";

            Automation automation = new Automation(name, AutomationType.SCHEDULE, conditions, actions);
            suggestions.add(new AutomationSuggestion(description, automation));
        }

        return suggestions;
    }

    /**
     * Looks for pairs of interactions (A then B on a different device) that consistently
     * occur within SEQUENCE_WINDOW_MINUTES of each other at least MIN_OCCURRENCES times.
     * Any interaction type can be the trigger - not just switchable ones.
     * If found, builds an Automation suggestion: when device A is in the state it was
     * set to, perform the action on device B.
     *
     * @param interactions the full interaction list
     * @param devices the device map used to validate compatibility
     * @return a list of sequence-based suggestions
     */
    private static List<AutomationSuggestion> detectSequencePatterns(
            List<DeviceInteraction> interactions, Map<Integer, Device> devices) throws ScheduleWithConditionDifferentFromTimeException {

        List<AutomationSuggestion> suggestions = new ArrayList<>();

        Map<String, Integer> pairCount = new HashMap<>();
        Map<String, DeviceInteraction[]> pairSamples = new HashMap<>();

        for (int i = 0; i < interactions.size(); i++) {
            DeviceInteraction a = interactions.get(i);

            for (int j = i + 1; j < interactions.size(); j++) {
                DeviceInteraction b = interactions.get(j);
                if (b.getDeviceId() == a.getDeviceId()) continue;

                long gapMinutes = Duration.between(
                        a.getTimestamp(), b.getTimestamp()).toMinutes();
                if (gapMinutes < 0 || gapMinutes > SEQUENCE_WINDOW_MINUTES) break;

                String pairKey = a.getDeviceId() + "_" + a.getType() + "_" + roundedValue(a.getValue())
                        + "__" + b.getDeviceId() + "_" + b.getType() + "_" + roundedValue(b.getValue());

                pairCount.merge(pairKey, 1, Integer::sum);
                pairSamples.putIfAbsent(pairKey, new DeviceInteraction[]{a, b});
            }
        }

        for (Map.Entry<String, Integer> entry : pairCount.entrySet()) {
            if (entry.getValue() < MIN_OCCURRENCES) continue;

            DeviceInteraction[] samples = pairSamples.get(entry.getKey());
            DeviceInteraction triggerSample = samples[0];
            DeviceInteraction actionSample  = samples[1];

            Device triggerDevice = devices.get(triggerSample.getDeviceId());
            Device actionDevice  = devices.get(actionSample.getDeviceId());
            if (triggerDevice == null || actionDevice == null) continue;

            Condition condition = buildCondition(triggerSample, triggerDevice);
            if (condition == null) continue;

            Action action = buildAction(actionSample, actionDevice);
            if (action == null) continue;

            List<Condition> conditions = new ArrayList<>();
            conditions.add(condition);

            List<Action> actions = new ArrayList<>();
            actions.add(action);
            StringBuilder nameBuilder = new StringBuilder("Suggested Automation: when device ")
                    .append(triggerSample.getDeviceId())
                    .append(" ")
                    .append(triggerSample.getType())
                    .append(valueDescription(triggerSample))
                    .append(" -> ")
                    .append(actionSample.getType())
                    .append(valueDescription(actionSample))
                    .append(" device ")
                    .append(actionSample.getDeviceId());
            String name = nameBuilder.toString();
            StringBuilder descriptionBuilder = new StringBuilder("Every time ")
                    .append(deviceLabel(triggerDevice))
                    .append(" is ")
                    .append(triggerSample.getType().toString().toLowerCase().replace("_", " "))
                    .append(triggerSample.getValue() != null ? " to " + triggerSample.getValue().intValue() : "")
                    .append(", ")
                    .append(deviceLabel(actionDevice))
                    .append(" is then ")
                    .append(actionSample.getType().toString().toLowerCase().replace("_", " "))
                    .append(actionSample.getValue() != null ? " to " + actionSample.getValue().intValue() : "")
                    .append(". Detected ")
                    .append(entry.getValue())
                    .append(" times.");
            String description = descriptionBuilder.toString();
            Automation automation = new Automation(name, AutomationType.AUTOMATION, conditions, actions);
            suggestions.add(new AutomationSuggestion(description, automation));
        }

        return suggestions;
    }

    /**
     * Builds the appropriate Condition from a recorded interaction and its device type.
     * Returns null if the device does not implement the required interface for the interaction type.
     *
     * @param interaction the recorded interaction that acts as the trigger
     * @param device the device used to validate compatibility
     * @return the constructed Condition, or null if incompatible
     */
    private static Condition buildCondition(DeviceInteraction interaction, Device device) {
        switch (interaction.getType()) {
            case TURN_ON:
                if (!(device instanceof SwitchableDevice)) return null;
                return new DeviceStateCondition(interaction.getDeviceId(), true);

            case TURN_OFF:
                if (!(device instanceof SwitchableDevice)) return null;
                return new DeviceStateCondition(interaction.getDeviceId(), false);

            case SET_LEVEL:
                if (!(device instanceof AdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new DeviceLevelCondition(interaction.getDeviceId(), interaction.getValue().intValue(), Operator.EQUALS);

            case SET_OPENING:
                if (!(device instanceof OpenableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new DeviceOpenCondition(interaction.getDeviceId(), interaction.getValue().intValue(), Operator.EQUALS);

            case SET_COLOR_TEMPERATURE:
                if (!(device instanceof ColorAdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new ColorTemperatureCondition(interaction.getDeviceId(), interaction.getValue().intValue(), Operator.EQUALS);

            default:
                return null;
        }
    }

    /**
     * Builds the appropriate Action from a recorded interaction and its device type.
     * Returns null if the device does not implement the required interface for the action type.
     *
     * @param interaction the recorded interaction
     * @param device the device used to validate compatibility
     * @return the constructed Action, or null if incompatible
     */
    private static Action buildAction(DeviceInteraction interaction, Device device) {
        switch (interaction.getType()) {
            case TURN_ON:
                if (!(device instanceof SwitchableDevice)) return null;
                return new TurnOnAction(interaction.getDeviceId());

            case TURN_OFF:
                if (!(device instanceof SwitchableDevice)) return null;
                return new TurnOffAction(interaction.getDeviceId());

            case SET_LEVEL:
                if (!(device instanceof AdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetLevelAction(interaction.getDeviceId(), interaction.getValue().intValue());

            case SET_OPENING:
                if (!(device instanceof OpenableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetOpeningAction(interaction.getDeviceId(), interaction.getValue().intValue());

            case SET_COLOR_TEMPERATURE:
                if (!(device instanceof ColorAdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetColorTemperatureAction(interaction.getDeviceId(), interaction.getValue().intValue());

            default:
                return null;
        }
    }

    /**
     * Finds the center of a time cluster if at least MIN_OCCURRENCES times fall
     * within SCHEDULE_WINDOW_MINUTES of each other. Returns null if no such cluster exists.
     *
     * @param times The list of times-of-day to analyze.
     * @return The average LocalTime of the cluster, or null if no cluster is found.
     */
    private static LocalTime findClusterCenter(List<LocalTime> times) {
        for (LocalTime anchor : times) {
            List<LocalTime> cluster = new ArrayList<>();
            for (LocalTime t : times) {
                if (minutesBetween(anchor, t) <= SCHEDULE_WINDOW_MINUTES) {
                    cluster.add(t);
                }
            }
            if (cluster.size() >= MIN_OCCURRENCES) {
                return averageTime(cluster);
            }
        }
        return null;
    }

    /**
     * Computes the absolute difference in minutes between two LocalTime values,
     * accounting for the midnight wrap-around.
     *
     * @param a The first time.
     * @param b The second time.
     * @return The shortest circular distance in minutes between a and b.
     */
    private static long minutesBetween(LocalTime a, LocalTime b) {
        long diff = Math.abs(a.toSecondOfDay() - b.toSecondOfDay()) / 60;
        return Math.min(diff, 24 * 60 - diff);
    }

    /**
     * Computes the average LocalTime from a list of times by averaging their seconds-of-day.
     *
     * @param times The list of times to average.
     * @return The average time.
     */
    private static LocalTime averageTime(List<LocalTime> times) {
        double sin = 0.0;
        double cos = 0.0;
        for (LocalTime t : times) {
            double angle = 2.0 * Math.PI * t.toSecondOfDay() / (24.0 * 60.0 * 60.0);
            sin += Math.sin(angle);
            cos += Math.cos(angle);
        }

        double averageAngle = Math.atan2(sin / times.size(), cos / times.size());
        if (averageAngle < 0) averageAngle += 2.0 * Math.PI;

        long seconds = Math.round(averageAngle * 24.0 * 60.0 * 60.0 / (2.0 * Math.PI));
        return LocalTime.ofSecondOfDay(seconds % (24 * 60 * 60));
    }

    /**
     * Rounds a value to the nearest 5 for grouping purposes, or returns "null"
     * as a string if the value is absent.
     *
     * @param value The value to round, may be null.
     * @return The rounded value as a string, or "null".
     */
    private static String roundedValue(Double value) {
        if (value == null) return "null";
        return String.valueOf((int) (Math.round(value / 5.0) * 5));
    }

    /**
     * Formats the optional interaction value for suggestion descriptions.
     *
     * @param interaction The recorded interaction.
     * @return A formatted value suffix, or an empty string.
     */
    private static String valueDescription(DeviceInteraction interaction) {
        return interaction.getValue() != null ? " to " + interaction.getValue().intValue() : "";
    }

    /**
     * Looks for interactions that consistently occur when temperature is above or below certain thresholds.
     * If found, builds an Automation suggestion: when temperature is cold/hot, perform the action on the device.
     *
     * @param interactions the full interaction list
     * @param devices the device map used to validate compatibility
     * @return a list of temperature-based suggestions
     * @throws ScheduleWithConditionDifferentFromTimeException if a generated schedule contains a non-time condition (should never happen in this method)
     */
    private static List<AutomationSuggestion> detectTemperaturePatterns(
            List<DeviceInteraction> interactions, Map<Integer, Device> devices) throws ScheduleWithConditionDifferentFromTimeException {

        List<AutomationSuggestion> suggestions = new ArrayList<>();
        Integer sensorId = findSensorId(devices, d -> d instanceof TemperatureSensor);
        if (sensorId == null) return suggestions;

        Map<String, List<DeviceInteraction>> groups = new HashMap<>();
        for (DeviceInteraction i : interactions) {
            String band = temperatureBand(i.getOutsideTemperature());
            if (band == null) continue;
            String key = i.getDeviceId() + "_" + i.getType() + "_" + roundedValue(i.getValue()) + "_temp_" + band;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<String, List<DeviceInteraction>> entry : groups.entrySet()) {
            List<DeviceInteraction> group = entry.getValue();
            if (group.size() < MIN_OCCURRENCES) continue;

            DeviceInteraction sample = group.get(0);
            Device device = devices.get(sample.getDeviceId());
            if (device == null) continue;
            Action action = buildAction(sample, device);
            if (action == null) continue;

            String band = temperatureBand(sample.getOutsideTemperature());
            Condition condition;
            String conditionText;
            if ("COLD".equals(band)) {
                condition = new TemperatureSensorCondition(sensorId, COLD_TEMPERATURE_THRESHOLD, Operator.LESS_THAN);
                conditionText = "below " + COLD_TEMPERATURE_THRESHOLD + "ºC";
            } else {
                condition = new TemperatureSensorCondition(sensorId, HOT_TEMPERATURE_THRESHOLD, Operator.GREATER_THAN);
                conditionText = "above " + HOT_TEMPERATURE_THRESHOLD + "ºC";
            }

            String name = "Suggested Automation: temperature " + band + " -> " + sample.getType() + valueDescription(sample) + " device " + sample.getDeviceId();
            String description = deviceLabel(device) + " was manually "
                    + sample.getType().toString().toLowerCase().replace("_", " ")
                    + valueDescription(sample)
                    + " at least " + MIN_OCCURRENCES + " times when temperature sensor read " + conditionText + ".";

            Automation automation = new Automation(name, AutomationType.AUTOMATION, List.of(condition), List.of(action));
            suggestions.add(new AutomationSuggestion(description, automation));
        }
        return suggestions;
    }

    /**
     * Looks for interactions that consistently occur when luminosity is above or below certain thresholds.
     * If found, builds an Automation suggestion: when luminosity is bright/dark, perform the action on the device.
     * 
     * @param interactions the full interaction list
     * @param devices the device map used to validate compatibility
     * @return a list of luminosity-based suggestions
     * @throws ScheduleWithConditionDifferentFromTimeException if a generated schedule contains a non-time condition (should never happen in this method)
     */
    private static List<AutomationSuggestion> detectLuminosityPatterns(
            List<DeviceInteraction> interactions, Map<Integer, Device> devices) throws ScheduleWithConditionDifferentFromTimeException {

        List<AutomationSuggestion> suggestions = new ArrayList<>();
        Integer sensorId = findSensorId(devices, d -> d instanceof LuminositySensor);
        if (sensorId == null) return suggestions;

        Map<String, List<DeviceInteraction>> groups = new HashMap<>();
        for (DeviceInteraction i : interactions) {
            String band = luminosityBand(i.getLuminosity());
            if (band == null) continue;
            String key = i.getDeviceId() + "_" + i.getType() + "_" + roundedValue(i.getValue()) + "_lux_" + band;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<String, List<DeviceInteraction>> entry : groups.entrySet()) {
            List<DeviceInteraction> group = entry.getValue();
            if (group.size() < MIN_OCCURRENCES) continue;

            DeviceInteraction sample = group.get(0);
            Device device = devices.get(sample.getDeviceId());
            if (device == null) continue;
            Action action = buildAction(sample, device);
            if (action == null) continue;

            String band = luminosityBand(sample.getLuminosity());
            Condition condition;
            String conditionText;
            if ("DARK".equals(band)) {
                condition = new LuminositySensorCondition(sensorId, DARK_LUMINOSITY_THRESHOLD, Operator.LESS_THAN);
                conditionText = "below " + DARK_LUMINOSITY_THRESHOLD + " lx";
            } else {
                condition = new LuminositySensorCondition(sensorId, BRIGHT_LUMINOSITY_THRESHOLD, Operator.GREATER_THAN);
                conditionText = "above " + BRIGHT_LUMINOSITY_THRESHOLD + " lx";
            }

            String name = "Suggested Automation: luminosity " + band + " -> " + sample.getType() + valueDescription(sample) + " device " + sample.getDeviceId();
            String description = deviceLabel(device) + " was manually "
                    + sample.getType().toString().toLowerCase().replace("_", " ")
                    + valueDescription(sample)
                    + " at least " + MIN_OCCURRENCES + " times when luminosity sensor read " + conditionText + ".";

            Automation automation = new Automation(name, AutomationType.AUTOMATION, List.of(condition), List.of(action));
            suggestions.add(new AutomationSuggestion(description, automation));
        }
        return suggestions;
    }

    /**
     * Looks for interactions that consistently occur when rainfall conditions indicate rain.
     * If found, builds an Automation suggestion: when rainfall sensor indicates rain, perform the action on the device.
     * @param interactions the full interaction list
     * @param devices the device map used to validate compatibility
     * @return a list of rainfall-based suggestions
     * @throws ScheduleWithConditionDifferentFromTimeException if a generated schedule contains a non-time condition (should never happen in this method)
     */
    private static List<AutomationSuggestion> detectRainPatterns(
            List<DeviceInteraction> interactions, Map<Integer, Device> devices) throws ScheduleWithConditionDifferentFromTimeException {

        List<AutomationSuggestion> suggestions = new ArrayList<>();
        Integer sensorId = findSensorId(devices, d -> d instanceof RainfallSensor);
        if (sensorId == null) return suggestions;

        Map<String, List<DeviceInteraction>> groups = new HashMap<>();
        for (DeviceInteraction i : interactions) {
            if (i.getWeather() == null) continue;
            boolean raining = i.getWeather() == WeatherCondition.RAINING
                           || i.getWeather() == WeatherCondition.STORMY;
            if (!raining) continue;
            String key = i.getDeviceId() + "_" + i.getType() + "_" + roundedValue(i.getValue()) + "_rain";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<String, List<DeviceInteraction>> entry : groups.entrySet()) {
            List<DeviceInteraction> group = entry.getValue();
            if (group.size() < MIN_OCCURRENCES) continue;

            DeviceInteraction sample = group.get(0);
            Device device = devices.get(sample.getDeviceId());
            if (device == null) continue;
            Action action = buildAction(sample, device);
            if (action == null) continue;

            Condition condition = new RainfallSensorCondition(sensorId, 1.0, Operator.GREATER_THAN);

            String name = "Suggested Automation: raining -> " + sample.getType() + valueDescription(sample) + " device " + sample.getDeviceId();
            String description = deviceLabel(device) + " was manually "
                    + sample.getType().toString().toLowerCase().replace("_", " ")
                    + valueDescription(sample)
                    + " at least " + MIN_OCCURRENCES + " times while it was raining.";

            Automation automation = new Automation(name, AutomationType.AUTOMATION, List.of(condition), List.of(action));
            suggestions.add(new AutomationSuggestion(description, automation));
        }
        return suggestions;
    }

    /**
     * Finds the ID of the first device that matches the given predicate, or returns null if none found.
     * Used to locate the relevant sensor IDs for temperature, luminosity, and rainfall pattern detection.
     * @param devices the device map to search through
     * @param predicate the condition that the device must satisfy
     * @return the ID of the first matching device, or null if no match is found
     */
    private static Integer findSensorId(Map<Integer, Device> devices, Predicate<Device> predicate) {
        return devices.values().stream()
            .filter(predicate)
            .map(Device::getId)
            .findFirst()
            .orElse(null);
    }

    /**
     * Categorizes a temperature value into "COLD", "HOT", or null if in between, based on predefined thresholds.
     * Used for grouping interactions in temperature-based pattern detection.
     * @param temperature the temperature value to categorize, may be null
     * @return "COLD" if below COLD_TEMPERATURE_THRESHOLD, "HOT" if above HOT_TEMPERATURE_THRESHOLD, or null if in between or if input is null
     */
    private static String temperatureBand(Double temperature) {
        if (temperature == null) return null;
        if (temperature < COLD_TEMPERATURE_THRESHOLD) return "COLD";
        if (temperature > HOT_TEMPERATURE_THRESHOLD) return "HOT";
        return null;
    }

    /**
     * Categorizes a luminosity value into "DARK", "BRIGHT", or null if in between, based on predefined thresholds.
     * Used for grouping interactions in luminosity-based pattern detection.
     * @param luminosity the luminosity value to categorize, may be null
     * @return "DARK" if below DARK_LUMINOSITY_THRESHOLD, "BRIGHT" if above BRIGHT_LUMINOSITY_THRESHOLD, or null if in between or if input is null
     */
    private static String luminosityBand(Double luminosity) {
        if (luminosity == null) return null;
        if (luminosity < DARK_LUMINOSITY_THRESHOLD) return "DARK";
        if (luminosity > BRIGHT_LUMINOSITY_THRESHOLD) return "BRIGHT";
        return null;
    }

    /**
     * Formats a device's class name and brand/model into a descriptive label for suggestion descriptions.
     * @param device the device to format
     * @return a string label describing the device, including its type and brand/model
     */
    private static String deviceLabel(Device device) {
        return device.getClass().getSimpleName() + " '" + device.getBrand() + " " + device.getModel() + "' [#" + device.getId() + "]";
    }
}
