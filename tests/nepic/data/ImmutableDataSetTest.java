package nepic.data;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nepic.geo.BoundedRegion;
import nepic.geo.BoundingBox;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * JUnit tests for {@link MutableDataSet}.
 *
 * @author AJ Parmidge
 */
public class ImmutableDataSetTest {
    private Point containedPoint1;
    private Point containedPoint2;
    private Point containedPoint3;
    private Point uncontainedPoint1;
    private Point uncontainedPoint2;
    private MutableDataSet underlyingDataSet;
    private ImmutableDataSet dataSet;

    @Before
    public void setUp() {
        underlyingDataSet = new MutableDataSet();
        underlyingDataSet.setRgb(0x684729);
        containedPoint1 = new Point(5, 6);
        containedPoint2 = new Point(5, 3);
        containedPoint3 = new Point(-39, 0);
        uncontainedPoint1 = new Point(-1, 14);
        uncontainedPoint2 = new Point(374, 9);
        underlyingDataSet.addAll(
                Lists.newArrayList(containedPoint1, containedPoint2, containedPoint3));
        dataSet = new ImmutableDataSet(underlyingDataSet);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_Null_Throws() {
        new ImmutableDataSet((DataSet) null);
    }

    @Test
    public void getRgb() {
        assertEquals(underlyingDataSet.getRgb(), dataSet.getRgb());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRgb_Throws() {
        dataSet.setRgb(0x859390);
    }

    @Test(expected = IllegalStateException.class)
    public void getMinX_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMinX();
    }

    @Test
    public void getMinX_Succeeds() {
        assertEquals(underlyingDataSet.getMinX(), dataSet.getMinX());
    }

    @Test(expected = IllegalStateException.class)
    public void getMaxX_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMaxX();
    }

    @Test
    public void getMaxX_Succeeds() {
        assertEquals(underlyingDataSet.getMaxX(), dataSet.getMaxX());
    }

    @Test(expected = IllegalStateException.class)
    public void getMinY_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMinY();
    }

    @Test
    public void getMinY_Succeeds() {
        assertEquals(underlyingDataSet.getMinY(), dataSet.getMinY());
    }

    @Test(expected = IllegalStateException.class)
    public void getMaxY_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMaxY();
    }

    @Test(expected = NullPointerException.class)
    public void boundsContain_Region_NullRegion_Throws() {
        dataSet.boundsContain((BoundedRegion) null);
    }

    @Test(expected = IllegalStateException.class)
    public void boundsContain_Region_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.boundsContain(
                new BoundingBox(1 /* minX */, 2 /* maxX */, 0 /* minY */, 5 /* maxY */));
    }

    @Test
    public void boundsContain_Region_Succeeds() {
        int minX = dataSet.getMinX();
        int maxX = dataSet.getMaxX();
        int minY = dataSet.getMinY();
        int maxY = dataSet.getMaxY();

        // Test the method.
        assertTrue(dataSet.boundsContain(new BoundingBox(minX, maxX, minY, maxY)));
        assertFalse(dataSet.boundsContain(new BoundingBox(minX, maxX, minY, maxY + 1)));
    }

    @Test(expected = IllegalStateException.class)
    public void boundsContain_Coordinate_EmptyDataSet_Throws() {
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.boundsContain(0, 0);
    }

    @Test
    public void boundsContain_Coordinate_Succeeds() {
        int x = dataSet.getMaxX();
        int y = dataSet.getMinY();

        assertTrue(dataSet.boundsContain(x, y));
        assertFalse(dataSet.boundsContain(x + 1, y));
        assertFalse(dataSet.boundsContain(x, y - 1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void add_Throws() {
        dataSet.add(new Point(4, 5));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addAll_NullCollection_Throws() {
        dataSet.addAll(Lists.newArrayList(new Point(3, 4), new Point(5, -1)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clear_Throws() {
        dataSet.clear();
    }

    @Test
    public void contains() {
        // Case: Non-empty data set contains the element.
        assertTrue(dataSet.contains(containedPoint1));

        // Case: Non-empty data set does NOT contain the element.
        assertFalse(dataSet.contains(uncontainedPoint1));

        // Case: Empty data set.
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty());
        assertFalse(dataSet.contains(containedPoint1));
    }

    @Test
    public void containsAll() {
        // Case: Non-empty data set containing none of the elements.
        assertFalse(dataSet.containsAll(Lists.newArrayList(uncontainedPoint1, uncontainedPoint2)));

        // Case: Non-empty data set containing some of the elements.
        assertFalse(dataSet.containsAll(Lists.newArrayList(containedPoint1, uncontainedPoint2)));
        assertFalse(dataSet.containsAll(Lists.newArrayList(uncontainedPoint1, containedPoint2)));

        // Case: Non-empty data set containing all of the elements.
        assertTrue(dataSet.containsAll(Lists.newArrayList(containedPoint1, containedPoint2)));

        // Case: Empty data set.
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty());
        assertFalse(dataSet.containsAll(Lists.newArrayList(containedPoint1, containedPoint2)));
    }

    @Test
    public void iterator() {
        // Non-empty data set.
        int pt1IteratedNum = 0;
        int pt2IteratedNum = 0;
        int pt3IteratedNum = 0;
        for (Point pt : dataSet) { // Implicitly uses the DataSet's iterator.
            if (pt.equals(containedPoint1)) {
                pt1IteratedNum++;
            } else if (pt.equals(containedPoint2)) {
                pt2IteratedNum++;
            } else if (pt.equals(containedPoint3)) {
                pt3IteratedNum++;
            } else {
                fail("Unexpected point " + pt);
            }
        }
        assertEquals(1, pt1IteratedNum); // Should only have iterated over point1 once.
        assertEquals(1, pt2IteratedNum); // Should only have iterated over point2 once.
        assertEquals(1, pt3IteratedNum); // Should only have iterated over point3 once.

        // Verify remove operation not supported.
        Iterator<Point> itr = dataSet.iterator();
        try {
            itr.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        // Empty data set.
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty());
        itr = dataSet.iterator();
        assertFalse(itr.hasNext());
        try {
            itr.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // Expected.
        }

        // Verify remove operation not supported.
        try {
            itr.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }

    @Test
    public void size() {
        assertEquals(underlyingDataSet.size(), dataSet.size());

        // Add a point to the underlying data set.
        underlyingDataSet.add(containedPoint2);
        assertEquals(underlyingDataSet.size(), dataSet.size());

        // Clear all points from the underlying data set.
        underlyingDataSet.clear();
        assertEquals(underlyingDataSet.size(), dataSet.size());
    }

    @Test
    public void toArray_NoArgs() {
        // Non-empty data set.
        Point[] arrayData = (Point[]) dataSet.toArray();
        assertEquals(3, arrayData.length);
        for (Point pt : arrayData) {
            assertTrue(dataSet.contains(pt));
        }

        // Verify that modifying a point in the array does NOT modify the point in the data set.
        assertFalse(arrayData[2].y == 13);
        arrayData[2].y = 13;
        assertFalse(dataSet.contains(arrayData[2]));

        // Empty data set.
        underlyingDataSet.clear();
        assertTrue(dataSet.isEmpty());
        arrayData = (Point[]) dataSet.toArray();
        assertEquals(0, arrayData.length);
    }

    @Test(expected = NullPointerException.class)
    public void toArray_GivenArrayNull_Throws() {
        dataSet.toArray((Point[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toArray_GivenArrayNotPointArray_Throws() {
        dataSet.toArray(new String[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toArray_GivenArrayWrongSize_Throws() {
        int size = dataSet.size();
        dataSet.toArray(new Point[size + 1]);
    }

    @Test
    public void toArray_GivenArray_Succeeds() {
        // Call method.
        Point[] arrayData = dataSet.toArray(new Point[dataSet.size()]);
        for (Point pt : arrayData) {
            assertTrue(dataSet.contains(pt));
        }

        // Verify that modifying a point in the array does NOT modify the point in the data set.
        assertFalse(arrayData[2].y == 13);
        arrayData[2].y = 13;
        assertFalse(dataSet.contains(arrayData[2]));
    }
}
