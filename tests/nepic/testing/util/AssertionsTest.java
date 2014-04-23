package nepic.testing.util;

import java.awt.Point;
import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AssertionsTest {

    @Test
    public void assertContains_string_succeeds() {
        Assertions.assertContains("Hello, world!", "");
        Assertions.assertContains("!", "");
        Assertions.assertContains("Hello, world!", "Hello");
        Assertions.assertContains("Hello, world!", "world");
    }

    @Test(expected = AssertionError.class)
    public void assertContains_nullString_throws() {
        Assertions.assertContains(null, "Hi!");
    }

    @Test(expected = AssertionError.class)
    public void assertContains_nullSubstring_throws() {
        Assertions.assertContains("Hi!", (String) null);
    }

    @Test(expected = AssertionError.class)
    public void assertContains_stringDoesNotContainSubstring_throws() {
        Assertions.assertContains("Hello, world!", "Goodbye");
    }

    @Test(expected = AssertionError.class)
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

    @Test(expected = AssertionError.class)
    public void assertContainsIgnoreCase_stringDoesNotContainSubstring_throws() {
        Assertions.assertContainsIgnoreCase("Hello, world!", "Once");
    }

    @Test(expected = AssertionError.class)
    public void assertContainsIgnoreCase_nullString_throws() {
        Assertions.assertContainsIgnoreCase(null, "Hi!");
    }

    @Test(expected = AssertionError.class)
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

    @Test
    public void assertContains_collectionDoesNotContainElement_throws() {
        // CASE: Null collection.
        try {
            Assertions.assertContains("Hello", (Collection<String>) null);
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Empty collection.
        try {
            Assertions.assertContains(true, Lists.newArrayList());
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Non-empty collection lacking desired element.
        try {
            Assertions.assertContains("Hello", Lists.newArrayList("Hi", "world!"));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }

    @Test
    public void assertNotContains_succeeds() {
        // CASE: Collection is null.
        Assertions.assertNotContains("Hello", (Collection<String>) null);

        // CASE: Empty collection.
        Assertions.assertNotContains(true, Lists.newArrayList());

        // CASE: Non-empty collection lacking desired element.
        Assertions.assertNotContains(1, Lists.newArrayList(0, 2, -9, 7, 5, 12));
    }

    @Test
    public void assertNotContains_collectionContainsElement_throws() {
        // CASE: Given element is only element.
        try {
            Assertions.assertNotContains("Hello", Lists.newArrayList("Hello"));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Given element is first element.
        try {
            Assertions.assertNotContains(true, Lists.newArrayList(true, false, false));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Given element is in the middle of the collection.
        try {
            Assertions.assertNotContains(5, Lists.newArrayList(9, -3, 0, 8, 5, 8, 3, 2, 0, -14));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Given element is the last element.
        try {
            Assertions.assertNotContains("Hello", Lists.newArrayList("Hi", "world!", "Hello"));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Given element appears multiple times in the collection.
        try {
            Assertions.assertNotContains(false,
                    Lists.newArrayList(true, false, false, true, false, true, false, true, true));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }

    @Test
    public void assertEqualsAnyOrder_succeeds() {
        // CASE: Both collections are null.
        Assertions.assertEqualsAnyOrder((Collection<Object>) null, (Collection<Object>) null);

        // CASE: Both collections are empty.
        Assertions.assertEqualsAnyOrder(Lists.newLinkedList(), Lists.newArrayList());

        // CASE: Collections have the same elements in the same order.
        Assertions.assertEqualsAnyOrder(Lists.newArrayList("Hi"), Lists.newArrayList("Hi"));
        Assertions.assertEqualsAnyOrder(Lists.newArrayList(0, 2, -9, 7, 5, 12),
                Lists.newArrayList(0, 2, -9, 7, 5, 12));

        // CASE: Collections have the same elements different orders.
        Assertions.assertEqualsAnyOrder(Lists.newArrayList("Hello", "world"),
                Lists.newArrayList("world", "Hello"));
        Assertions.assertEqualsAnyOrder(Lists.newArrayList(1, 2, 5, 2, 6, 7, 9, 0, 2),
                Lists.newArrayList(0, 1, 2, 2, 2, 5, 6, 7, 9));
    }

    @Test
    public void assertEqualsAnyOrder_oneCollectionNull_throws() {
        // CASE: 2nd collection is null.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList(2, 5), (Collection<Integer>) null);
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: 1st collection is null.
        try {
            Assertions.assertEqualsAnyOrder((Collection<Object>) null, Lists.newArrayList());
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }

    @Test
    public void assertEqualsAnyOrder_collectionsHaveDifferentNumElements_throws() {
        // CASE: 1st collection has more elements than 2nd collection.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList(2, 5), Lists.newArrayList(5));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: 2nd collection has more elements than 1st collection.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList("Bwah", "Mal", "Inara"),
                    Lists.newArrayList("Bwah", "Inara", "Mal", "Inara"));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }

    @Test
    public void assertEqualsAnyOrder_collectionsContainDifferentElements_throws() {
        // CASE: Collections are distinct.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList(1, 2, 3, 4),
                    Lists.newArrayList(5, 6, 7, 8));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: Collections contain only some of the same elements.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList(1, 2, 3, 4),
                    Lists.newArrayList(1, 2, 1, 3));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: The collections contain the same elements, but different numbers of repetitions of
        // each element.
        try {
            Assertions.assertEqualsAnyOrder(Lists.newArrayList(1, 2, 2, 3, 4),
                    Lists.newArrayList(1, 1, 2, 3, 4));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }

    @Test
    public void assertDistinct_succeeds() {
        // CASE: Both collections are empty.
        Assertions.assertDistinct(Lists.newArrayList(), Sets.newHashSet());

        // CASE: One of the collections is empty.
        Assertions.assertDistinct(Lists.newArrayList(), Sets.newHashSet());

        // CASE: Both collections are non-empty, but do not contain the same elements.
        Assertions.assertDistinct(Lists.newArrayList(1, 1, 4, 5), Sets.newHashSet(2, 2, 3));
        Assertions.assertDistinct(Lists.newArrayList("Hello", "Hi"),
                Lists.newArrayList("HelloHi", "world"));
        Assertions.assertDistinct(Lists.newArrayList(true), Sets.newHashSet(false));
    }

    @Test
    public void assertDistinct_throws() {
        // CASE: The collections share an element.
        try {
            Assertions.assertDistinct(Lists.newArrayList(1, 2, 2, 3, 4),
                    Lists.newArrayList(6, 0, 3, 5, -3));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: The collections share multiple elements.
        try {
            Assertions.assertDistinct(Lists.newArrayList(9, 1, 2, 5, 2, 5, 7, 9),
                    Lists.newArrayList(0, 1, 1, 2, 3, 4, 6));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }

        // CASE: The collections are the same.
        try {
            Assertions.assertDistinct(Lists.newArrayList(5, 4, 3, 2, 1),
                    Lists.newArrayList(5, 4, 3, 2, 1));
            throw new IllegalStateException("Expected AssertionError.");
        } catch (AssertionError expected) {
            // Expected.
        }
    }
}
