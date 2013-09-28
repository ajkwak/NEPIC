package nepic.util;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A mutable implementation of the the {@link DataSet} interface.
 *
 * @author AJ Parmidge
 */
public class MutableDataSet implements DataSet {
    private String name = ""; // Default is empty string.
    private final List<Point> data;
    private int rgb = 0x000000; // Default is black.

    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;

    /**
     * Creates an empty {@link MutableDataSet}.
     */
    public MutableDataSet() {
        this.data = new LinkedList<Point>();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name (label) of this data set to the given value.
     *
     * @param rgb the color to set
     */
    @Override
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = ""; // Reset to empty if given a null string
        }
    }

    @Override
    public int getRgb() {
        return rgb;
    }

    @Override
    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    @Override
    public int getMinX() {
        Verify.state(!data.isEmpty(), "No min x of empty data set.");
        return minX;
    }

    @Override
    public int getMaxX() {
        Verify.state(!data.isEmpty(), "No max x of empty data set.");
        return maxX;
    }

    @Override
    public int getMinY() {
        Verify.state(!data.isEmpty(), "No min y of empty data set.");
        return minY;
    }

    @Override
    public int getMaxY() {
        Verify.state(!data.isEmpty(), "No max y of empty data set.");
        return maxY;
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        Verify.state(!data.isEmpty(), "Bounds of empty dataset undefined.");
        return region.getMinX() >= minX && region.getMaxX() <= maxX && region.getMinY() >= minY
                && region.getMaxY() <= maxY;
    }

    @Override
    public boolean boundsContain(int x, int y) {
        Verify.state(!data.isEmpty(), "Bounds of empty dataset undefined.");
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    @Override
    public boolean add(Point pt) {
        Verify.notNull(pt, "Point");
        int x = pt.x;
        int y = pt.y;
        data.add(new Point(x, y));
        adjustMinimaAndMaxima(x, y);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Point> pts) {
        Verify.noNullElements(pts);
        for (Point pt : pts) {
            add(pt);
        }
        return true;
    }

    /**
     * Clears all the data from this data set, while leaving the name and color of the
     * {@link DataSet} intact.
     */
    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
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

    /**
     * @return {@code false}
     */
    @Override
    public boolean remove(Object o) {
        return false;
    }

    /**
     * @return {@code false}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * @return {@code false}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public int size() {
        return data.size();
    }

    /**
     * @return an array containing all points in this data set. However, changing the points in this
     *         array will NOT affect the points in the data set.
     */
    @Override
    public Object[] toArray() {
        Point[] array = new Point[data.size()];
        int i = 0;
        for (Point datum : data) {
            array[i++] = new Point(datum.x, datum.y);
        }
        return array;
    }

    /**
     * @return an array containing all points in this data set. However, changing the points in this
     *         array will NOT affect the points in the data set.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        Verify.notNull(a, "Given array");
        Verify.argument(a instanceof Point[] && a.length == data.size(),
                "Given argument must be an array of points of length " + data.size());
        Point[] array = (Point[]) a;
        int i = 0;
        for (Point datum : data) {
            array[i++] = new Point(datum.x, datum.y);
        }
        return a;
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
}
