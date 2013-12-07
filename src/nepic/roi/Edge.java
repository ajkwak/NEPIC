package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nepic.util.Verify;

public class Edge {
    private static final double FLAT_EDGE_THRESHOLD = 0.33;
    public enum Type {
        RISING,
        FALLING,
        FLAT
    }

    private final Type type;
    List<Point> points;
    List<Integer> positiveDeltaYs;
    List<Integer> negativeDeltaYs;

    public Edge(Type type, List<Point> pts) {
        Verify.notNull(type);
        Verify.nonEmpty(pts);
        this.type = type;
        points = new ArrayList<Point>(pts.size());
        for (Point pt : pts) { // Each point must have an x value 1 more than the last
            points.add(new Point(pt.x, pt.y));
        }
        positiveDeltaYs = new LinkedList<Integer>();
        positiveDeltaYs = new LinkedList<Integer>();
        Point startPt = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Point endPt = points.get(i);
            Verify.argument(endPt.x == startPt.x + 1);
            int changeY = endPt.y - startPt.y;
            if (changeY > 0) {
                positiveDeltaYs.add(changeY);
            } else if (changeY < 0) {
                negativeDeltaYs.add(changeY);
            }
        }
        Collections.sort(positiveDeltaYs);
        Collections.sort(negativeDeltaYs);
    }

    public Type getType() {
        return type;
    }

    public int getStartX() {
        return points.get(0).x;
    }

    public int getEndX() {
        return points.get(points.size() - 1).x;
    }

    public int getTotalChangeX() {
        return getEndX() - getStartX();
    }

    public int getStartY() {
        return points.get(0).y;
    }

    public int getEndY() {
        return points.get(points.size() - 1).y;
    }

    public double getAvgDeltaY() {
        return getTotalChangeY() / (points.size() - 1);
    }

    public int getMedianPositiveDeltaY() {
        return positiveDeltaYs.get(positiveDeltaYs.size() / 2 + 1);
    }

    public int getMedianNegativeDeltaY() {
        return negativeDeltaYs.get(negativeDeltaYs.size() / 2 + 1);
    }

    public int getTotalChangeY() {
        return getEndY() - getStartY();
    }

    public int getDomainSize() {
        return getTotalChangeX() + 1;
    }

    public int getRangeSize() {
        return Math.abs(getTotalChangeY()) + 1;
    }

}
