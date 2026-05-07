package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.user.User;
import domuscontrol.user.UserRole;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class UserUITest {

    @Test
    void constructorAndSetModelDoNotThrow() {
        UserUI ui = new UserUI(new DomusControl(), new Scanner(""));

        assertDoesNotThrow(() -> ui.setModel(new DomusControl()));
    }

    @Test
    void showUserDetailsPrintsFallbackForNullUser() {
        UserUI ui = new UserUI(new DomusControl(), new Scanner(""));

        String output = captureOutput(() -> ui.showUserDetails(null));

        assertTrue(output.contains("No user to display."));
    }

    @Test
    void showUserDetailsPrintsUserFields() {
        UserUI ui = new UserUI(new DomusControl(), new Scanner(""));
        User user = new User("Ada", "ada@example.com", "secret", Map.of(1, UserRole.USER));

        String output = captureOutput(() -> ui.showUserDetails(user));

        assertTrue(output.contains("Profile"));
        assertTrue(output.contains("Ada"));
        assertTrue(output.contains("ada@example.com"));
        assertTrue(output.contains("House ID 1 not found"));
    }

    private String captureOutput(Runnable runnable) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            runnable.run();
        } finally {
            System.setOut(original);
        }
        return output.toString();
    }
}
