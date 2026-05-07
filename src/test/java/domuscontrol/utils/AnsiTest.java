package domuscontrol.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class AnsiTest {

    @Test
    void constantsExposeAnsiCodesAndWidth() {
        assertEquals("\033[0m", Ansi.RESET);
        assertEquals("\033[1m", Ansi.BOLD);
        assertEquals("\033[36m", Ansi.CYAN);
        assertEquals(47, Ansi.WIDTH);
    }

    @Test
    void promptFormatsLabel() {
        assertEquals(Ansi.CYAN + "  › " + Ansi.RESET + "Name: ", Ansi.prompt("Name"));
    }

    @Test
    void listMethodsPrintFormattedOutput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        try {
            System.setOut(new PrintStream(output));
            Ansi.listTitle("Devices");
            Ansi.listRow("Lamp");
            Ansi.listSeparator();
        } finally {
            System.setOut(original);
        }

        String text = output.toString();
        assertTrue(text.contains("Devices"));
        assertTrue(text.contains("Lamp"));
        assertTrue(text.contains("─".repeat(Ansi.WIDTH)));
    }
}
