package domuscontrol.suggestions;

import domuscontrol.routines.Automation;
import domuscontrol.routines.AutomationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutomationSuggestionTest {

    @Test
    void defaultConstructorCreatesEmptySuggestion() {
        AutomationSuggestion suggestion = new AutomationSuggestion();

        assertEquals("", suggestion.getDescription());
        assertNull(suggestion.getAutomation());
    }

    @Test
    void constructorAndSettersCopyAutomation() throws Exception {
        Automation automation = new Automation("Auto", AutomationType.AUTOMATION, null, null);
        AutomationSuggestion suggestion = new AutomationSuggestion("Use this", automation);
        automation.setName("Changed");

        assertEquals("Use this", suggestion.getDescription());
        assertEquals("Auto", suggestion.getAutomation().getName());

        suggestion.setDescription("Other");
        suggestion.setAutomation(null);

        assertEquals("Other", suggestion.getDescription());
        assertNull(suggestion.getAutomation());
    }

    @Test
    void getAutomationReturnsClone() throws Exception {
        Automation automation = new Automation("Auto", AutomationType.AUTOMATION, null, null);
        AutomationSuggestion suggestion = new AutomationSuggestion("Use this", automation);

        suggestion.getAutomation().setName("Changed");

        assertEquals("Auto", suggestion.getAutomation().getName());
    }

    @Test
    void cloneEqualsHashCodeAndToStringUseFields() throws Exception {
        Automation automation = new Automation("Auto", AutomationType.AUTOMATION, null, null);
        AutomationSuggestion suggestion = new AutomationSuggestion("Use this", automation);

        AutomationSuggestion copy = suggestion.clone();

        assertNotSame(suggestion, copy);
        assertEquals(suggestion, copy);
        assertEquals(suggestion.hashCode(), copy.hashCode());
        assertEquals("AutomationSuggestion { Description: 'Use this', Automation: Auto }", suggestion.toString());
    }
}
