package domuscontrol.ui;

import domuscontrol.DomusControl;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.menu.Menu;
import domuscontrol.utils.Ansi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class DomusControlUI {

    private DomusControl model;
    private final Scanner sc;
    private final UserUI userUI;
    private String currentUserEmail;

    public DomusControlUI() {
        this.model = new DomusControl();
        this.sc = new Scanner(System.in);
        this.userUI = new UserUI(this.model, this.sc);
        this.currentUserEmail = null;
    }

    public void run() {
        printWelcome();
        Menu menu = new Menu(new String[]{
                "Login",
                "Register",
                "Load State"
        }, model::getCurrentState);

        menu.setHandler(1, this::doLogin);
        menu.setHandler(2, this::doRegister);
        menu.setHandler(3, this::doLoadState);

        menu.run();
    }

    private void printWelcome() {
        int w = 42;
        String horiz = "═".repeat(w);
        String blank = Ansi.CYAN + " ║" + " ".repeat(w) + "║" + Ansi.RESET;
        String line1 = centeredRow("D O M U S  C O N T R O L", w, true);
        String line2 = centeredRow("Smart Home Automation", w, false);
        System.out.println();
        System.out.println(Ansi.CYAN + " ╔" + horiz + "╗" + Ansi.RESET);
        System.out.println(blank);
        System.out.println(line1);
        System.out.println(line2);
        System.out.println(blank);
        System.out.println(Ansi.CYAN + " ╚" + horiz + "╝" + Ansi.RESET);
        System.out.println();
    }

    private String centeredRow(String text, int width, boolean bold) {
        int pad = Math.max(0, width - text.length());
        int left = pad / 2;
        int right = pad - left;
        String styled = bold
            ? Ansi.BOLD + Ansi.WHITE + text + Ansi.RESET
            : Ansi.DIM + text + Ansi.RESET;
        return Ansi.CYAN + " ║" + Ansi.RESET + " ".repeat(left) + styled + " ".repeat(right) + Ansi.CYAN + "║" + Ansi.RESET;
    }

    private void doLogin() {
        System.out.print(Ansi.prompt("Email or Name"));
        String user = sc.nextLine();
        System.out.print(Ansi.prompt("Password"));
        String password = sc.nextLine();
        try {
            this.currentUserEmail = model.validateLogin(user, password).getEmail();
            userUI.show(this.currentUserEmail);
        } catch (UserNotFoundException e) {
            System.out.println("  No account found with " + user + ".");
        } catch (LoginInvalidPasswordException e) {
            System.out.println("  Incorrect password.");
        }
    }

    private void doRegister() {
        System.out.print(Ansi.prompt("Name"));
        String name = sc.nextLine();
        System.out.print(Ansi.prompt("Email"));
        String email = sc.nextLine();
        System.out.print(Ansi.prompt("Password"));
        String password = sc.nextLine();
        try {
            model.registerUser(name, email, password);
            System.out.println("  Registered successfully.");
            this.currentUserEmail = email;
            userUI.show(this.currentUserEmail);
        } catch (UserAlreadyExistsException e) {
            System.out.println("  Email already registered.");
        }
    }

    private void doLoadState() {
        System.out.print(Ansi.prompt("File name"));
        String path = "saves/" + sc.nextLine().trim();
        try {
            this.model = DomusControl.loadState(path);
            this.userUI.setModel(this.model);
            System.out.println("  State loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("  File not found: " + path);
        } catch (IOException e) {
            System.out.println("  Error loading state: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("  Error loading state: corrupted file.");
        }
    }
}
