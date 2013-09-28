package nepic.util;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;

/**
 * An immutable implementation of the {@link DataSet} interface.
 *
 * @author AJ Parmidge
 */
public class ImmutableDataSet implements DataSet {
    private final DataSet dataSet;

    /**
     * Creates an immutable wrapper of the given {@link DataSet}.
     *
     * @param dataSet the data set for which to create an immutable wrapper
     */
    public ImmutableDataSet(DataSet dataSet) {
        Verify.notNull(dataSet, "DataSet");
        this.dataSet = dataSet;
    }

    @Override
    public String getName() {
        return dataSet.getName();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRgb() {
        return dataSet.getRgb();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public void setRgb(int rgb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinX() {
        return dataSet.getMinX();
    }

    @Override
    public int getMaxX() {
        return dataSet.getMaxX();
    }

    @Override
    public int getMinY() {
        return dataSet.getMinY();
    }

    @Override
    public int getMaxY() {
        return dataSet.getMaxY();
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        return dataSet.boundsContain(region);
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return dataSet.boundsContain(x, y);
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public boolean add(Point e) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public boolean addAll(Collection<? extends Point> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return dataSet.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return dataSet.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return dataSet.isEmpty();
    }

    @Override
    public Iterator<Point> iterator() {
        return dataSet.iterator(); // The MutableDataSet's iterator is already immmutable.
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always (this implementation is immutable)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return dataSet.size();
    }

    @Override
    public Object[] toArray() {
        return dataSet.toArray(); // Returns a copy, therefore maintaining this class as immutable.
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return dataSet.toArray(a); // Returns a copy, therefore maintaining this class as immutable.
    }
}
