package domuscontrol.ui;

import domuscontrol.DomusControl;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ActionsUITest {

    @Test
    void constructorAndSetModelDoNotThrow() {
        ActionsUI ui = new ActionsUI(new DomusControl(), new Scanner(""));

        assertDoesNotThrow(() -> ui.setModel(new DomusControl()));
    }
}
