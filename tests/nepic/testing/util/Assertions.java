package nepic.testing.util;

import junit.framework.AssertionFailedError;

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
            throw new AssertionFailedError("Expected \"" + string + "\" to contain substring \""
                    + substring + "\"");
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
            throw new AssertionFailedError("Expected \"" + string + "\" to contain substring \""
                    + substring + "\"");
        }
    }
}
