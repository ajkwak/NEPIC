package nepic.util;

import static nepic.testing.util.Assertions.assertContains;
import static nepic.testing.util.Assertions.assertContainsIgnoreCase;
import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * JUnit tests for {@link Verify}.
 *
 * @author AJ Parmidge
 */
public class VerifyTest {

    @Test
    public void notNull_nullObject_throwsWithMessage() {
        String message = "Here's my message!";
        try {
            Verify.notNull(null, message);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void notNull_nonNullObject_succeeds() {
        // None of these should throw.
        Verify.notNull("", "Message");
        Verify.notNull(new Point(9, 8), "Message");
        Verify.notNull(Lists.newArrayList(), "Message");
        Verify.notNull(Lists.newArrayList("", "voila"), "Message");
    }

    @Test
    public void argument_false_throwsWithMessage() {
        String message = "Hello, world!";
        try {
            Verify.argument("" == "Voila", message);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void argument_true_succeeds() {
        // None of these should throw.
        Verify.argument(true, "Message");
        Verify.argument(2 * 6 == new Integer(13) - 1, "Message");
        Verify.argument("" != null, "Message");
    }

    @Test
    public void state_false_throwsWithMessage() {
        String message = "Hello, world!";
        try {
            Verify.state("" == "Voila", message);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals(message, expected.getMessage());
        }
    }

    @Test
    public void state_true_succeeds() {
        // None of these should throw.
        Verify.state(true, "Message");
        Verify.state(2 * 6 == new Integer(13) - 1, "Message");
        Verify.state("" != null, "Message");
    }

    @Test(expected = NullPointerException.class)
    public void nonEmpty_nullCollection_throws() {
        Verify.nonEmpty((Collection<?>) null);
    }

    @Test
    public void nonEmpty_nullCollection_throwsWithMessage() {
        String collectionName = "My Collection";
        try {
            Verify.nonEmpty((Collection<?>) null, collectionName);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertContains(expected.getMessage(), collectionName);
        }
    }

    @Test
    public void nonEmpty_emptyCollection_throws() {
        try {
            Verify.nonEmpty(new ArrayList<String>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertContainsIgnoreCase(expected.getMessage(), "empty");
        }
    }

    @Test
    public void nonEmpty_emptyCollection_throwsWithMessage() {
        String collectionName = "My Collection";
        try {
            Verify.nonEmpty(new ArrayList<String>(), collectionName);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            String message = expected.getMessage();
            assertContains(message, collectionName);
            assertContainsIgnoreCase(message, "empty");
        }
    }

    @Test
    public void nonEmpty_nonEmptyCollection_succeeds() {
        // Call method without message.
        Verify.nonEmpty(Lists.newArrayList("Hello"));
        Verify.nonEmpty(Lists.newArrayList("Hello", "World"));

        // Call method with message.
        Verify.nonEmpty(Lists.newArrayList("Hello"), "My Collection");
        Verify.nonEmpty(Lists.newArrayList("Hello", "World"), "My Collection");
    }

    @Test(expected = NullPointerException.class)
    public void nonEmpty_nullString_throws() {
        Verify.nonEmpty((String) null);
    }

    @Test
    public void nonEmpty_nullString_throwsWithMessage() {
        String stringName = "My String";
        try {
            Verify.nonEmpty((String) null, stringName);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertContains(expected.getMessage(), stringName);
        }
    }

    @Test
    public void nonEmpty_emptyString_throws() {
        try {
            Verify.nonEmpty("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertContainsIgnoreCase(expected.getMessage(), "empty");
        }
    }

    @Test
    public void nonEmpty_emptyString_throwsWithMessage() {
        String collectionName = "My Collection";
        try {
            Verify.nonEmpty(new ArrayList<String>(), collectionName);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            String message = expected.getMessage();
            assertContains(message, collectionName);
            assertContainsIgnoreCase(message, "empty");
        }
    }

    @Test
    public void nonEmpty_nonEmptyString_succeeds() {
        // Call method without message.
        Verify.nonEmpty(" ");
        Verify.nonEmpty("a");
        Verify.nonEmpty("Hello world!");

        // Call method with message.
        Verify.nonEmpty(" ", "My String");
        Verify.nonEmpty("a", "My String");
        Verify.nonEmpty("Hello world!", "My String");
    }

    @Test(expected = NullPointerException.class)
    public void noNullElements_nullCollection_throws() {
        Verify.noNullElements((List<Point>) null);
    }

    @Test
    public void noNullElements_nullCollection_throwsWithMessage() {
        String collectionName = "My Collection";
        try {
            Verify.nonEmpty((String) null, collectionName);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertContains(expected.getMessage(), collectionName);
        }
    }

    @Test
    public void noNullElements_collectionWithNullElement_throws() {
        noNullElements_collectionWithNullElement_throws(Lists.newArrayList("", "hi", null), 2);
        noNullElements_collectionWithNullElement_throws(Lists.newArrayList(null, "hi", null), 0);
        noNullElements_collectionWithNullElement_throws(Lists.newArrayList("", null, "hi"), 1);
        noNullElements_collectionWithNullElement_throws(Lists.newArrayList((String) null), 0);
        noNullElements_collectionWithNullElement_throws(Lists.newArrayList("", null, null, ""), 1);
    }

    private void noNullElements_collectionWithNullElement_throws(Collection<?> collection,
            int firstNullElementPos) {
        try {
            Verify.noNullElements(collection);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertContainsIgnoreCase(expected.getMessage(), "element " + firstNullElementPos);
        }
    }

    @Test
    public void noNullElements_collectionWithNullElement_throwsWithMessage() {
        noNullElements_collectionWithNullElement_throwsWithMessage(
                Lists.newArrayList("", "hi", null), "My Collection", 2);
        noNullElements_collectionWithNullElement_throwsWithMessage(
                Lists.newArrayList(null, "hi", null), "My Collection", 0);
        noNullElements_collectionWithNullElement_throwsWithMessage(
                Lists.newArrayList("", null, "hi"), "My Collection", 1);
        noNullElements_collectionWithNullElement_throwsWithMessage(
                Lists.newArrayList((String) null), "My Collection", 0);
        noNullElements_collectionWithNullElement_throwsWithMessage(
                Lists.newArrayList("", null, null, ""), "My Collection", 1);
    }

    private void noNullElements_collectionWithNullElement_throwsWithMessage(
            Collection<?> collection, String collectionName, int firstNullElementPos) {
        try {
            Verify.noNullElements(collection, collectionName);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            String message = expected.getMessage();
            assertContainsIgnoreCase(message, "element " + firstNullElementPos);
            assertContains(message, collectionName);
        }
    }

    @Test
    public void noNullElements_succeeds() {
        // None of these should thow.
        Verify.noNullElements(Lists.newArrayList());
        Verify.noNullElements(Lists.newArrayList(""));
        Verify.noNullElements(Lists.newArrayList("Hello"));
        Verify.noNullElements(Lists.newArrayList("Hello", "World", ""));
    }
}
