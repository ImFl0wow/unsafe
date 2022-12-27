package tools.unsafe.reflection.mimic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MimicTest {

    @Test
    public void testMimic() {
        String str = "foo";
        Measurable measurable = Mimic.as(
                Measurable.class,
                str
        );

        assertEquals(str.length(), measurable.length());

    }

    public static interface Measurable {

        int length();

    }


}