package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.menu.Menu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Entry point of the MVC delegate pattern.
 * Acts simultaneously as View and Controller: holds the model directly,
 * builds Menu instances with lambda handlers, and delegates sub-flows to
 * {@link UserUI}. No separate Controller class exists - this class owns
 * the session state (current logged-in user email).
 */
public class DomusControlUI {

    private DomusControl model;
    private final Scanner sc;
    private final UserUI userUI;
    private String currentUserEmail;

    /**
     * Initialises the model, the shared Scanner, and the sub-UI chain.
     */
    public DomusControlUI() {
        this.model = new DomusControl();
        this.sc = new Scanner(System.in);
        this.userUI = new UserUI(this.model, this.sc);
        this.currentUserEmail = null;
    }

    public void run() {
        Menu menu = new Menu(new String[]{
                "Login",
                "Register",
                "Load State"
        });

        menu.setHandler(1, this::doLogin);
        menu.setHandler(2, this::doRegister);
        menu.setHandler(3, this::doLoadState);

        menu.run();
    }

    private void doLogin() {
        System.out.print("Email or Name: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        try {
            this.currentUserEmail = model.validateLogin(user, password).getEmail();
            userUI.show(this.currentUserEmail);
        } catch (UserNotFoundException e) {
            System.out.println("No account found with "+ user + ".");
        } catch (LoginInvalidPasswordException e) {
            System.out.println("Incorrect password.");
        }
    }

    private void doRegister() {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        try {
            model.registerUser(name, email, password);
            System.out.println("Registered successfully.");
            this.currentUserEmail = email;
            userUI.show(this.currentUserEmail);
        } catch (UserAlreadyExistsException e) {
            System.out.println("Email already registered.");
        }
    }

    private void doLoadState() {
        System.out.print("File name: ");
        String path = "saves/" + sc.nextLine().trim();
        try {
            this.model = DomusControl.loadState(path);
            this.userUI.setModel(this.model);
            System.out.println("State loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + path);
        } catch (IOException e) {
            System.out.println("Error loading state: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading state: corrupted file.");
        }
    }
}
