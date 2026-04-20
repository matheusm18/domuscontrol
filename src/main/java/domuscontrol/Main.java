package domuscontrol;

/**
 * The Main class serves as the entry point for the Domus Control application.
 * It initializes the application infrastructure and starts the main application loop.
 */
public class Main {
    public static void main(String[] args) {
        DomusControlApplication app = new DomusControlApplication();
        app.init();
        app.start();
    }
}