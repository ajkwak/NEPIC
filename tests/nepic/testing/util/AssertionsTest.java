package nepic.testing.util;

import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.Collection;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.google.common.collect.Lists;

public class AssertionsTest {

    @Test
    public void assertContains_string_succeeds() {
        Assertions.assertContains("Hello, world!", "");
        Assertions.assertContains("!", "");
        Assertions.assertContains("Hello, world!", "Hello");
        Assertions.assertContains("Hello, world!", "world");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContains_nullString_throws() {
        Assertions.assertContains(null, "Hi!");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContains_nullSubstring_throws() {
        Assertions.assertContains("Hi!", (String) null);
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContains_stringDoesNotContainSubstring_throws() {
        Assertions.assertContains("Hello, world!", "Goodbye");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContains_stringContainsNonCaseSensitiveSubstring_throws() {
        Assertions.assertContains("Hello, world!", "hello");
    }

    @Test
    public void assertContainsIgnoreCase_succeeds() {
        Assertions.assertContainsIgnoreCase("Hello, world!", "");
        Assertions.assertContainsIgnoreCase("!", "");
        Assertions.assertContainsIgnoreCase("Hello, world!", "Hello,");
        Assertions.assertContainsIgnoreCase("Hello, world!", "HELLO");
        Assertions.assertContainsIgnoreCase("Hello, world!", "o, wOrLd");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContainsIgnoreCase_stringDoesNotContainSubstring_throws() {
        Assertions.assertContainsIgnoreCase("Hello, world!", "Once");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContainsIgnoreCase_nullString_throws() {
        Assertions.assertContainsIgnoreCase(null, "Hi!");
    }

    @Test(expected = AssertionFailedError.class)
    public void assertContainsIgnoreCase_nullSubstring_throws() {
        Assertions.assertContainsIgnoreCase("Hello, world!", null);
    }

    @Test
    public void assertContains_collection_succeeds() {
        Assertions.assertContains("Hello", Lists.newArrayList("Hello", "world"));
        Assertions.assertContains(5, Lists.newArrayList(8, 7, 6, 5));
        Assertions.assertContains(5, Lists.newArrayList(8, 7, 5, 6, 5));
        Assertions.assertContains(true, Lists.newArrayList(false, true, false));
        Assertions.assertContains(new Point(5, 5), Lists.newArrayList(new Point(5, 5)));
    }

    public void assertContains_collectionDoesNotContainElement_throws() {
        // CASE: Null collection.
        try {
            Assertions.assertContains("Hello", (Collection<String>) null);
            fail("Expected AssertionFailedError.");
        } catch (AssertionFailedError expected) {
            // Expected.
        }

        // CASE: Empty collection.
        try {
            Assertions.assertContains(true, Lists.newArrayList());
            fail("Expected AssertionFailedError.");
        } catch (AssertionFailedError expected) {
            // Expected.
        }

        // CASE: Non-empty collection lacking desired element.
        try {
            Assertions.assertContains("Hello", Lists.newArrayList("Hi", "world!"));
            fail("Expected AssertionFailedError.");
        } catch (AssertionFailedError expected) {
            // Expected.
        }
    }
}
