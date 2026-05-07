package domuscontrol.menu;

import domuscontrol.simulation.SimulationState;
import domuscontrol.utils.Ansi;
import java.util.*;
import java.util.function.Supplier;

/**
 * Menu class for displaying interactive console menus with options, handlers, and preconditions.
 * Supports dynamic option availability and custom handlers for each menu entry.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Menu {

    /** 
     * Interface for defining menu option handlers. 
     */
    public interface Handler {
        /**
         * Executes the menu option.
         */
        void execute();
    }

    /**
     * Interface for defining menu option preconditions.
     */
    public interface PreCondition {
        /**
         * Validates whether the menu option is executable.
         *
         * @return true if the menu option is valid and can be executed, false otherwise.
         */
        boolean validate();
    }


    /**
     * The width of the menu display.
     */
    private static final int WIDTH = Ansi.WIDTH;

    /**
     * Scanner for reading user input.
     */
    private static Scanner is = new Scanner(System.in);

    /**
     * The menu title.
     */
    private final String title;

    /**
     * Supplier for the current simulation state (for display).
     */
    private final Supplier<SimulationState> state;

    /**
     * List of menu option labels.
     */
    private List<String> opcoes;

    /**
     * List of preconditions for each menu option.
     */
    private List<PreCondition> disponivel;

    /**
     * List of handlers for each menu option.
     */
    private List<Handler> handlers;

    /**
     * Indicates if the menu has been stopped.
     */
    private boolean stopped = false;

    /**
     * Label for the exit option.
     */
    private String exitLabel = "Back";

    /**
     * Constructs a menu with the default title and given options.
     *
     * @param opcoes the menu options
     * @param state the simulation state supplier
     */
    public Menu(String[] opcoes, Supplier<SimulationState> state) {
        this("DomusControl", opcoes, state);
    }

    /**
     * Constructs a menu with a custom title and options.
     *
     * @param title the menu title
     * @param opcoes the menu options
     * @param state the simulation state supplier
     */
    public Menu(String title, String[] opcoes, Supplier<SimulationState> state) {
        this.title = title;
        this.state = state;
        this.opcoes = Arrays.asList(opcoes);
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.opcoes.forEach(s -> {
            this.disponivel.add(() -> true);
            this.handlers.add(() -> System.out.println(Ansi.YELLOW + "  Option not implemented." + Ansi.RESET));
        });
    }

    /**
     * Stops the menu loop.
     */
    public void stop() {
        this.stopped = true;
    }

    /**
     * Sets the label for the exit option.
     *
     * @param exitLabel the exit label
     */
    public void setExitLabel(String exitLabel) {
        this.exitLabel = exitLabel;
    }

    /**
     * Runs the menu loop, displaying options and handling user input.
     */
    public void run() {
        int op;
        stopped = false;
        do {
            show();
            op = readOption();
            if (op > 0 && !this.disponivel.get(op - 1).validate()) {
                System.out.println(Ansi.DIM + "  Option unavailable." + Ansi.RESET);
            } else if (op > 0) {
                this.handlers.get(op - 1).execute();
            }
        } while (op != 0 && !stopped);
    }

    /**
     * Sets a precondition for a menu option.
     *
     * @param i the option index (1-based)
     * @param b the precondition
     */
    public void setPreCondition(int i, PreCondition b) {
        this.disponivel.set(i - 1, b);
    }

    /**
     * Sets a handler for a menu option.
     *
     * @param i the option index (1-based)
     * @param h the handler to execute
     */
    public void setHandler(int i, Handler h) {
        this.handlers.set(i - 1, h);
    }

    /**
     * Displays the menu and all options.
     */
    private void show() {
        String horiz = "═".repeat(WIDTH);
        System.out.println();
        System.out.println(Ansi.CYAN + " ╔" + horiz + "╗" + Ansi.RESET);
        printTitle(title);
        System.out.println(Ansi.CYAN + " ║" + " ".repeat(WIDTH) + "║" + Ansi.RESET);
        printState(state.get());
        System.out.println(Ansi.CYAN + " ╠" + horiz + "╣" + Ansi.RESET);
        for (int i = 0; i < opcoes.size(); i++) {
            boolean avail = disponivel.get(i).validate();
            printOption(String.valueOf(i + 1), avail ? opcoes.get(i) : "---", avail);
        }
        System.out.println(Ansi.CYAN + " ╠" + horiz + "╣" + Ansi.RESET);
        printOption("0", exitLabel, true);
        System.out.println(Ansi.CYAN + " ╚" + horiz + "╝" + Ansi.RESET);
    }

    /**
     * Prints the menu title centered.
     *
     * @param text the title text
     */
    private void printTitle(String text) {
        int pad = Math.max(0, WIDTH - text.length());
        int left = pad / 2;
        int right = pad - left;
        String content = " ".repeat(left) + Ansi.BOLD + Ansi.WHITE + text + Ansi.RESET + " ".repeat(right);
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

    /**
     * Prints the simulation state information.
     *
     * @param stateObj the simulation state
     */
    private void printState(SimulationState stateObj) {
        String line1 = stateObj.getCurrentDateTime().toLocalDate().toString() + "  " + stateObj.getCurrentDateTime().toLocalTime().toString();
        String line2 = String.format("%.1f", stateObj.getTemperature()) + "ºC  " + stateObj.getWeather().toString()
                + "  " + String.format("%.0f", stateObj.getLuminosity()) + " lx";

        printCenteredLine(line1);
        printCenteredLine(line2);
    }
    
    /**
     * Prints a centered line in the menu.
     *
     * @param text the text to print
     */
    private void printCenteredLine(String text) {
        int pad = Math.max(0, WIDTH - text.length());
        int left = pad / 2;
        int right = pad - left;
        String content = " ".repeat(left) + Ansi.WHITE + text + Ansi.RESET + " ".repeat(right);
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

    /**
     * Prints a menu option line.
     *
     * @param num the option number
     * @param text the option label
     * @param available whether the option is available
     */
    private void printOption(String num, String text, boolean available) {
        int numberWidth = Math.max(2, String.valueOf(this.opcoes.size()).length());
        String paddedNum = String.format("%" + numberWidth + "s", num);
        String visible = "  " + paddedNum + "  " + text;
        int pad = Math.max(0, WIDTH - visible.length());
        String content;
        if (available) {
            content = "  " + Ansi.YELLOW + Ansi.BOLD + paddedNum + Ansi.RESET + "  " + text + " ".repeat(pad);
        } else {
            content = Ansi.DIM + visible + " ".repeat(pad) + Ansi.RESET;
        }
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

    /**
     * Reads the user's option selection.
     * @return the selected option or -1 if invalid.
     */
    private int readOption() {
        int op;
        System.out.print(Ansi.prompt("Option"));
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            op = -1;
        }
        if (op < 0 || op > this.opcoes.size()) {
            System.out.println(Ansi.DIM + "  Invalid option." + Ansi.RESET);
            op = -1;
        }
        return op;
    }
}
