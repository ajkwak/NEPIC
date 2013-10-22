package nepic.util;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import nepic.data.MutableDataSet;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link MutableDataSet}.
 *
 * @author AJ Parmidge
 */
public class MutableDataSetTest {
    private MutableDataSet dataSet;

    @Before
    public void setUp() {
        this.dataSet = new MutableDataSet();
    }

    @Test
    public void dataSetInitiallyEmpty() {
        dataSet = new MutableDataSet();
        assertTrue(dataSet.isEmpty());
        assertEquals(0, dataSet.size());
        assertEquals("", dataSet.getName());
    }

    @Test
    public void setName_NonEmptyString() {
        String name = "Region of Interest";
        assertFalse(name.equals(dataSet.getName()));

        // Set data set name to the given value.
        dataSet.setName(name);
        assertEquals(name, dataSet.getName());
    }

    @Test
    public void setName_EmptyString() {
        dataSet.setName("nonEmptyString");
        String name = "";
        assertFalse(name.equals(dataSet.getName()));

        // Set data set name to the given value.
        dataSet.setName(name);
        assertEquals(name, dataSet.getName());
    }

    @Test
    public void setName_NullToEmptyString() {
        dataSet.setName("nonEmptyString");
        String name = null;
        assertNotNull(dataSet.getName());

        // Set data set name to the given value.
        dataSet.setName(name);
        assertEquals("", dataSet.getName());
    }

    @Test
    public void setRgb() {
        int rgb = 0x839259;
        assertFalse(dataSet.getRgb() == rgb);

        // Set data set RGB to the given value.
        dataSet.setRgb(rgb);
        assertEquals(rgb, dataSet.getRgb());
    }

    @Test(expected = IllegalStateException.class)
    public void getMinX_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMinX();
    }

    @Test(expected = IllegalStateException.class)
    public void getMaxX_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMaxX();
    }

    @Test(expected = IllegalStateException.class)
    public void getMinY_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMinY();
    }

    @Test(expected = IllegalStateException.class)
    public void getMaxY_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.getMaxY();
    }

    @Test(expected = NullPointerException.class)
    public void boundsContain_Region_NullRegion_Throws() {
        dataSet.add(new Point(0, 0)); // Make sure the data set is non-empty.
        dataSet.boundsContain((BoundedRegion) null);
    }

    @Test(expected = IllegalStateException.class)
    public void boundsContain_Region_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.boundsContain(
                new TestBoundedRegion(1 /* minX */, 2 /* maxX */, 0 /* minY */, 5 /* maxY */));
    }

    @Test
    public void boundsContain_Region_Succeeds() {
        int minX = 0;
        int maxX = 5;
        int minY = -13;
        int maxY = 12;
        dataSet.add(new Point(minX, minY));
        dataSet.add(new Point(maxX, maxY));
        assertEquals(minX, dataSet.getMinX());
        assertEquals(maxX, dataSet.getMaxX());
        assertEquals(minY, dataSet.getMinY());
        assertEquals(maxY, dataSet.getMaxY());

        // Test the method.
        assertTrue(dataSet.boundsContain(new TestBoundedRegion(minX, maxX, minY, maxY)));
        assertFalse(dataSet.boundsContain(new TestBoundedRegion(minX, maxX, minY, maxY + 1)));
    }

    @Test(expected = IllegalStateException.class)
    public void boundsContain_Coordinate_EmptyDataSet_Throws() {
        assertTrue(dataSet.isEmpty()); // Verify preconditions.
        dataSet.boundsContain(0, 0);
    }

    @Test
    public void boundsContain_Coordinate_Succeeds() {
        int x = 89;
        int y = 49;
        dataSet.add(new Point(x, y));

        assertTrue(dataSet.boundsContain(x, y));
        assertFalse(dataSet.boundsContain(x + 1, y));
        assertFalse(dataSet.boundsContain(x, y - 1));
    }

    @Test(expected = NullPointerException.class)
    public void add_Null_Throws() {
        dataSet.add((Point) null);
    }

    @Test
    public void add_Succeeds() {
        assertTrue(dataSet.isEmpty());

        // Add the first point.
        Point pt1 = new Point(5, 16);
        dataSet.add(pt1);
        assertTrue(dataSet.contains(pt1));
        assertEquals(1, dataSet.size());
        assertEquals(pt1.x, dataSet.getMinX());
        assertEquals(pt1.x, dataSet.getMaxX());
        assertEquals(pt1.y, dataSet.getMinY());
        assertEquals(pt1.y, dataSet.getMaxY());

        // Add another point that doesn't change the bounds of the data set.
        dataSet.add(pt1);
        assertTrue(dataSet.contains(pt1));
        assertEquals(2, dataSet.size());
        assertEquals(pt1.x, dataSet.getMinX());
        assertEquals(pt1.x, dataSet.getMaxX());
        assertEquals(pt1.y, dataSet.getMinY());
        assertEquals(pt1.y, dataSet.getMaxY());

        // Add a third point that changes the bounds of the data set.
        Point pt3 = new Point(4, 55);
        dataSet.add(pt3);
        assertTrue(dataSet.contains(pt3));
        assertEquals(3, dataSet.size());
        assertEquals(pt3.x, dataSet.getMinX());
        assertEquals(pt1.x, dataSet.getMaxX());
        assertEquals(pt1.y, dataSet.getMinY());
        assertEquals(pt3.y, dataSet.getMaxY());
    }

    @Test(expected = NullPointerException.class)
    public void addAll_NullCollection_Throws() {
        dataSet.addAll((Collection<Point>) null);
    }

    @Test(expected = NullPointerException.class)
    public void addAll_NullPointInCollection_Throws() {
        dataSet.addAll(Lists.newArrayList(new Point(0, 0), null, new Point(47, 8)));
    }

    @Test
    public void addAll_Succeeds() {
        List<Point> toAdd = Lists.newArrayList(new Point(4, -4), new Point(38, 0), new Point(8, 9));
        dataSet.addAll(toAdd);
        assertTrue(dataSet.containsAll(toAdd));
    }

    @Test
    public void clear() {
        // Case: Data set is already empty.
        assertTrue(dataSet.isEmpty());
        dataSet.clear();
        assertTrue(dataSet.isEmpty());

        // Case: Data set is non-empty.
        dataSet.add(new Point(38, 9));
        assertFalse(dataSet.isEmpty());
        dataSet.clear();
        assertTrue(dataSet.isEmpty());
    }

    @Test
    public void contains() {
        // Case: Empty data set.
        assertTrue(dataSet.isEmpty());
        assertFalse(dataSet.contains(new Point(4, 5)));

        // Case: Non-empty data set contains the element.
        Point added = new Point(4, 5);
        dataSet.add(added);
        assertTrue(dataSet.contains(added));

        // Case: Non-empty data set does NOT contain the element.
        Point notAdded = new Point(10, 6);
        assertFalse(dataSet.contains(notAdded));
    }

    @Test
    public void containsAll() {
        // Case: Empty data set.
        assertTrue(dataSet.isEmpty());
        assertFalse(dataSet.containsAll(Lists.newArrayList(new Point(3, 3), new Point(1, 2))));

        // Case: Non-empty data set containing none of the elements.
        Point contained1 = new Point(3, 7);
        Point contained2 = new Point(-4, 89);
        Point uncontained1 = new Point(1, 6);
        Point uncontained2 = new Point(3, -4);
        dataSet.add(contained1);
        dataSet.add(contained2);
        assertFalse(dataSet.containsAll(Lists.newArrayList(uncontained1, uncontained2)));

        // Case: Non-empty data set containing some of the elements.
        assertFalse(dataSet.containsAll(Lists.newArrayList(contained1, uncontained2)));
        assertFalse(dataSet.containsAll(Lists.newArrayList(uncontained1, contained2)));

        // Case: Non-empty data set containing all of the elements.
        assertTrue(dataSet.containsAll(Lists.newArrayList(contained1, contained2)));
    }

    @Test
    public void iterator() {
        // Empty data set.
        assertTrue(dataSet.isEmpty());
        Iterator<Point> itr = dataSet.iterator();
        assertFalse(itr.hasNext());
        try{
            itr.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // Expected.
        }

        // Non-empty data set.
        Point point1 = new Point(5, 8);
        Point point2 = new Point(7, 9);
        Point point3 = new Point(78, -3);
        dataSet.add(point1);
        dataSet.add(point2);
        dataSet.add(point3);
        int pt1IteratedNum = 0;
        int pt2IteratedNum = 0;
        int pt3IteratedNum = 0;
        for (Point pt : dataSet) { // Implicitly uses the DataSet's iterator.
            if (pt.equals(point1)) {
                pt1IteratedNum++;
            } else if (pt.equals(point2)) {
                pt2IteratedNum++;
            } else if (pt.equals(point3)) {
                pt3IteratedNum++;
            } else {
                fail("Unexpected point " + pt);
            }
        }
        assertEquals(1, pt1IteratedNum); // Should only have iterated over point1 once.
        assertEquals(1, pt2IteratedNum); // Should only have iterated over point2 once.
        assertEquals(1, pt3IteratedNum); // Should only have iterated over point3 once.
    }

    @Test
    public void size() {
        assertTrue(dataSet.isEmpty());
        assertEquals(0, dataSet.size());

        // Add a point.
        dataSet.add(new Point(3, 4));
        assertEquals(1, dataSet.size());

        // Add another point.
        dataSet.add(new Point(-1, 7));
        assertEquals(2, dataSet.size());

        // Clear all points from the data set.
        dataSet.clear();
        assertEquals(0, dataSet.size());
    }

    @Test
    public void toArray_NoArgs() {
        // Empty data set.
        assertTrue(dataSet.isEmpty());
        Point[] arrayData = (Point[]) dataSet.toArray();
        assertEquals(0, arrayData.length);

        // Non-empty data set.
        dataSet.addAll(Lists.newArrayList(new Point(0, 0), new Point(67, -2), new Point(4, 1)));
        arrayData = (Point[]) dataSet.toArray();
        assertEquals(3, arrayData.length);
        for (Point pt : arrayData) {
            assertTrue(dataSet.contains(pt));
        }

        // Verify that modifying a point in the array does NOT modify the point in the data set.
        assertFalse(arrayData[2].y == 13);
        arrayData[2].y = 13;
        assertFalse(dataSet.contains(arrayData[2]));
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
        // Set up data set.
        dataSet.add(new Point(4, 3));
        assertEquals(1, dataSet.size());

        // Call method.
        dataSet.toArray(new Point[3]);
    }

    @Test
    public void toArray_GivenArray_Succeeds() {
        // Set up data set.
        dataSet.addAll(Lists.newArrayList(new Point(4, 2), new Point(8, 90), new Point(-1, 0)));
        assertEquals(3, dataSet.size());

        // Call method.
        Point[] arrayData = dataSet.toArray(new Point[3]);
        for (Point pt : arrayData) {
            assertTrue(dataSet.contains(pt));
        }

        // Verify that modifying a point in the array does NOT modify the point in the data set.
        assertFalse(arrayData[2].y == 13);
        arrayData[2].y = 13;
        assertFalse(dataSet.contains(arrayData[2]));
    }
}
