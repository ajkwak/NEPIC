package nepic.util;

import java.awt.Point;

/**
 * Represents a single pixel with integer coordinates {@code (x, y)} and RGB-formatted color.
 *
 * @author AJ Parmidge
 */
public class Pixel extends Point implements Comparable<Pixel> {
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
    public Pixel(int x, int y, int theRelLum) {
        super(x, y);
        this.color = theRelLum;
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
    public int compareTo(Pixel compareAgainst) {
        int compareVal = compareAgainst.color - this.color;
        if (compareVal == 0)
            compareVal = compareAgainst.x - this.color;
        if (compareVal == 0)
            compareVal = compareAgainst.y - this.y;
        return compareVal;
    }

    /**
     * Determines whether this {@code Pixel} is flush with (i.e. touching on one side; not just
     * touching on a corner} the given {@link Point}.
     * 
     * @param p the point to check
     * @return {@code true} if this {@link Pixel} is touching the given {@link Point} direcly on any
     *         of its sides (not merely touching the given point on any of its corners); otherwise
     *         {@code false}
     */
    public boolean flushWith(Point p) {
        return areFlush(this, p);
    }

    /**
     * Converts the given pixel intensity (PI) to an RGB-formatted color. If the pixel intensity is
     * outside the range {@code 0-255}, it will be converted to this range before being converted
     * into a RGB color.
     *
     * @param pi the pixel intensity to convert
     * @return the RGB-formatted color corresponding to the given pixel intensity
     */
    public static int piToRgb(int pi) {
        pi = pi > 255 ? 255 : pi < 0 ? 0 : pi;
        int rgbVal = 0;// sets alpha of RGB
        rgbVal = (rgbVal << 8) | pi;// sets red of rgb
        rgbVal = (rgbVal << 8) | pi;// sets green of rgb
        rgbVal = (rgbVal << 8) | pi;// sets blue of rgb
        return rgbVal;
    }

    /**
     * Converts the given grayscale RGB color to the corresponding pixel intensity (PI).
     * 
     * <p>
     * For example, if the RGB color {@code 0xffffff} (i.e. {@code R = 255}, {@code G = 255}, and
     * {@code B = 255}) is passed given, the pixel intensity {@code 255} (which corresponds to any
     * single component of this grayscale RGB value) will be returned.
     * 
     * @param rgb the grayscale RGB color to convert
     * @return the relative pixel intensity (from 0-255) of the grayscale RGB color
     * @throws IllegalArgumentException if the given color is not grayscale
     */
    public static int rgbToPi(int rgb) {
        Verify.argument(isGrayscale(rgb), "Cannot convert non-grayscale rgb " + rgb
                + " into pixel intensity.");
        return 255 & rgb;
    }

    /**
     * Determine whether this {@code Pixel} touches (on any side, or on any corner) the given
     * {@link Point}.
     *
     * @param p the point to check
     * @return {@code} true if this {@link} is touching the given {@code Point} on any of its sides
     *         or corners; otherwise {@code false}
     */
    public boolean touches(Point p) {
        return areTouching(this, p);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + "): relLum = " + color;
    }

    /**
     * Determines if the given points (treated as square pixels in a grid) share at least one side.
     * 
     * @param p1 the first point to check
     * @param p2 the second point to check
     * @return {@code true} if the two points (treated as square pixels in a grid) share at least
     *         one side; otherwise {@code false}
     */
    public static boolean areFlush(Point p1, Point p2) {
        int diffX = p1.x - p2.x;
        diffX = diffX * diffX;// Makes diffX always positive; will not affect it if one or zero
        int diffY = p1.y - p2.y;
        diffY = diffY * diffY;// Makes diffY always positive; will not affect it if one or zero
        return (diffY == 0 && diffX == 1) || (diffX == 0 && diffY == 1);
    }
    /**
     * Determines if the given points (treated as square pixels in a grid) share at least one side
     * or corner.
     *
     * @param p1 the first point to check
     * @param p2 the second point to check
     * @return {@code true} if the two points (treated as square pixels in a grid) share at least
     *         one side or corner; otherwise {@code false}
     */
    public static boolean areTouching(Point p1, Point p2) {
        int diffX = p1.x - p2.x;
        diffX = diffX * diffX;// Makes diffX always positive; will not affect it if one or zero
        int diffY = p1.y - p2.y;
        diffY = diffY * diffY;// Makes diffY always positive; will not affect it if one or zero
        return diffY <= 1 && diffX <= 1;
    }

    /**
     * Determines if the given RGB-formatted color is grayscale.
     * 
     * @param rgb the RGB-formatted color to examine
     * @return {@code true} if the given {@code rgb} value is grayscale; otherwise {@code false}
     */
    public static boolean isGrayscale(int rgb) {
        int blue = 255 & rgb;
        rgb = rgb >> 8;
        int green = 255 & rgb;
        rgb = rgb >> 8;
        int red = 255 & rgb;
        if (red != blue || red != green) {// if pixel is NOT grayscale!!
            return false;
        }
        return true;
    }

}