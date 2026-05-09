package domuscontrol.menu;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MenuTest {

    @Test
    void runExecutesSelectedHandlerAndStops() throws Exception {
        setMenuInput("1\n");
        AtomicInteger calls = new AtomicInteger();
        Menu menu = new Menu("Test", new String[] {"Do it"}, () -> "2026-01-01  12:00\n20.0ºC  SUNNY  500 lx");
        menu.setHandler(1, () -> {
            calls.incrementAndGet();
            menu.stop();
        });

        captureOutput(menu::run);

        assertEquals(1, calls.get());
    }

    @Test
    void runDoesNotExecuteUnavailableOption() throws Exception {
        setMenuInput("1\n0\n");
        AtomicInteger calls = new AtomicInteger();
        Menu menu = new Menu("Test", new String[] {"Do it"}, () -> "2026-01-01  12:00\n20.0ºC  SUNNY  500 lx");
        menu.setPreCondition(1, () -> false);
        menu.setHandler(1, calls::incrementAndGet);

        String output = captureOutput(menu::run);

        assertEquals(0, calls.get());
        assertTrue(output.contains("Option unavailable."));
    }

    @Test
    void invalidInputPrintsInvalidOptionAndCanExit() throws Exception {
        setMenuInput("x\n0\n");
        Menu menu = new Menu("Test", new String[] {"Do it"}, () -> "2026-01-01  12:00\n20.0ºC  SUNNY  500 lx");

        String output = captureOutput(menu::run);

        assertTrue(output.contains("Invalid option."));
    }

    @Test
    void setExitLabelChangesRenderedExitText() throws Exception {
        setMenuInput("0\n");
        Menu menu = new Menu("Test", new String[] {"Do it"}, () -> "2026-01-01  12:00\n20.0ºC  SUNNY  500 lx");
        menu.setExitLabel("Quit");

        String output = captureOutput(menu::run);

        assertTrue(output.contains("Quit"));
    }

    private void setMenuInput(String input) throws Exception {
        Field field = Menu.class.getDeclaredField("is");
        field.setAccessible(true);
        field.set(null, new Scanner(new ByteArrayInputStream(input.getBytes())));
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
