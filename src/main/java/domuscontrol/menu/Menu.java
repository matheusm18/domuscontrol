package domuscontrol.menu;

import domuscontrol.simulation.SimulationState;
import domuscontrol.utils.Ansi;
import java.util.*;
import java.util.function.Supplier;

public class Menu {

    public interface Handler {
        void execute();
    }

    public interface PreCondition {
        boolean validate();
    }

    private static final int WIDTH = Ansi.WIDTH;
    private static Scanner is = new Scanner(System.in);

    private final String title;
    private final Supplier<SimulationState> state;
    private List<String> opcoes;
    private List<PreCondition> disponivel;
    private List<Handler> handlers;
    private boolean stopped = false;
    private String exitLabel = "Back";

    public Menu(String[] opcoes, Supplier<SimulationState> state) {
        this("DomusControl", opcoes, state);
    }

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

    public void stop() {
        this.stopped = true;
    }

    public void setExitLabel(String exitLabel) {
        this.exitLabel = exitLabel;
    }

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

    public void setPreCondition(int i, PreCondition b) {
        this.disponivel.set(i - 1, b);
    }

    public void setHandler(int i, Handler h) {
        this.handlers.set(i - 1, h);
    }

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

    private void printTitle(String text) {
        int pad = Math.max(0, WIDTH - text.length());
        int left = pad / 2;
        int right = pad - left;
        String content = " ".repeat(left) + Ansi.BOLD + Ansi.WHITE + text + Ansi.RESET + " ".repeat(right);
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

    private void printState(SimulationState stateObj) {
        String line1 = stateObj.getCurrentDateTime().toLocalDate().toString() + "  " + stateObj.getCurrentDateTime().toLocalTime().toString();
        String line2 = stateObj.getTemperature() + "ºC  " + stateObj.getWeather().toString();

        printCenteredLine(line1);
        printCenteredLine(line2);
    }
    
    private void printCenteredLine(String text) {
        int pad = Math.max(0, WIDTH - text.length());
        int left = pad / 2;
        int right = pad - left;
        String content = " ".repeat(left) + Ansi.WHITE + text + Ansi.RESET + " ".repeat(right);
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

    private void printOption(String num, String text, boolean available) {
        String visible = "  " + num + "  " + text;
        int pad = Math.max(0, WIDTH - visible.length());
        String content;
        if (available) {
            content = "  " + Ansi.YELLOW + Ansi.BOLD + num + Ansi.RESET + "  " + text + " ".repeat(pad);
        } else {
            content = Ansi.DIM + visible + " ".repeat(pad) + Ansi.RESET;
        }
        System.out.println(Ansi.CYAN + " ║" + Ansi.RESET + content + Ansi.CYAN + "║" + Ansi.RESET);
    }

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
