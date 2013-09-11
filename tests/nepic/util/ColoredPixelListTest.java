package nepic.util;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class ColoredPixelListTest {
    private int color = 0x123456;

    @Test(expected = NullPointerException.class)
    public void constructor_NullPixelList_Fails() {
        new ColoredPointList(null, color);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_EmptyPixelList_Fails() {
        List<Point> emptyList = new LinkedList<Point>();
        assertTrue("Empty list is not empty.", emptyList.isEmpty());
        new ColoredPointList(emptyList, color);
    }

    @Test
    public void constructor_NonEmptyPixelList_Succeeds() {
        List<Point> nonEmptyList = Lists.newArrayList(new Point(0, 0));
        assertFalse("List is empty!", nonEmptyList.isEmpty());
        ColoredPointList pixelList = new ColoredPointList(nonEmptyList, color);

        // Verify that the list in the ColoredPixelList is the same as the parameter list
        assertEquals(nonEmptyList, pixelList.points);

        // Verify that the ColoredPixelList's color is the same as the parameter color
        assertEquals(color, pixelList.rgb);
    }
}
