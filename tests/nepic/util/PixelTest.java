package nepic.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit tests for {@link Pixel}.
 *
 * @author AJ Parmidge
 */
public class PixelTest {

    @Test
    public void ctor() {
        // CASE: Three-parameter constructor.
        int x = -34;
        int y = 41;
        int color = 0x4459af;
        Pixel pixel = new Pixel(x, y, color);
        assertEquals(x, pixel.x);
        assertEquals(y, pixel.y);
        assertEquals(color, pixel.color);

        // CASE: Two-parameter constructor.
        pixel = new Pixel(x, y);
        assertEquals(x, pixel.x);
        assertEquals(y, pixel.y);
    }
}
