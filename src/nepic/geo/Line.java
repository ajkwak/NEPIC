package nepic.geo;

import java.awt.Point;
import java.awt.geom.Point2D;

import nepic.util.Verify;

/**
 * NOTE: all methods treating this treating a {@link Line} object as a line segment contain the word
 * 'segment' in their declarations.
 *
 * @author AJ Parmidge
 * @since ????
 * @version AutoCBFnder_Alpha_v1-1-2013-03-14
 *
 */
public class Line {
    /**
     * The x-coordinate of the starting point of the line.
     */
    final int startX;
    /**
     * The x-coordinate of the ending point of the line.
     */
    final int endX;
    /**
     * The y-coordinate of the starting point of the line.
     */
    final int startY;
    /**
     * The y-coordinate of the ending point of the line.
     */
    final int endY;
    /**
     * True if line is more vertical than horizontal; otherwise false.
     */
    final boolean moreVertical;
    /**
     * Indicates the direction of movement from <code> startX </code> to <code> endX </code>.
     * Evaluates to +1 if <code> endX </code> >= <code> startX </code>. Otherwise evaluates to -1.
     */
    final int xIncrement;
    /**
     * Indicates the direction of movement from <code> startY </code> to <code> endY </code>.
     * Evaluates to +1 if <code> endY </code> >= <code> startY </code>. Otherwise evaluates to -1.
     */
    final int yIncrement;

    public Line(Point p1, Point p2) {
        Verify.notNull(p1, "Starting point of a line cannot be null");
        Verify.notNull(p2, "Ending point of a line cannot be null");
        startX = p1.x;
        endX = p2.x;
        startY = p1.y;
        endY = p2.y;

        int changeX = endX - startX;
        xIncrement = changeX < 0 ? -1 : 1;
        int changeY = endY - startY;
        yIncrement = changeY < 0 ? -1 : 1;
        moreVertical = xIncrement * changeX < yIncrement * changeY;
    }

    /**
     *
     * NOTE: remember that positive on the y-axis is going DOWN the image
     *
     * @param pt a point that the line passes through
     * @param theta angle from the positive x axis (radians), from -pi/2 to pi/2
     */
    public Line(Point origin, double theta) {
        final int length = 500;
        int c = length / 2;

        double adjRat = Math.cos(theta);
        double oppRat = Math.sin(theta);

        int x = (int) (c * adjRat + (adjRat < 0 ? -0.5 : 0.5));// a is the adjacent side (x)
        int y = (int) (c * oppRat + (oppRat < 0 ? -0.5 : 0.5));// b is the opposite side (y)

        startX = origin.x - x;
        endX = origin.x + x;
        startY = origin.y - y;
        endY = origin.y + y;

        int changeX = endX - startX;
        xIncrement = changeX < 0 ? -1 : 1;
        int changeY = endY - startY;
        yIncrement = changeY < 0 ? -1 : 1;
        moreVertical = xIncrement * changeX < yIncrement * changeY;
    }

    /**
     * Determines whether this line is vertical.
     *
     * @return true if this line is vertical (has a slope of infinity); otherwise false.
     */
    public boolean isVertical() {
        return startX == endX;
    }

    /**
     * Determines whether this line is horizontal.
     *
     * @return true if this line is horizontal (has a slope of zero); otherwise false;
     */
    public boolean isHorizontal() {
        return startY == endY;
    }

    /**
     * Determines if this line is parallel to (has the same slope as) the parameter line.
     *
     * @param other line to which to compare the slope of this line.
     * @return true if the two lines are parallel; otherwise false.
     */
    public boolean isParallelTo(Line other) {
        Verify.notNull(other, "Cannot be parallel to a null line.");

        // Special case: If either line is vertical, both must be for them to be parallel.
        boolean thisLineVertical = this.isVertical();
        boolean otherLineVertical = other.isVertical();
        if (thisLineVertical || otherLineVertical) {
            return thisLineVertical && otherLineVertical;
        }

        // If neither of the lines are vertical, determine if parallel using slope.
        int changeX = endX - startX;
        int changeY = endY - startY;
        int oChangeX = other.endX - other.startX;
        int oChangeY = other.endY - other.startY;

        // Determine if the slopes are the same (without the error of floating point calculations).
        return ((changeX / changeY) == (oChangeX / oChangeY))
                && (Math.abs(changeX % changeY) == Math.abs(oChangeX % oChangeY));
    }

    /**
     * Determines if this line is equivalent to the parameter line.
     *
     * @param other the line to which to compare this line.
     * @return true if this {@link Line} and <code> other </code> are equivalent lines (i.e. if they
     *         are parallel and share a point); otherwise returns false.
     */
    public boolean isSameLineAs(Line other) {
        // To be the same line, the two lines must be parallel and share a point.
        return isParallelTo(other) && startY == Math.round(other.getY(startX));
    }

    /**
     * Finds the intersection between this line and the parameter line.
     *
     * @param other the line with which to find an intersection.
     * @return null if this {@link Line} and <code> other </code> are parallel. Otherwise returns
     *         the single intersection between the two lines.
     */
    // Treats both this and other as continuous lines
    public Point2D getLineIntersection(Line other) {
        if (this.isParallelTo(other)) {
            // Then either there is no intersection or there are infinitely many intersections.
            return null;
        }
        // Otherwise, lines intersect at exactly one point

        // This line: y = (m1)x + b1
        double m1 = ((double) (endY - startY)) / (endX - startX);
        System.out.println("m1 = " + m1);
        double b1 = startY - m1 * startX;
        System.out.println("b1 = " + b1);

        // Other line: y = (m2)x + b2
        double m2 = ((double) (other.endY - other.startY)) / (other.endX - other.startX);
        System.out.println("m2 = " + m2);
        double b2 = other.startY - m2 * other.startX;
        System.out.println("b2 = " + b2);

        double intersectX = (b2 - b1) / (m1 - m2);
        double intersectY = m1 * intersectX + b1;

        return new Point2D.Double(intersectX, intersectY);
    }

    public LineSegment boundTo(BoundedRegion region) {
        int minX = region.getMinX();
        int minY = region.getMinY();
        int maxX = region.getMaxX();
        int maxY = region.getMaxY();

        // Special case: horizontal line
        if (isHorizontal()) {
            if (startY >= minY && startY <= maxY) {
                return new LineSegment(new Point(minX, startY), new Point(maxX, startY));
            } else {
                return null;
            }
        }

        // Special case: vertical line
        if (isVertical()) {
            if (startX >= minX && startX <= maxX) {
                return new LineSegment(new Point(startX, minY), new Point(startX, maxY));
            } else {
                return null;
            }
        }

        Point startPt = null;

        int yAtMinX = (int) Math.round(getY(minX));
        int yAtMaxX = (int) Math.round(getY(maxX));
        int xAtMinY = (int) Math.round(getX(minY));
        int xAtMaxY = (int) Math.round(getX(maxY));

        if (yAtMinX >= minY && yAtMinX <= maxY) {
            startPt = new Point(minX, yAtMinX);
        }
        if (yAtMaxX >= minY && yAtMaxX <= maxY) {
            Point edgePt = new Point(maxX, yAtMaxX);
            if (startPt == null) {
                startPt = edgePt;
            } else {
                if (!edgePt.equals(startPt)) {
                    return new LineSegment(startPt, edgePt);
                }
            }
        }
        if (xAtMinY >= minX && xAtMinY <= maxX) {
            Point edgePt = new Point(xAtMinY, minY);
            if (startPt == null) {
                startPt = edgePt;
            } else {
                if (!edgePt.equals(startPt)) {
                    return new LineSegment(startPt, edgePt);
                }
            }
        }
        if (xAtMaxY >= minX && xAtMaxY <= maxX) {
            Point edgePt = new Point(xAtMaxY, maxY);
            if (startPt == null) {
                startPt = edgePt;
            } else {
                if (!edgePt.equals(startPt)) {
                    return new LineSegment(startPt, edgePt);
                }
            }
        }
        return null;
    }

    /**
     * Finds the intersection between this line and the parameter line segment.
     *
     * @param lineSegment the line segment with which to find an intersection.
     * @return null if this line does not cross the parameter line segment or if this line is
     *         equivalent to the line the segment would be if it were not being treated as a
     *         segment; otherwise returns the single intersection between this line and the
     *         parameter line segment.
     *
     */
    // Treats this as a line, other as a line segment
    public Point2D getIntersectionWithSegment(LineSegment lineSegment) {
        Point2D intersection = getLineIntersection(lineSegment);
        if (intersection != null) {
            if (lineSegment.domainContains(intersection.getX())
                    && lineSegment.rangeContains(intersection.getY())) {
                return intersection;
            }
        }
        return null;
    }

    public Double getX(int y) {
        int changeY = endY - startY;
        if (changeY == 0) {
            return null;
        }
        y = y - startY;
        return (double) ((changeY - y) * startX + y * endX) / changeY;
    }

    public Double getY(int x) {
        int changeX = endX - startX;
        if (changeX == 0) {
            return null;
        }
        x = x - startX;
        return (double) ((changeX - x) * startY + x * endY) / changeX;
    }

    /**
     *
     * @return the angle from the positive x-axis (from -pi/2 to pi/2).
     */
    public double getAngleFromX() { // TODO sth wrong here
        return Math.atan(((double) (endY - startY)) / (endX - startX));
    }

    @Override
    public String toString() {
        return "Line: (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")";
    }

    // public static void main(String[] args) {
    // Point p1 = new Point(5, 9);
    // Point p2 = new Point(2, 7);
    // Line l1 = new Line(p1, p2);
    //
    // Point p3 = new Point(5, 1);
    // Point p4 = new Point(2, 3);
    // Line l2 = new Line(p3, p4);
    //
    // // System.out.println("l1 parallel to l2? " + l1.isParallelTo(l2));
    // // System.out.println("l1 same line as to l2? " + l1.isSameLineAs(l2));
    // // System.out.println("l1 parallel to l1? " + l1.isParallelTo(l1));
    // // System.out.println("l1 same line as to l1? " + l1.isSameLineAs(l1));
    // // System.out.println("l1 parallel to l3? " + l1.isParallelTo(l3));
    // // System.out.println("l2 same line as to l3? " + l2.isSameLineAs(l3));
    // System.out.println("l1 intersection l2: " + l1.getLineIntersection(l2));
    // }

}
