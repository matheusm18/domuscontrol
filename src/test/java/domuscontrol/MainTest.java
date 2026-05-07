package domuscontrol;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void mainMethodExistsWithExpectedSignature() throws Exception {
        Method main = Main.class.getMethod("main", String[].class);

        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }
}
