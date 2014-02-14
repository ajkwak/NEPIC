package nepic.util;

/**
 * Class representing a one-dimensional range of values, from a lower bound to an upper bound.
 *
 * @author AJ Parmidge
 */
public class Range {
    /**
     * The lower bound of this {@link Range}.
     */
    public final int min;
    /**
     * The upper bound of this {@link Range}.
     */
    public final int max;

    /**
     * Creates a {@link Range} with the given bounds.
     *
     * @param bound1 the first (lower or upper) bound of the {@link Range} to create
     * @param bound2 the second (upper or lower) bound of the {@link Range} to create
     */
    public Range(int bound1, int bound2) {
        if (bound1 <= bound2) {
            this.min = bound1;
            this.max = bound2;
        } else {
            this.min = bound2;
            this.max = bound1;
        }
    }

    public int length() {
        return max - min + 1;
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }
}
