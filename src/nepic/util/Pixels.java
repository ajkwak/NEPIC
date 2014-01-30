package nepic.util;

import java.awt.Point;

// TODO: this is to the Pixel class what Lists is to the List class!
/**
 * Class containing useful utility methods for {@link Pixel} objects.
 *
 * @author AJ Parmidge
 */
public class Pixels {

    /**
     * This class is uninstantiable.
     */
    private Pixels() {
        throw new UnsupportedOperationException();
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

}
