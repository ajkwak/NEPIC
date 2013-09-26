package nepic.util;

import java.util.Collection;

/**
 * A collection of static methods that verify certain conditions about their parameters, otherwise
 * throwing exceptions.
 *
 * @author AJ Parmidge
 *
 */
public class Verify {

    /**
     * This class is uninstantiable.
     */
    private Verify() {
        throw new IllegalArgumentException();
    }

    /**
     * Verifies that the given object is not {@code null}.
     *
     * @param toCheck the object to check
     * @throws NullPointerException if the given object is {@code null}
     */
    public static void notNull(Object toCheck) {
        if (toCheck == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Verifies that the given object is not {@code null}.
     *
     * @param toCheck the object to check
     * @param message the {@link Exception} message to display if {@code toCheck} is {@code null}
     * @throws NullPointerException if toCheck is {@code null}
     */
    public static void notNull(Object toCheck, String message) {
        if (toCheck == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Verifies that the given argument is {@code true}.
     *
     * @param toCheck the argument to check
     * @throws IllegalArgumentException if {@code toCheck} is {@code false}
     */
    public static void argument(boolean toCheck) {
        if (!toCheck) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Verifies that the given argument is {@code true}.
     *
     * @param toCheck the argument to check
     * @param message the {@link Exception} message to display if {@code toCheck} is {@code false}
     * @throws IllegalArgumentException if {@code toCheck} is {@code false}
     */
    public static void argument(boolean toCheck, String message) {
        if (!toCheck) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Verifies that the given state is {@code true}.
     *
     * @param toCheck the state to check
     * @throws IllegalStateException if the given state is {@code false}
     */
    public static void state(boolean toCheck) {
        if (!toCheck) {
            throw new IllegalStateException();
        }
    }

    /**
     * Verifies that the given state is {@code true}.
     *
     * @param toCheck the state to check
     * @param message the {@link Exception} message to display if the givens state is {@code false}
     * @throws IllegalStateException if the given state is {@code false}
     */
    public static void state(boolean toCheck, String message) {
        if (!toCheck) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Verifies that the given {@link Iterable} is non-empty.
     *
     * @param toCheck the iterable to check.
     * @throws NullPointerException if the given iterable is {@code null}
     * @throws IllegalArgumentException if the given iterable is empty
     */
    public static void nonEmpty(Iterable<?> toCheck) {
        Verify.nonEmpty(toCheck, "given iterable");
    }

    /**
     * Verifies that the given {@link Iterable} is non-empty.
     *
     * @param toCheck the iterable to check.
     * @param item the name of the iterable variable
     * @throws NullPointerException if the given iterable is {@code null}
     * @throws IllegalArgumentException if the given iterable is empty
     */
    public static void nonEmpty(Iterable<?> toCheck, String item) {
        String msg = "Expected " + item + " to be non-empty.  Instead got " + item;
        Verify.notNull(toCheck, msg);
        if (toCheck instanceof Collection) {
            Verify.argument(!((Collection<?>) toCheck).isEmpty(), msg);
        } else {
            Verify.argument(toCheck.iterator().hasNext(), msg);
        }
    }

    /**
     * Verifies that the given {@link String} is non-empty.
     * 
     * @param toCheck the string to check.
     * @throws NullPointerException if the given string is {@code null}
     * @throws IllegalArgumentException if the given string is empty
     */
    public static void nonEmpty(String toCheck) {
        Verify.nonEmpty(toCheck, "given String");
    }

    /**
     * Verifies that the given {@link String} is non-empty.
     * 
     * @param toCheck the string to check.
     * @message the name of the string variable
     * @throws NullPointerException if the given string is {@code null}
     * @throws IllegalArgumentException if the given string is empty
     */
    public static void nonEmpty(String toCheck, String item) {
        String msg = "Expected " + item + " to be non-empty string.";
        Verify.notNull(toCheck, msg);
        Verify.argument(!toCheck.isEmpty(), msg);
    }
}
