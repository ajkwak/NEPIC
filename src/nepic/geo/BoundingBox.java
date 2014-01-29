package nepic.geo;

import java.awt.Point;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since ???
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 * 
 */
public class BoundingBox implements BoundedRegion {
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;

    public BoundingBox(int minX, int maxX, int minY, int maxY) {
        update(minX, maxX, minY, maxY);
    }

    /**
     * Constructs the {@link BoundingBox} from two points. These points must be either represent
     * top-left corner and bottom-right corner of the bounding box, or the top-right corner and the
     * bottom-left corner. The order of the two parameters of this method does not matter.
     * 
     * @param p1 one of the points defining the {@link BoundingBox}
     * @param p2 the other point defining the {@link BoundingBox}
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

    public void editBoundariesIfNeeded(int x, int y) {
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

    // // In place
    // public void cropTo(BoundingBox other) {
    // // Throw exception if bounding boxes do not overlap
    // Verify.argument(intersectsWith(other), "Unable to crop " + this + " to " + other
    // + ".  Bounding boxes do not overlap.");
    //
    // if (this.minX < other.minX) {
    // minX = other.minX;
    // }
    // if (this.maxX > other.maxX) {
    // maxX = other.maxX;
    // }
    // if (this.minY < other.minY) {
    // minY = other.minY;
    // }
    // if (this.maxY > other.maxY) {
    // maxY = other.maxY;
    // }
    // }

    public boolean intersectsWith(BoundingBox other) { // TODO: convert to BoundedRegion?? How
                                                       // restate method name?
        return other.maxX >= this.minX && other.minX <= this.maxX && other.maxY >= this.minY
                && other.minY <= this.maxY;
    }

    public BoundingBox getIntersectionWith(BoundingBox other) {
        if (!intersectsWith(other)) {
            return null;
        }

        // Otherwise, find boundaries of intersection
        int iMinX = Math.max(minX, other.minX); // iMinX --> intersectionMinX
        int iMaxX = Math.min(maxX, other.maxX);
        int iMinY = Math.max(minY, other.minY);
        int iMaxY = Math.min(maxY, other.maxY);

        return new BoundingBox(iMinX, iMaxX, iMinY, iMaxY);
    }

    @Override
    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        Verify.argument(minX <= maxX, "minX to set must be <= " + maxX);
        this.minX = minX;
    }

    @Override
    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        Verify.argument(minX <= maxX, "maxX to set must be >= " + minX);
        this.maxX = maxX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        Verify.argument(minY <= maxY, "minY to set must be <= " + maxY);
        this.minY = minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        Verify.argument(minY <= maxY, "maxY to set must be >= " + minY);
        this.maxY = maxY;
    }

    public void update(int minX, int maxX, int minY, int maxY) {
        Verify.argument(minX <= maxX && minY <= maxY, "minX (=" + minX + ") must be <= to maxX(="
                + maxX + "); minY (=" + minY + ") must be <= to maxY (=" + maxY + ")");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public Polygon asPolygon() {
        Point[] corners = new Point[] {
                new Point(minX, minY),
                new Point(maxX, minY),
                new Point(maxX, maxY),
                new Point(minX, maxY) };
        return new Polygon(corners);
    }

    public BoundingBox deepCopy() {
        return new BoundingBox(minX, maxX, minY, maxY);
    }

    public Point getMidPoint() {
        return new Point((maxX + minX) / 2, (maxY + minY) / 2);
    }

    @Override
    public String toString() {
        return "BoundingBox[ x: (" + minX + "..." + maxX + "), y (" + minY + "..." + maxY + ") ]";
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

}
