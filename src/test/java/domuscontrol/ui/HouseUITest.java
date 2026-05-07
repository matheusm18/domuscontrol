package domuscontrol.ui;

import domuscontrol.DomusControl;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class HouseUITest {

    @Test
    void constructorAndSetModelDoNotThrow() {
        HouseUI ui = new HouseUI(new DomusControl(), new Scanner(""));

        assertDoesNotThrow(() -> ui.setModel(new DomusControl()));
    }
}
