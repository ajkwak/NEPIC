package nepic.util;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ColoredDataSet implements Iterable<Point> {
    private final int rgb;
    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;
    List<Point> data;

    public ColoredDataSet(int rgb) {
        data = new LinkedList<Point>();
        this.rgb = rgb;
    }

    public ColoredDataSet addPoint(Point pt) {
        int x = pt.x;
        int y = pt.y;
        data.add(new Point(x, y));
        adjustMinimaAndMaxima(x, y);
        return this;
    }

    public ColoredDataSet addPoints(Iterable<? extends Point> pts) {
        for (Point pt : pts) {
            addPoint(pt);
        }
        return this;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getRgb() {
        return rgb;
    }

    private void adjustMinimaAndMaxima(int x, int y) {
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

    public int size() {
        return data.size();
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            Iterator<Point> dataItr = data.iterator();

            @Override
            public boolean hasNext() {
                return dataItr.hasNext();
            }

            @Override
            public Point next() {
                return dataItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
