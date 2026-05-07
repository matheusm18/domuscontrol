package domuscontrol.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DomusControlUITest {

    @Test
    void constructorCreatesUi() {
        assertDoesNotThrow(DomusControlUI::new);
    }
}
