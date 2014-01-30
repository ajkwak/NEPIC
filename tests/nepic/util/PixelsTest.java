package nepic.util;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Test;

/**
 * JUnit tests for {@link Pixels}.
 *
 * @author AJ Parmidge
 */
public class PixelsTest {

    @Test
    public void areFlush() {
        // A point is NOT flush with itself.
        assertFalse(Pixels.areFlush(new Point(0, 0), new Point(0, 0)));

        // A pixel is flush with another pixel with which it shares an edge.
        assertTrue(Pixels.areFlush(new Point(5, -6), new Point(5, -7)));
        assertTrue(Pixels.areFlush(new Point(5, -6), new Point(5, -5)));
        assertTrue(Pixels.areFlush(new Point(5, -6), new Point(4, -6)));
        assertTrue(Pixels.areFlush(new Point(5, -6), new Point(6, -6)));

        // A pixel is NOT flush with another pixel with which it shares only a corner.
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(4, -5)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(6, -5)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(4, -7)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(6, -7)));

        // A point is NOT flush with pixels it does not touch.
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(4, 14)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(0, 0)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(6, 9)));
        assertFalse(Pixels.areFlush(new Point(5, -6), new Point(-6, -16)));
    }

    @Test
    public void areTouching() {
        // A point is touching with itself.
        assertTrue(Pixels.areTouching(new Point(0, 0), new Point(0, 0)));

        // A pixel is touching another pixel with which it shares an edge.
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(5, -7)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(5, -5)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(4, -6)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(6, -6)));

        // A pixel is touching another pixel with which it shares only a corner.
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(4, -5)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(6, -5)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(4, -7)));
        assertTrue(Pixels.areTouching(new Point(5, -6), new Point(6, -7)));

        // A point is NOT touching another pixel with which it does not share an edge or corner.
        assertFalse(Pixels.areTouching(new Point(5, -6), new Point(4, 14)));
        assertFalse(Pixels.areTouching(new Point(5, -6), new Point(0, 0)));
        assertFalse(Pixels.areTouching(new Point(5, -6), new Point(6, 9)));
        assertFalse(Pixels.areTouching(new Point(5, -6), new Point(-6, -16)));
    }
}
