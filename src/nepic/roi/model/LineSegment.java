package nepic.roi.model;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

public class LineSegment extends Line {

    public LineSegment(Point startPoint, Point endPoint) {
        super(startPoint, endPoint);
    }

    /**
     * Gets the end-points of the line segment. NOTE: This method treats this {@link Line} object as
     * a line segment rather than a continuous line.
     *
     * @return the end-points of the line segment.
     */
    public Point[] getEndPoints() {
        return new Point[] { new Point(startX, startY), new Point(endX, endY) };
    }

    /**
     * Finds the intersection between this line segment and the other line segment.
     *
     * @param other the line segment with which to find an intersection.
     * @return null if the two line segments do not cross, or if the lines represented by the line
     *         segments are equivalent; otherwise returns the single intersection between the two
     *         line segments.
     */
    // Treats both this and other as a line segment
    public Point2D getSegmentIntersection(LineSegment other) {
        Point2D intersection = super.getLineIntersection(other);
        if (intersection != null) {
            double intersectX = intersection.getX();
            double intersectY = intersection.getY();
            if (this.domainContains(intersectX) && this.rangeContains(intersectY)
                    && other.domainContains(intersectX) && other.rangeContains(intersectY)) {
                return intersection;
            }
        }
        return null;
    }

    /**
     * Finds the intersection between this line segment and the other line.
     *
     * @param line the line with which to find an intersection.
     * @return null if the two line segments do not cross, or if the lines represented by the line
     *         segments are equivalent; otherwise returns the single intersection between the two
     *         line segments.
     */
    // Convenience method which calls the getIntersection(LineSegment) on the line parameter.
    public Point2D getIntersectionWithLine(Line line) {
        return line.getIntersectionWithSegment(this);
    }

    public Point getMidPoint() {
        return new Point((startX + endX) / 2, (startY + endY) / 2);
    }

    public double getLength() {
        int changeX = endX - startX;
        int changeY = endY - startY;
        return Math.sqrt(changeX * changeX + changeY * changeY);
    }

    @Override
    public Double getX(int y) {
        if (rangeContains(y)) {
            Double x = super.getX(y);
            if (x != null && domainContains(x)) {
                return x;
            }
        }
        return null;
    }

    @Override
    public Double getY(int x) {
        if (domainContains(x)) {
            Double y = super.getY(x);
            if (y != null && rangeContains(y)) {
                return y;
            }
        }
        return null;
    }

    /**
     * Determines whether the given x value is within the domain of this line segment.
     *
     * @param x
     * @return
     */
    public boolean domainContains(double x) {
        return (x >= startX && x <= endX) || (x > endX && x < startX);
    }

    /**
     * Does NOT include end points in domain
     *
     * @param x
     * @return
     */
    public boolean domainContainsExclusive(int x) {
        return (x > startX && x < endX) || (x > endX && x < startX);
    }

    public boolean rangeContains(double y) {
        return (y >= startY && y <= endY) || (y > endY && y < startY);
    }

    public boolean rangeContainsExclusive(int y) {
        return (y > startY && y < endY) || (y > endY && y < startY);
    }

    public enum IncludeStart {
        YES(0),
        NO(1);
        final int val;

        private IncludeStart(int n) {
            val = n;
        }
    }

    public enum IncludeEnd {
        YES(0),
        NO(-1);
        int val;

        private IncludeEnd(int n) {
            val = n;
        }
    }

    /**
     * Draw line including endpoints
     *
     * @return
     */
    public List<Point> draw() {
        return draw(IncludeStart.YES, IncludeEnd.YES);
    }// drawLine_avg

    public List<Point> draw(IncludeStart is, IncludeEnd ie) {
        if (moreVertical) {
            return drawByY(is, ie);
        } else {
            return drawByX(is, ie);
        }
    }// drawLine_avg

    // Horizontal lines: includes only 1 point at most
    public List<Point> drawByY(IncludeStart is, IncludeEnd ie) {
        List<Point> pixsInLine = new LinkedList<Point>();
        int changeY = yIncrement * (endY - startY);
        int nextY;
        double nextX;
        for (int i = 0 + is.val; i <= changeY + ie.val; i++) {
            nextY = startY + yIncrement * i;
            nextX = (double) ((changeY - i) * startX + i * endX) / changeY;
            nextX = nextX < 0 ? nextX - 0.5 : nextX + 0.5;
            pixsInLine.add(new Point((int) nextX, nextY));
        }// for all y values
        return pixsInLine;
    }

    // Vertical lines: includes only at most 1 point
    public List<Point> drawByX(IncludeStart is, IncludeEnd ie) {
        List<Point> pixsInLine = new LinkedList<Point>();
        int changeX = xIncrement * (endX - startX);
        int nextX;
        double nextY;
        for (int i = 0 + is.val; i <= changeX + ie.val; i++) {
            nextX = startX + xIncrement * i;
            nextY = ((double) (changeX - i) * startY + i * endY) / changeX;
            nextY = nextY < 0 ? nextY - 0.5 : nextY + 0.5;
            pixsInLine.add(new Point(nextX, (int) nextY));
        }// for all y values
        return pixsInLine;
    }

    public static void main(String[] args) {
        LineSegment line = new LineSegment(new Point(0, 0), new Point(5, 5));
        System.out.println(line.getY(4));
    }

}
