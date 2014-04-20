package nepic.geo;

import java.awt.Point;

import nepic.util.Verify;

/**
 * Class representing a rectangular region with defined boundaries. The sides of the rectangular
 * {@link BoundingBox} are parallel with the x and y axes in Cartesian space.
 *
 * @author AJ Parmidge
 */
public class BoundingBox implements BoundedRegion {
    private int minX, maxX, minY, maxY;

    /**
     * Creates a {@link BoundingBox} with the given boundaries.
     *
     * @param minX the minimum value on the x-axis included in the bounds of the bounding box
     * @param maxX the maximum value on the x-axis included in the bounds of the bounding box
     * @param minY the minimum value on the y-axis included in the bounds of the bounding box
     * @param maxY the maximum value on the y-axis included in the bounds of the bounding box
     */
    public BoundingBox(int minX, int maxX, int minY, int maxY) {
        resetBounds(minX, maxX, minY, maxY);
    }

    /**
     * Constructs the {@link BoundingBox} from two points. These points must be either represent
     * top-left corner and bottom-right corner of the bounding box, or the top-right corner and the
     * bottom-left corner. The order of the two parameters does not matter.
     *
     * @param p1 one of the points defining the bounding box
     * @param p2 the other point defining the bounding box
     */
    public BoundingBox(Point p1, Point p2) {
        // Determine minX, maxX
        int x1 = p1.x;
        int x2 = p2.x;
        if (x1 < x2) {
            minX = x1;
            maxX = x2;
        } else {
            minX = x2;
            maxX = x1;
        }

        // Determine minY, maxY
        x1 = p1.y;
        x2 = p2.y;
        if (x1 < x2) {
            minY = x1;
            maxY = x2;
        } else {
            minY = x2;
            maxY = x1;
        }
    }

    /**
     * Updates the bounds of this {@link BoundingBox} if needed such that the given point is
     * included in the {@link BoundingBox}.
     *
     * @param x the x-coordinate of the point to include in the bounding box
     * @param y the y-coordinate of the point to include in the bounding box
     */
    public void update(int x, int y) {
        if (x < minX) {
            minX = x;
        }
        if (x > maxX) {
            maxX = x;
        }

        if (y < minY) {
            minY = y;
        }
        if (y > maxY) {
            maxY = y;
        }
    }

    /**
     * Determine if this bounding box intersects with the given bounding box.
     *
     * @param other the bounding box with which to check for overlap
     * @return {@code true} if this bounding box overlaps with the given bounding box; otherwise
     *         {@code false}
     */
    public boolean intersectsWith(BoundingBox other) {
        return other.maxX >= this.minX && other.minX <= this.maxX && other.maxY >= this.minY
                && other.minY <= this.maxY;
    }

    /**
     * Gets the intersection between this bounding box and the given bounding box.
     *
     * @param other the bounnding box with which to find an intersection
     * @return the intersection (overlapped region) of the two bounding boxes, if one exists;
     *         otherwise {@code null}
     */
    public BoundingBox getIntersectionWith(BoundingBox other) {
        if (!intersectsWith(other)) {
            return null;
        }

        // Find the boundaries of the intersection.
        return new BoundingBox(Math.max(minX, other.minX),
                Math.min(maxX, other.maxX),
                Math.max(minY, other.minY),
                Math.min(maxY, other.maxY));
    }

    @Override
    public int getMinX() {
        return minX;
    }

    /**
     * Sets the minimum x-value covered by this bounding box to the given value.
     *
     * @param minX the minimum x-value to set (cannot be larger than the bounding box's current
     *        maximum x-value)
     */
    public void setMinX(int minX) {
        Verify.argument(minX <= maxX, "minX to set must be <= " + maxX);
        this.minX = minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    /**
     * Sets the maximum x-value covered by this bounding box to the given value.
     *
     * @param maxX the maximum x-value to set (cannot be smaller than the bounding box's current
     *        minimum x-value)
     */
    public void setMaxX(int maxX) {
        Verify.argument(minX <= maxX, "maxX to set must be >= " + minX);
        this.maxX = maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    /**
     * Sets the minimum y-value covered by this bounding box to the given value.
     *
     * @param minY the minimum y-value to set (cannot be larger than the bounding box's current
     *        maximum y-value)
     */
    public void setMinY(int minY) {
        Verify.argument(minY <= maxY, "minY to set must be <= " + maxY);
        this.minY = minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    /**
     * Sets the maximum y-value covered by this bounding box to the given value.
     *
     * @param maxY the maximum y-value to set (cannot be smaller than the bounding box's current
     *        minimum y-value)
     */
    public void setMaxY(int maxY) {
        Verify.argument(minY <= maxY, "maxY to set must be >= " + minY);
        this.maxY = maxY;
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        return minX <= region.getMinX() && maxX >= region.getMaxX() && minY <= region.getMinY()
                && maxY >= region.getMaxY();
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * Resets the bounds of this bounding box to the given values.
     *
     * @param minX the minimum x-value to set
     * @param maxX the maximum x-value to set
     * @param minY the minimum y-value to set
     * @param maxY the maximum y-value to set
     * @throws IllegalArgumentException if {@code minX > maxX} or {@code minY > maxY}
     */
    public void resetBounds(int minX, int maxX, int minY, int maxY) {
        Verify.argument(minX <= maxX && minY <= maxY, "minX (=" + minX + ") must be <= to maxX(="
                + maxX + "); minY (=" + minY + ") must be <= to maxY (=" + maxY + ")");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Converts this {@link BoundingBox} to a {@link Polygon}
     *
     * @return the polygon representation of this bounding box
     */
    public Polygon asPolygon() {
        Point[] corners = new Point[] {
                new Point(minX, minY),
                new Point(maxX, minY),
                new Point(maxX, maxY),
                new Point(minX, maxY) };
        return new Polygon(corners);
    }

    /**
     * Creates a precise duplicate of this {@link BoundingBox}
     *
     * @return the duplicate of this bounding box
     */
    public BoundingBox deepCopy() {
        return new BoundingBox(minX, maxX, minY, maxY);
    }

    /**
     * Gets the point at the center of this {@code BoundingBox}.
     *
     * @return the point that lies at the center of this bounding box
     */
    public Point getMidPoint() {
        return new Point((maxX + minX) / 2, (maxY + minY) / 2);
    }

    @Override
    public String toString() {
        return "BoundingBox[ x: (" + minX + "..." + maxX + "), y (" + minY + "..." + maxY + ") ]";
    }
}
