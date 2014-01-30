package nepic.testing.util;

import junit.framework.AssertionFailedError;

import org.junit.Test;

public class AssertionsTest {

    @Test
    public void assertContains_succeeds() {
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
        Assertions.assertContains("Hi!", null);
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

}
