package nepic.testing.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This class contains a collection of static assert methods with helpful fail errors. This class
 * acts as an adjunct to the {code org.junit.Assert} class.
 *
 * @author AJ Parmidge
 */
public class Assertions {

    /**
     * Asserts that the given string contains the given substring (case-sensitive).
     *
     * @param string the string to check
     * @param substring the substring to check
     */
    public static void assertContains(String string, String substring) {
        if (string == null || substring == null || !string.contains(substring)) {
            fail("Expected \"" + string + "\" to contain substring \"" + substring + "\"");
        }
    }

    /**
     * Asserts that the given string contains the given substring (NOT case-sensitive).
     *
     * @param string the string to check
     * @param substring the substring to check
     */
    public static void assertContainsIgnoreCase(String string, String substring) {
        if (string == null || substring == null
                || !string.toLowerCase().contains(substring.toLowerCase())) {
            fail("Expected \"" + string + "\" to contain substring \"" + substring + "\"");
        }
    }

    /**
     * Asserts that the given {@link Collection} contains the given element.
     *
     * @param element the element to check
     * @param collection the collection to check
     */
    public static <E> void assertContains(E element, Collection<E> collection) {
        if (collection == null || !collection.contains(element)) {
            fail("Expected to find <" + element + "> in <" + collection + ">");
        }
    }

    /**
     * Asserts that the given {@link Collection} does NOT contain the given element.
     *
     * @param element the element to check
     * @param collection the collection to check
     */
    public static <E> void assertNotContains(E element, Collection<E> collection) {
        if (collection != null && collection.contains(element)) {
            fail("Expected to NOT find <" + element + "> in <" + collection + ">");
        }
    }

    /**
     * Asserts that the two {@link Collection}s contain exactly the same elements, but those
     * elements may be ordered differently.
     *
     * @param c1 the first collection to check
     * @param c2 the second collection to check
     */
    public static <E> void assertEqualsAnyOrder(Collection<E> c1, Collection<E> c2) {
        if (c1 == null && c2 == null) {
            return; // They are equal.
        } else if (c1 == null ^ c2 == null) {
            fail("One of the given collections is null.");
        }
        assertEquals("Given collections have different lengths!  c1.size = " + c1.size()
                + ", c2.size = " + c2.size(), c1.size(), c2.size());

        // Make an element hashmap of c1. This maps <element> --> <number of instances in c1>
        HashMap<E, Integer> c1Map = Maps.newHashMap();
        for (E element : c1) {
            Integer numInstances = c1Map.get(element);
            if (numInstances == null) {
                numInstances = 0;
            }
            c1Map.put(element, numInstances + 1);
        }

        // Make an element hashmap of c2.
        HashMap<E, Integer> c2Map = Maps.newHashMap();
        for (E element : c2) {
            Integer numInstances = c2Map.get(element);
            if (numInstances == null) {
                numInstances = 0;
            }
            c2Map.put(element, numInstances + 1);
        }

        // Get the union of the keySets of the two HashMaps.
        Set<E> keySetUnion = Sets.newHashSet(c1Map.keySet());
        keySetUnion.addAll(c2Map.keySet()); // This should return false if c1 and c2 are equal.

        // Verify that the hashmaps are the same.
        for (E key : keySetUnion) {
            Integer numInstancesC1 = c1Map.get(key);
            if (numInstancesC1 == null) {
                numInstancesC1 = 0;
            }
            Integer numInstancesC2 = c2Map.get(key);
            if (numInstancesC2 == null) {
                numInstancesC2 = 0;
            }
            if (numInstancesC1 != numInstancesC2) {
                if (numInstancesC1 > numInstancesC2) {
                    fail("1st collection contains more instances of <" + key
                            + "> than the 2nd collection");
                } else {
                    fail("1st collection contains fewer instances of <" + key
                            + "> than the 2nd collection");
                }
            }
        }
    }

    /**
     * Asserts that the two given collections contain no equivalent elements.
     *
     * @param c1 the first collection to check
     * @param c2 the second collection to check
     */
    public static <E> void assertDistinct(Collection<E> c1, Collection<E> c2) {
        // Make an element hash set of c1.
        HashSet<E> c1HashSet = Sets.newHashSet();
        for (E element : c1) {
            c1HashSet.add(element);
        }

        // Check the elements in c2 against this has set.
        for (E element : c2) {
            if (c1HashSet.contains(element)) {
                fail("Both collections contain the element <" + element + ">");
            }
        }
    }
}
