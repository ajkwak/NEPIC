package nepic.util;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DataSet implements Iterable<Point> {
    private int minX, maxX, minY, maxY;
    List<Point> data;

    public DataSet() {
        data = new LinkedList<Point>();
    }

    public DataSet(Iterable<? extends Point> pts) {
        this();
        addPoints(pts);
    }

    public DataSet addPoint(Point pt) {
        int x = pt.x;
        int y = pt.y;
        data.add(new Point(x, y));
        adjustMinimaAndMaxima(x, y);
        return this;
    }

    public DataSet addPoints(Iterable<? extends Point> pts) {
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
