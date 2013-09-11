package nepic.util;

import java.awt.Point;

/**
 * Represents a single Pixel of information in the image to analyze (allows relative luminosity of
 * specific pixel to be stored for picture analysis)
 * 
 * @author AJ
 */
public class Pixel extends Point implements Comparable<Pixel> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The relative luminosity of the pixel (0-255). Since all pictures analyzed are grayscale,
     * corresponds to any of the red, green, or blue color components of the pixel color.
     */
    public int relLum;// The relative luminosity of the pixel (black = 0, white = 255)

    /**
     * Constructs a Pixel object based on the Pixel's location and relative luminosity value.
     * 
     * @param theX The horizontal component of the Pixel's location.
     * @param theY The vertical component of the Pixel's location.
     * @param theLum The relative luminosity of the Pixel to be constructed.
     */
    public Pixel(int theX, int theY, int theRelLum) {
        super(theX, theY);
        relLum = theRelLum;
    }// Pixel constructor

    /**
     * Constructs a Pixel object based only on the Pixel's location (used if the relative luminosity
     * of the Pixel is not known).
     * 
     * @param theX The horizontal component of the Pixel's location.
     * @param theY The vertical component of the Pixel's location.
     */
    public Pixel(int theX, int theY) {
        super(theX, theY);
        relLum = 0;
    }// Pixel constructor

    /**
     * Determines if the location (within an image) of the invoking and parameter Pixels are the
     * same
     * 
     * @param theOther the Pixel whose location is being compared to that of the invoking object
     * @return true if the location of the invoking object and parameter are the same; false if
     *         either component of the location of the invoking object and parameter differ
     */
    @Override
    public boolean equals(Object theOther) {
        if (!(theOther instanceof Point)) {
            return false;
        }
        Point compareAgainst = (Point) theOther;
        return x == compareAgainst.x && y == compareAgainst.y;
    }// equals

    /**
     * Makes a String representation of the invoking Pixel, showing its location and relative
     * luminosity
     * 
     * @return String representation of the invoking Pixel
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + "): relLum = " + relLum;
    }// toString

    public static byte rgbToRelLum(int theRGB) {// TODO: check with mom that am doing correctly
        if (!isGrayscale(theRGB))// if pixel is NOT grayscale!!
            throw new IllegalArgumentException("Cannot convert the RGB val " + theRGB
                    + "into a pixel intensity because it is NOT grayscale.");
        return (byte) (255 & theRGB);
    }// rgbToRelLum

    @Override
    public int compareTo(Pixel compareAgainst) {
        int compareVal = compareAgainst.relLum - this.relLum;
        if (compareVal == 0)
            compareVal = compareAgainst.x - this.relLum;
        if (compareVal == 0)
            compareVal = compareAgainst.y - this.y;
        return compareVal;
    }// compareTo

    public static int relLumToRGB(int theRelLum) {
        if (theRelLum > 255)
            theRelLum = 255;// so don't cut off
        int rgbVal = 0;// sets alpha of RGB
        rgbVal = (rgbVal << 8) | theRelLum;// sets red of rgb
        rgbVal = (rgbVal << 8) | theRelLum;// sets green of rgb
        rgbVal = (rgbVal << 8) | theRelLum;// sets blue of rgb
        return rgbVal;
    }// relLumToRGB

    public static boolean isGrayscale(int rgb) {
        int blue = 255 & rgb;// turns RGB value into relLum value
        rgb = rgb >> 8;
        int green = 255 & rgb;
        rgb = rgb >> 8;
        int red = 255 & rgb;
        if (red != blue || red != green) {// if pixel is NOT grayscale!!
            return false;
        }
        return true;
    }

    /**
     * 
     * @param p
     * @return true if pixels are flush or if pixels are kitty-corner to each other (touch on their
     *         corners)
     */
    public boolean touch(Point p) {
        return touching(this, p);
    }// touch

    /**
     * 
     * @param p
     * @return true if pixels touch each other along edge; otherwise false
     */
    public boolean flushWith(Point p) {
        return flush(this, p);
    }// flush

    public static boolean touching(Point p1, Point p2) {
        int diffX = p1.x - p2.x;
        diffX = diffX * diffX;// makes diffX always positive; will not affect it if one or zero
        int diffY = p1.y - p2.y;
        diffY = diffY * diffY;// makes diffY always positive; will not affect it if one or zero
        return diffY <= 1 && diffX <= 1;
    }

    public static boolean flush(Point p1, Point p2) {
        int diffX = p1.x - p2.x;
        diffX = diffX * diffX;// makes diffX always positive; will not affect it if one or zero
        int diffY = p1.y - p2.y;
        diffY = diffY * diffY;// makes diffY always positive; will not affect it if one or zero
        return (diffY == 0 && diffX == 1) || (diffX == 0 && diffY == 1);
    }

}// Pixel class