package nepic.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit tests for {@link Range}.
 *
 * @author AJ Parmidge
 */
public class RangeTest {

    @Test
    public void ctor() {
        // CASE: bound1 < bound2
        Range range = new Range(5, 10);
        assertEquals(5, range.min);
        assertEquals(10, range.max);

        // CASE: bound1 == bound2
        range = new Range(10, 10);
        assertEquals(10, range.min);
        assertEquals(10, range.max);

        // CASE: bound1 > bound2
        range = new Range(10, 5);
        assertEquals(5, range.min);
        assertEquals(10, range.max);
    }

    @Test
    public void length() {
        assertEquals(6, new Range(5, 10).length());
        assertEquals(1, new Range(10, 10).length());
        assertEquals(6, new Range(10, 5).length());
    }
}
