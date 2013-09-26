package nepic.util;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * JUnit tests for {@link Verify}.
 *
 * @author AJ Parmidge
 */
public class VerifyTest {

    @Test(expected = NullPointerException.class)
    public void notNull_NullObject_Throws() {
        Verify.notNull(null);
    }

    @Test
    public void notNull_NullObject_ThrowsWithMessage() {
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
        Verify.notNull("");
        Verify.notNull(new Point(9, 8));
        Verify.notNull(Lists.newArrayList());
        Verify.notNull(Lists.newArrayList("", "voila"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void argument_False_Throws() {
        Verify.argument(false);
    }

    @Test
    public void argument_False_ThrowsWithMessage() {
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
        Verify.argument(true);
        Verify.argument(2 * 6 == new Integer(13) - 1);
        Verify.argument("" != null);
    }

    @Test(expected = IllegalStateException.class)
    public void state_False_Throws() {
        Verify.state(false);
    }

    @Test
    public void state_False_ThrowsWithMessage() {
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
        Verify.state(true);
        Verify.state(2 * 6 == new Integer(13) - 1);
        Verify.state("" != null);
    }

    @Test(expected = NullPointerException.class)
    public void nonEmpty_NullIterable_Throws() {
        Verify.nonEmpty((Iterable<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmpty_EmptyCollection_Throws() {
        Verify.nonEmpty(new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmpty_EmptyIterable_Throws() {
        Verify.nonEmpty(newIterable());
    }

    @Test
    public void nonEmpty_NonEmptyCollection_Succeeds() {
        Verify.nonEmpty(Lists.newArrayList("Hello", "World"));
    }

    @Test
    public void nonEmpty_NonEmptyIterable_Throws() {
        Verify.nonEmpty(newIterable("Voila"));
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

    private <T> Iterable<T> newIterable(final T... elements) {
        return new Iterable<T>(){
            List<T> list = Lists.newArrayList(elements);

            @Override
            public Iterator<T> iterator() {
                return list.iterator();
            }
        };
    }
}
