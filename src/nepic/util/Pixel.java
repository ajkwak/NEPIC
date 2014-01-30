package nepic.util;

import java.awt.Point;

/**
 * Represents a single pixel with integer coordinates {@code (x, y)} and RGB-formatted color.
 *
 * @author AJ Parmidge
 */
public class Pixel extends Point {
    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -8623199990657154329L;
    /**
     * The RGB color of the {@link Pixel}.
     */
    public int color;

    /**
     * Constructs a {@link Pixel} with the given coordinate location and color.
     *
     * @param x the x-coordinate of the newly constructed {@link Pixel}
     * @param y the y-coordinate of the newly constructed {@link Pixel}
     * @param theLum the RGB color of the newly constructed {@link Pixel}
     */
    public Pixel(int x, int y, int color) {
        super(x, y);
        this.color = color;
    }

    /**
     * Constructs a {@link Pixel} with the given coordinate location.
     *
     * @param x the x-coordinate of the newly constructed {@link Pixel}
     * @param y the y-coordinate of the newly constructed {@link Pixel}
     */
    public Pixel(int x, int y) {
        super(x, y);
        color = 0;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + "): color = 0x" + Integer.toHexString(color);
    }
}