package nepic.util;

import java.awt.Point;

public class DoublePoint { // TODO: rename
    public double x;
    public double y;

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point asPoint() {
        int roundedX = (int) Math.round(x);
        int roundedY = (int) Math.round(y);
        return new Point(roundedX, roundedY);
    }

    public static boolean sameWithinError(double d1, double d2, double error) {
        return d2 >= d1 - error && d2 <= d1 + error;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
