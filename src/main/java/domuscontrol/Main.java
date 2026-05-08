package domuscontrol;

import domuscontrol.ui.DomusControlUI;

/**
 * Application entry point.
 * Bootstraps the MVC delegate chain by instantiating {@link DomusControlUI},
 * which owns the model and acts as the top-level View/Controller.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class Main {

    /** 
     * Private default constructor to prevent instantiation of this class. 
     * This class is not meant to be instantiated, as it only contains the main method to start the application.
     */
    private Main() {
    }

    /**
     * Main method that starts the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        new DomusControlUI().run();
    }
}
