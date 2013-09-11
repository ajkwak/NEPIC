package nepic.util;

import java.util.List;

public class Verify {

    /**
     * @param toCheck
     * @throws NullPointerException if toCheck is null
     */
    public static void notNull(Object toCheck) {
        if (toCheck == null) {
            throw new NullPointerException();
        }
    }

    /**
     * @param toCheck
     * @param message message to display in NullPointerException thrown if toCheck is null
     * @throws NullPointerException if toCheck is null
     */
    public static void notNull(Object toCheck, String message) {
        if (toCheck == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * @param toCheck
     * @throws IllegalArgumentException if toCheck is false
     */
    public static void argument(boolean toCheck) {
        if (!toCheck) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param toCheck
     * @param message message to display in exception if argument fails
     * @throws IllegalArgumentException if toCheck is false
     */
    public static void argument(boolean toCheck, String message) {
        if (!toCheck) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param toCheck
     * @throws IllegalStateException if toCheck is false
     */
    public static void state(boolean toCheck) {
        if (!toCheck) {
            throw new IllegalStateException();
        }
    }

    /**
     * @param toCheck
     * @param message message to display in exception if argument fails
     * @throws IllegalStateException if toCheck is false
     */
    public static void state(boolean toCheck, String message) {
        if (!toCheck) {
            throw new IllegalStateException(message);
        }
    }

    public static void nonEmpty(List<?> toCheck) {
        if (toCheck == null || toCheck.isEmpty()) {
            throw new IllegalArgumentException("The given list was null or empty.");
        }
    }

    public static void nonEmpty(List<?> toCheck, String message) {
        if (toCheck == null || toCheck.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

}
