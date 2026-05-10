package domuscontrol.utils;

/**
 * Utility class for ANSI escape sequences and console formatting.
 * Provides constants for text styling (bold, dim, colors) and helper methods for formatted output.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Ansi {

    /**
     * Private default constructor to prevent instantiation of this utility class.
     */
    private Ansi() {
    }

    /**
     * ANSI code to reset all text formatting.
     */
    public static final String RESET  = "\033[0m";

    /**
     * ANSI code for bold text.
     */
    public static final String BOLD   = "\033[1m";

    /**
     * ANSI code for dimmed text.
     */
    public static final String DIM    = "\033[2m";

    /**
     * ANSI code for cyan text color.
     */
    public static final String CYAN   = "\033[36m";

    /**
     * ANSI code for yellow text color.
     */
    public static final String YELLOW = "\033[33m";

    /**
     * ANSI code for green text color.
     */
    public static final String GREEN  = "\033[32m";

    /**
     * ANSI code for white text color.
     */
    public static final String WHITE  = "\033[97m";

    /**
     * The standard console width for formatted output.
     */
    public static final int WIDTH = 47;

    /**
     * Formats a prompt string for user input with styling.
     *
     * @param label the prompt label
     * @return formatted prompt string with ANSI styling
     */
    public static String prompt(String label) {
        return CYAN + "  › " + RESET + label + ": ";
    }

    /**
     * Prints a formatted list title.
     *
     * @param title the title to display
     */
    public static void listTitle(String title) {
        System.out.println("\n" + BOLD + WHITE + "  " + title + RESET);
        System.out.println(CYAN + "  " + "─".repeat(WIDTH) + RESET);
    }

    /**
     * Prints a formatted separator line.
     */
    public static void listSeparator() {
        System.out.println(CYAN + "  " + "─".repeat(WIDTH) + RESET);
    }

    /**
     * Prints a formatted list row.
     *
     * @param content the content to display
     */
    public static void listRow(String content) {
        System.out.println("  " + content);
    }

    /**
     * Prints a dimmed error or invalid-input message.
     *
     * @param message the message to display
     */
    public static void error(String message) {
        System.out.println(DIM + "  " + message + RESET);
    }
}
