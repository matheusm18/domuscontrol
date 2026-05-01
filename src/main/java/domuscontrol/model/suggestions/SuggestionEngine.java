package domuscontrol.model.suggestions;

import domuscontrol.model.device.Device;
import domuscontrol.model.device.types.AdjustableDevice;
import domuscontrol.model.device.types.ColorAdjustableDevice;
import domuscontrol.model.device.types.OpenableDevice;
import domuscontrol.model.device.types.SwitchableDevice;
import domuscontrol.model.routines.Automation;
import domuscontrol.model.routines.AutomationType;
import domuscontrol.model.routines.Action;
import domuscontrol.model.routines.Condition;
import domuscontrol.model.routines.actions.TurnOnAction;
import domuscontrol.model.routines.actions.TurnOffAction;
import domuscontrol.model.routines.actions.SetLevelAction;
import domuscontrol.model.routines.actions.SetOpeningAction;
import domuscontrol.model.routines.actions.SetColorTemperatureAction;
import domuscontrol.exceptions.ScheduleWithConditionDifferentFromTimeException;
import domuscontrol.model.routines.conditions.ColorTemperatureCondition;
import domuscontrol.model.routines.conditions.DeviceLevelCondition;
import domuscontrol.model.routines.conditions.DeviceOpenCondition;
import domuscontrol.model.routines.conditions.DeviceStateCondition;
import domuscontrol.model.routines.conditions.Operator;
import domuscontrol.model.routines.conditions.TimeCondition;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless engine that analyses the interaction history of a house and
 * produces a list of automation or schedule suggestions based on detected patterns.
 *
 * Two types of patterns are detected:
 * - Time-based (Schedule): the same device action occurs repeatedly around the same time of day.
 * - Sequence-based (Automation): one device interaction is consistently followed by another
 *   on a different device within a short time window.
 */
public class SuggestionEngine {

    /** Minimum number of occurrences required to consider a pattern valid. */
    private static final int MIN_OCCURRENCES = 3;

    /** Maximum deviation in minutes from the cluster center for a schedule pattern. */
    private static final int SCHEDULE_WINDOW_MINUTES = 15;

    /** Maximum gap in minutes between two interactions to be considered a sequence. */
    private static final int SEQUENCE_WINDOW_MINUTES = 2;

    /**
     * Private constructor — this class is not meant to be instantiated.
     */
    private SuggestionEngine() {}

    /**
     * Analyses the interaction log and produces a list of suggestions based on
     * detected time-based and sequence-based patterns.
     *
     * The live device map is passed directly from House to allow the engine to build
     * fully functional Action and Condition objects with real device references.
     *
     * @param logger  The interaction logger containing the house's history.
     * @param devices The live internal device map of the house.
     * @return A list of automation suggestions ready to be presented to the user.
     */
    public static List<AutomationSuggestion> suggest(InteractionLogger logger, Map<Integer, Device> devices, int userId) throws ScheduleWithConditionDifferentFromTimeException {
        List<AutomationSuggestion> suggestions = new ArrayList<>();
        List<DeviceInteraction> interactions = new ArrayList<>();
        for (DeviceInteraction i : logger.getInteractions()) {
            if (i.getUserId() == userId) interactions.add(i);
        }

        suggestions.addAll(detectSchedulePatterns(interactions, devices));
        suggestions.addAll(detectSequencePatterns(interactions, devices));

        return suggestions;
    }

    /**
     * Groups interactions by (deviceId, type, value) and checks whether any group
     * contains MIN_OCCURRENCES or more entries whose time-of-day falls within
     * SCHEDULE_WINDOW_MINUTES of a common center. If so, builds a Schedule suggestion.
     *
     * @param interactions The full interaction list.
     * @param devices      The live device map.
     * @return A list of schedule-based suggestions.
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
                    + " device " + sample.getDeviceId() + " at " + clusterCenter;
            String description = "Device '" + device.getModel() + "' was manually "
                    + sample.getType().toString().toLowerCase().replace("_", " ")
                    + " at least " + MIN_OCCURRENCES + " times around " + clusterCenter + ".";

            Automation automation = new Automation(name, AutomationType.SCHEDULE, conditions, actions);
            suggestions.add(new AutomationSuggestion(description, automation));
        }

        return suggestions;
    }

    /**
     * Looks for pairs of interactions (A then B on a different device) that consistently
     * occur within SEQUENCE_WINDOW_MINUTES of each other at least MIN_OCCURRENCES times.
     * Any interaction type can be the trigger — not just switchable ones.
     * If found, builds an Automation suggestion: when device A is in the state it was
     * set to, perform the action on device B.
     *
     * @param interactions The full interaction list.
     * @param devices      The live device map.
     * @return A list of sequence-based suggestions.
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

                long gapMinutes = java.time.Duration.between(
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
                    .append(" -> ")
                    .append(actionSample.getType())
                    .append(" device ")
                    .append(actionSample.getDeviceId());
            String name = nameBuilder.toString();
            StringBuilder descriptionBuilder = new StringBuilder("Every time '")
                    .append(triggerDevice.getModel())
                    .append("' is ")
                    .append(triggerSample.getType().toString().toLowerCase().replace("_", " "))
                    .append(triggerSample.getValue() != null ? " to " + triggerSample.getValue().intValue() : "")
                    .append(", '")
                    .append(actionDevice.getModel())
                    .append("' is then ")
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
     * Builds the appropriate Condition from a DeviceInteraction and its live device reference.
     * Returns null if the device does not implement the required interface for the interaction type.
     *
     * @param interaction The recorded interaction that acts as the trigger.
     * @param device      The live device reference.
     * @return The constructed Condition, or null if incompatible.
     */
    private static Condition buildCondition(DeviceInteraction interaction, Device device) {
        switch (interaction.getType()) {
            case TURN_ON:
                if (!(device instanceof SwitchableDevice)) return null;
                return new DeviceStateCondition((SwitchableDevice) device, true);

            case TURN_OFF:
                if (!(device instanceof SwitchableDevice)) return null;
                return new DeviceStateCondition((SwitchableDevice) device, false);

            case SET_LEVEL:
                if (!(device instanceof AdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new DeviceLevelCondition(
                        (AdjustableDevice) device, interaction.getValue().intValue(), Operator.EQUALS);

            case SET_OPENING:
                if (!(device instanceof OpenableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new DeviceOpenCondition(
                        (OpenableDevice) device, interaction.getValue().intValue(), Operator.EQUALS);

            case SET_COLOR_TEMPERATURE:
                if (!(device instanceof ColorAdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new ColorTemperatureCondition(
                        (ColorAdjustableDevice) device, interaction.getValue().intValue(), Operator.EQUALS);

            default:
                return null;
        }
    }

    // Helpers
    /**
     * Builds the appropriate Action from a DeviceInteraction and its live device reference.
     * Returns null if the device does not implement the required interface for the action type.
     *
     * @param interaction The recorded interaction.
     * @param device      The live device reference.
     * @return The constructed Action, or null if incompatible.
     */
    private static Action buildAction(DeviceInteraction interaction, Device device) {
        switch (interaction.getType()) {
            case TURN_ON:
                if (!(device instanceof SwitchableDevice)) return null;
                return new TurnOnAction((SwitchableDevice) device);

            case TURN_OFF:
                if (!(device instanceof SwitchableDevice)) return null;
                return new TurnOffAction((SwitchableDevice) device);

            case SET_LEVEL:
                if (!(device instanceof AdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetLevelAction((AdjustableDevice) device, interaction.getValue().intValue());

            case SET_OPENING:
                if (!(device instanceof OpenableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetOpeningAction((OpenableDevice) device, interaction.getValue().intValue());

            case SET_COLOR_TEMPERATURE:
                if (!(device instanceof ColorAdjustableDevice)) return null;
                if (interaction.getValue() == null) return null;
                return new SetColorTemperatureAction(
                        (ColorAdjustableDevice) device, interaction.getValue().intValue());

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
        long total = 0;
        for (LocalTime t : times) {
            total += t.toSecondOfDay();
        }
        return LocalTime.ofSecondOfDay(total / times.size());
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
}