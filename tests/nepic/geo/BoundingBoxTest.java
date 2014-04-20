package nepic.geo;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Test;

public class BoundingBoxTest {
    private static final int MIN_X = 10;
    private static final int MAX_X = 15;
    private static final int MIN_Y = -13;
    private static final int MAX_Y = 0;

    @Test(expected = IllegalArgumentException.class)
    public void ctor_integers_minXGreaterThanMaxX_throws() {
        new BoundingBox(MAX_X /* minX */, MIN_X /* maxX */, MIN_Y /* minY */, MAX_Y /* maxY */);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctor_integers_minYGreaterThanMaxY_throws() {
        new BoundingBox(MIN_X /* minX */, MAX_X /* maxX */, MAX_Y /* minY */, MIN_Y /* maxY */);
    }

    @Test
    public void ctor_integers_succeeds() {
        // CASE: BoundingBox is single point.
        int minX = MIN_X;
        int maxX = minX;
        int minY = MIN_Y;
        int maxY = minY;
        BoundingBox boundingBox = new BoundingBox(minX, maxX, minY, maxY);
        assertExpectedBoundaries(boundingBox, minX, maxX, minY, maxY);

        // CASE: BoundingBox is horizontal line.
        maxX = MAX_X;
        boundingBox = new BoundingBox(minX, maxX, minY, maxY);
        assertExpectedBoundaries(boundingBox, minX, maxX, minY, maxY);

        // CASE: BoundingBox is vertical line.
        maxX = MIN_X;
        maxY = MAX_Y;
        boundingBox = new BoundingBox(minX, maxX, minY, maxY);
        assertExpectedBoundaries(boundingBox, minX, maxX, minY, maxY);

        // CASE: BoundingBox is rectangle.
        maxX = MAX_X;
        boundingBox = new BoundingBox(minX, maxX, minY, maxY);
        assertExpectedBoundaries(boundingBox, minX, maxX, minY, maxY);
    }

    @Test
    public void ctor_points_succeeds() {
        Point p1 = new Point(MIN_X, MIN_Y);

        // CASE: BoundingBox is single point.
        BoundingBox boundingBox = new BoundingBox(p1, p1);
        assertExpectedBoundaries(boundingBox, p1.x, p1.x, p1.y, p1.y);

        // CASE: BoundingBox is horizontal line.
        Point p2 = new Point(MAX_X, MIN_Y);
        boundingBox = new BoundingBox(p1, p2);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, p1.y, p1.y);
        boundingBox = new BoundingBox(p2, p1);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, p1.y, p1.y);

        // CASE: BoundingBox is vertical line.
        p2 = new Point(MIN_X, MAX_Y);
        boundingBox = new BoundingBox(p1, p2);
        assertExpectedBoundaries(boundingBox, p1.x, p1.x, MIN_Y, MAX_Y);
        boundingBox = new BoundingBox(p2, p1);
        assertExpectedBoundaries(boundingBox, p1.x, p1.x, MIN_Y, MAX_Y);

        // CASE: BoundingBox is rectangle.
        p2 = new Point(MAX_X, MAX_Y);
        boundingBox = new BoundingBox(p1, p2);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        boundingBox = new BoundingBox(p2, p1);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        p1 = new Point(MIN_X, MAX_Y);
        p2 = new Point(MAX_X, MIN_Y);
        boundingBox = new BoundingBox(p1, p2);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        boundingBox = new BoundingBox(p2, p1);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
    }

    @Test
    public void update() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: Update the BoundingBox with a point already contained by the box.
        boundingBox.update(MIN_X, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: Update the BoundingBox with a point outside the bounds of the box.
        boundingBox.update(MIN_X - 1, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X - 1, MAX_X, MIN_Y, MAX_Y);
        boundingBox.update(MIN_X, MAX_Y + 1);
        assertExpectedBoundaries(boundingBox, MIN_X - 1, MAX_X, MIN_Y, MAX_Y + 1);
        boundingBox.update(MAX_X + 1, MIN_Y - 1);
        assertExpectedBoundaries(boundingBox, MIN_X - 1, MAX_X + 1, MIN_Y - 1, MAX_Y + 1);
    }

    @Test
    public void intersectsWith() {
        BoundingBox box1 = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: BoundingBoxes don't intersect.
        BoundingBox box2 = new BoundingBox(MAX_X + 1, MAX_X + 5, MIN_Y - 14, MIN_Y - 10);
        assertFalse(box1.intersectsWith(box2));
        assertFalse(box2.intersectsWith(box1));

        // CASE: BoundingBoxes intersect on only a single point.
        box2 = new BoundingBox(MAX_X, MAX_X + 5, MIN_Y - 10, MIN_Y);
        assertTrue(box1.intersectsWith(box2));
        assertTrue(box2.intersectsWith(box1));

        // CASE: BoundingBoxes intersect on only a single line.
        box2 = new BoundingBox(MIN_X, MAX_X, MIN_Y - 5, MIN_Y);
        assertTrue(box1.intersectsWith(box2));
        assertTrue(box2.intersectsWith(box1));

        // CASE: BoundingBoxes intersect on rectangle.
        box2 = new BoundingBox((MIN_X + MAX_X) / 2, MAX_X, (MIN_Y + MAX_Y) / 3, MAX_Y);
        assertTrue(box1.intersectsWith(box2));
        assertTrue(box2.intersectsWith(box1));

        // CASE: Entire BoundingBox is intersection.
        assertTrue(box1.intersectsWith(box1));
    }

    @Test
    public void getIntersectionWith() {
        BoundingBox box1 = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: BoundingBoxes don't intersect.
        BoundingBox box2 = new BoundingBox(MAX_X + 1, MAX_X + 5, MIN_Y - 14, MIN_Y - 10);
        assertNull(box1.getIntersectionWith(box2));
        assertNull(box2.getIntersectionWith(box1));

        // CASE: BoundingBoxes intersect on only a single point.
        box2 = new BoundingBox(MAX_X, MAX_X + 5, MIN_Y - 10, MIN_Y);
        BoundingBox intersection = box1.getIntersectionWith(box2);
        assertNotNull(intersection);
        assertExpectedBoundaries(intersection, MAX_X, MAX_X, MIN_Y, MIN_Y);
        intersection = box2.getIntersectionWith(box1);
        assertNotNull(intersection);
        assertExpectedBoundaries(intersection, MAX_X, MAX_X, MIN_Y, MIN_Y);

        // CASE: BoundingBoxes intersect on only a single line.
        box2 = new BoundingBox(MIN_X, MAX_X, MIN_Y - 5, MIN_Y);
        intersection = box1.getIntersectionWith(box2);
        assertExpectedBoundaries(intersection, MIN_X, MAX_X, MIN_Y, MIN_Y);
        intersection = box2.getIntersectionWith(box1);
        assertExpectedBoundaries(intersection, MIN_X, MAX_X, MIN_Y, MIN_Y);

        // CASE: BoundingBoxes intersect on rectangle.
        int intersectionMinX = (MIN_X + MAX_X) / 2;
        int intersectionMinY = (MIN_Y + MAX_Y) / 3;
        box2 = new BoundingBox(intersectionMinX, MAX_X + 5, intersectionMinY, MAX_Y + 10);
        intersection = box1.getIntersectionWith(box2);
        assertExpectedBoundaries(intersection, intersectionMinX, MAX_X, intersectionMinY, MAX_Y);
        intersection = box2.getIntersectionWith(box1);
        assertExpectedBoundaries(intersection, intersectionMinX, MAX_X, intersectionMinY, MAX_Y);

        // CASE: Entire BoundingBox is intersection.
        intersection = box1.getIntersectionWith(box1);
        assertNotNull(intersection);
        assertExpectedBoundaries(intersection, MIN_X, MAX_X, MIN_Y, MAX_Y);
    }

    @Test
    public void setMinX_invalid_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.setMinX(MAX_X + 1);
            fail("Expected exception thrown.  Instead, minX = " + boundingBox.getMinX());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void setMinX_succeeds() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // Set the minX to lower than the current minX.
        int newMinX = MIN_X - 5;
        boundingBox.setMinX(newMinX);
        assertExpectedBoundaries(boundingBox, newMinX, MAX_X, MIN_Y, MAX_Y);

        // Set the minX between the current minX and the maxX.
        newMinX = MIN_X;
        boundingBox.setMinX(newMinX);
        assertExpectedBoundaries(boundingBox, newMinX, MAX_X, MIN_Y, MAX_Y);

        // Set the minX to the maxX;
        newMinX = MAX_X;
        boundingBox.setMinX(newMinX);
        assertExpectedBoundaries(boundingBox, newMinX, MAX_X, MIN_Y, MAX_Y);
    }

    @Test
    public void setMaxX_invalid_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.setMaxX(MIN_X - 4);
            fail("Expected exception thrown.  Instead, maxX = " + boundingBox.getMaxX());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void setMaxX_succeeds() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // Set the maxX to greater than the current maxX.
        int newMaxX = MAX_X + 3;
        boundingBox.setMaxX(newMaxX);
        assertExpectedBoundaries(boundingBox, MIN_X, newMaxX, MIN_Y, MAX_Y);

        // Set the maxX between the minX and the current maxX.
        newMaxX = MAX_X;
        boundingBox.setMaxX(newMaxX);
        assertExpectedBoundaries(boundingBox, MIN_X, newMaxX, MIN_Y, MAX_Y);

        // Set the maxX to the minX;
        newMaxX = MIN_X;
        boundingBox.setMaxX(newMaxX);
        assertExpectedBoundaries(boundingBox, MIN_X, newMaxX, MIN_Y, MAX_Y);
    }

    @Test
    public void setMinY_invalid_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.setMinY(MAX_X + 1);
            fail("Expected exception thrown.  Instead, minY = " + boundingBox.getMinY());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void setMinY_succeeds() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // Set the minX to lower than the current minX.
        int newMinY = MIN_Y - 5;
        boundingBox.setMinY(newMinY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, newMinY, MAX_Y);

        // Set the minX between the current minX and the maxX.
        newMinY = MIN_Y;
        boundingBox.setMinY(newMinY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, newMinY, MAX_Y);

        // Set the minX to the maxX;
        newMinY = MAX_Y;
        boundingBox.setMinY(newMinY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, newMinY, MAX_Y);
    }

    @Test
    public void setMaxY_invalid_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.setMaxX(MIN_Y - 4);
            fail("Expected exception thrown.  Instead, maxY = " + boundingBox.getMaxY());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void setMaxY_succeeds() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, MAX_Y);

        // Set the maxX to greater than the current maxX.
        int newMaxY = MAX_Y + 3;
        boundingBox.setMaxY(newMaxY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, newMaxY);

        // Set the maxX between the minX and the current maxX.
        newMaxY = MAX_Y;
        boundingBox.setMaxY(newMaxY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, newMaxY);

        // Set the maxX to the minX;
        newMaxY = MIN_Y;
        boundingBox.setMaxY(newMaxY);
        assertExpectedBoundaries(boundingBox, MIN_X, MAX_X, MIN_Y, newMaxY);
    }

    @Test
    public void boundsContain_boundedRegion() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);

        // Case: Bounds contain none of the given region.
        BoundingBox notContained = new BoundingBox(MIN_X - 14, MIN_X - 10, MAX_X + 13, MAX_X + 23);
        assertFalse(boundingBox.boundsContain(notContained));

        // CASE: Bounds contain part of the given region.
        notContained = new BoundingBox(MIN_X, MAX_X + 1, MIN_Y, MIN_Y);
        assertFalse(boundingBox.boundsContain(notContained));

        // CASE: Bounds contain all of the given region.
        BoundingBox contained = new BoundingBox(MIN_X + 1, MAX_X - 1, MIN_Y + 1, MAX_Y - 1);
        assertTrue(boundingBox.boundsContain(contained));

        // CASE: Bounds are the same as the given region.
        assertTrue(boundingBox.boundsContain(boundingBox));
    }

    @Test
    public void boundsContain_point() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: The given point is not contained in the BoundingBox.
        assertFalse(boundingBox.boundsContain(MIN_X - 1, MIN_Y)); // Too far left.
        assertFalse(boundingBox.boundsContain(MIN_X, MAX_Y + 1)); // Too far down.
        assertFalse(boundingBox.boundsContain(MIN_X, MIN_Y - 1)); // Too far up.
        assertFalse(boundingBox.boundsContain(MAX_X + 1, MIN_Y)); // Too far right.

        // CASE: Bounds contain the given point.
        assertTrue(boundingBox.boundsContain(MIN_X, MIN_Y)); // Top left corner.
        assertTrue(boundingBox.boundsContain(MAX_X, MAX_Y)); // Top right corner
        assertTrue(boundingBox.boundsContain(MIN_X, MAX_Y)); // Bottom left corner.
        assertTrue(boundingBox.boundsContain(MAX_X, MIN_Y)); // Bottom right corner.
        Point midPoint = boundingBox.getMidPoint();
        assertTrue(boundingBox.boundsContain(midPoint.x, midPoint.y)); // Midpoint.
    }

    @Test
    public void resetBounds_minXGreaterThanMaxX_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.resetBounds(MIN_X, MIN_X - 1, MIN_Y, MIN_Y);
            fail("Expected IllegalArgumentException thrown.  Instead got " + boundingBox);
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void resetBounds_minYGreaterThanMaxY_throws() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        try {
            boundingBox.resetBounds(MIN_X, MIN_X, MIN_Y, MIN_Y - 1);
            fail("Expected IllegalArgumentException thrown.  Instead got " + boundingBox);
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void resetBounds_succeeds() {
        BoundingBox boundingBox = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);

        // CASE: Reset boundaries to single point.
        int newMinX = MIN_X - 1;
        int newMaxX = newMinX;
        int newMinY = MIN_Y - 4;
        int newMaxY = newMinY;
        boundingBox.resetBounds(newMinX, newMaxX, newMinY, newMaxY);
        assertExpectedBoundaries(boundingBox, newMinX, newMaxX, newMinY, newMaxY);

        // CASE: BoundingBox is horizontal line.
        newMinX = MIN_X + 1;
        newMaxX = MIN_X + 42;
        newMinY = MIN_Y - 5;
        newMaxY = newMinY;
        boundingBox.resetBounds(newMinX, newMaxX, newMinY, newMaxY);
        assertExpectedBoundaries(boundingBox, newMinX, newMaxX, newMinY, newMaxY);

        // CASE: BoundingBox is vertical line.
        newMinX = MIN_X + 1;
        newMaxX = newMinX;
        newMinY = MIN_Y + 1;
        newMaxY = MIN_Y + 2;
        boundingBox.resetBounds(newMinX, newMaxX, newMinY, newMaxY);
        assertExpectedBoundaries(boundingBox, newMinX, newMaxX, newMinY, newMaxY);

        // CASE: BoundingBox is rectangle.
        newMinX = MIN_X + 1;
        newMaxX = MIN_X + 42;
        newMinY = MIN_Y - 5;
        newMaxY = MIN_Y - 3;
        boundingBox.resetBounds(newMinX, newMaxX, newMinY, newMaxY);
        assertExpectedBoundaries(boundingBox, newMinX, newMaxX, newMinY, newMaxY);
    }

    @Test
    public void asPolygon() { // TODO
        fail("Not yet implemented");
    }

    @Test
    public void deepCopy() {
        BoundingBox bounds = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        BoundingBox boundsCopy = bounds.deepCopy();
        assertEquals(bounds.getMinX(), boundsCopy.getMinX());
        assertEquals(bounds.getMaxX(), boundsCopy.getMaxX());
        assertEquals(bounds.getMinY(), boundsCopy.getMinY());
        assertEquals(bounds.getMaxY(), boundsCopy.getMaxY());
    }

    @Test
    public void getMidPoint() {
        BoundingBox bounds = new BoundingBox(MIN_X, MAX_X, MIN_Y, MAX_Y);
        Point midPoint = bounds.getMidPoint();
        assertEquals((bounds.getMaxX() + bounds.getMinX()) / 2, midPoint.x);
        assertEquals((bounds.getMaxY() + bounds.getMinY()) / 2, midPoint.y);
    }

    private void assertExpectedBoundaries(BoundingBox boundingBox, int expectedMinX,
            int expectedMaxX, int expectedMinY, int expectedMaxY) {
        assertEquals(expectedMinX, boundingBox.getMinX());
        assertEquals(expectedMaxX, boundingBox.getMaxX());
        assertEquals(expectedMinY, boundingBox.getMinY());
        assertEquals(expectedMaxY, boundingBox.getMaxY());
    }
}
