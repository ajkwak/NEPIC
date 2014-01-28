package nepic.util;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * JUnit tests for {@link Verify}.
 *
 * @author AJ Parmidge
 */
public class VerifyTest {

    @Test
    public void notNull_NullObject_Throws() {
        String message = "Here's my message!";
        try {
            Verify.notNull(null, message);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void notNull_NonNullObject_Succeeds() {
        // None of these should throw.
        Verify.notNull("", "Message");
        Verify.notNull(new Point(9, 8), "Message");
        Verify.notNull(Lists.newArrayList(), "Message");
        Verify.notNull(Lists.newArrayList("", "voila"), "Message");
    }

    @Test
    public void argument_False_Throws() {
        String message = "Hello, world!";
        try {
            Verify.argument("" == "Voila", message);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void argument_True_Succeeds() {
        // None of these should throw.
        Verify.argument(true, "Message");
        Verify.argument(2 * 6 == new Integer(13) - 1, "Message");
        Verify.argument("" != null, "Message");
    }

    @Test
    public void state_False_Throws() {
        String message = "Hello, world!";
        try {
            Verify.state("" == "Voila", message);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void state_True_Succeeds() {
        // None of these should throw.
        Verify.state(true, "Message");
        Verify.state(2 * 6 == new Integer(13) - 1, "Message");
        Verify.state("" != null, "Message");
    }

    @Test(expected = NullPointerException.class)
    public void nonEmpty_NullCollection_Throws() {
        Verify.nonEmpty((Collection<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmpty_EmptyCollection_Throws() {
        Verify.nonEmpty(new ArrayList<String>());
    }

    @Test
    public void nonEmpty_NonEmptyCollection_Succeeds() {
        Verify.nonEmpty(Lists.newArrayList("Hello"));
        Verify.nonEmpty(Lists.newArrayList("Hello", "World"));
    }

    @Test(expected = NullPointerException.class)
    public void nonEmpty_NullString_Throws() {
        Verify.nonEmpty((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmpty_EmptyString_Throws() {
        Verify.nonEmpty("");
    }

    @Test
    public void nonEmpty_NonEmptyString_Succeeds() {
        Verify.nonEmpty(" ");
        Verify.nonEmpty("a");
        Verify.nonEmpty("Hello world!");
    }

    @Test(expected = NullPointerException.class)
    public void noNullElements_NullCollection_Throws() {
        Verify.noNullElements((List<Point>) null);
    }

    @Test(expected = NullPointerException.class)
    public void noNullElements_NullElement_Throws() {
        Verify.noNullElements(Lists.newArrayList("Hello", "World", null));
    }

    @Test
    public void noNullElements_Succeeds() {
        Verify.noNullElements(Lists.newArrayList("Hello", "World", ""));
    }
}
