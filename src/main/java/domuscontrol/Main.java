package domuscontrol;

import domuscontrol.ui.DomusControlUI;

/**
 * Application entry point.
 * Bootstraps the MVC delegate chain by instantiating {@link domuscontrol.ui.DomusControlUI},
 * which owns the model and acts as the top-level View/Controller.
 */
public class Main {
    public static void main(String[] args) {
        new DomusControlUI().run();
    }
}