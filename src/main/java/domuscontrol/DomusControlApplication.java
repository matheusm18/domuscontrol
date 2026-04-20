package domuscontrol;

public class DomusControlApplication {

    private DomusControlController controller;

    public void init() {
        this.controller = new DomusControlController();
    }

    /** Starts the main menu loop. Blocks until the user exits. */
    public void start() {
        System.out.println("Welcome to DomusControl!");
    }
}