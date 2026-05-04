package domuscontrol.utils;

public class Ansi {
    public static final String RESET  = "\033[0m";
    public static final String BOLD   = "\033[1m";
    public static final String DIM    = "\033[2m";
    public static final String CYAN   = "\033[36m";
    public static final String YELLOW = "\033[33m";
    public static final String GREEN  = "\033[32m";
    public static final String WHITE  = "\033[97m";

    public static final int WIDTH = 47;

    public static String prompt(String label) {
        return CYAN + "  › " + RESET + label + ": ";
    }

    public static void listTitle(String title) {
        System.out.println("\n" + BOLD + WHITE + "  " + title + RESET);
        System.out.println(CYAN + "  " + "─".repeat(WIDTH) + RESET);
    }

    public static void listSeparator() {
        System.out.println(CYAN + "  " + "─".repeat(WIDTH) + RESET);
    }

    public static void listRow(String content) {
        System.out.println("  " + content);
    }
}
