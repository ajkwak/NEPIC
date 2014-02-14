package nepic.data;

import static org.junit.Assert.assertEquals;
import static nepic.testing.util.Assertions.*;

import java.util.List;

import nepic.util.Range;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * JUnit tests for {@link Histogram}.
 * 
 * @author AJ Parmidge
 */
public class HistogramTest {
    private final List<Integer> data = Lists.newArrayList(
            -5, 1, 1, 2, 2, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8,
            9, 9, 10, 10, 10, 11, 12, 12, 13, 13, 13, 14, 15, 16, 17, 17, 23, 23, 24, 25, 25);
    private Histogram histogram;

    @Before
    public void setUp() {
        histogram = new Histogram.Builder(-13, 52).addValues(data).build();
    }

    @Test
    public void getRange() {
        Range histRange = histogram.getRange();
        assertEquals(-5, histRange.min);
        assertEquals(25, histRange.max);
    }

    @Test
    public void getMagnitudeAt() {
        assertEquals(1, histogram.getMagnitudeAt(-5));
        assertEquals(8, histogram.getMagnitudeAt(6));
        assertEquals(8, histogram.getMagnitudeAt(6));
        assertEquals(3, histogram.getMagnitudeAt(10));
        assertEquals(1, histogram.getMagnitudeAt(14));
        assertEquals(2, histogram.getMagnitudeAt(25));
    }

    @Test
    public void getMax() {
        assertEquals(25, histogram.getMax());
    }

    @Test
    public void getMean() {
        assertEquals(9.52, histogram.getMean(), 0.001);
    }

    @Test
    public void getMedian() {
        assertEquals(8, histogram.getMedian());
    }

    @Test
    public void getMin() {
        assertEquals(-5, histogram.getMin());
    }

    @Test
    public void getModes() {
        List<Integer> modes = histogram.getModes();
        assertContains(6, modes);
        assertContains(8, modes);
    }

    @Test
    public void getNumberModeInstances() {
        assertEquals(8, histogram.getNumberModeInstances());
    }

    @Test
    public void getNumValues() {
        assertEquals(50, histogram.getNumValues());
    }

    @Test
    public void getNumValuesBetween_succeeds() {
        assertEquals(0, histogram.getNumValuesBetween(-50, -45));
        assertEquals(0, histogram.getNumValuesBetween(45, 50));
        assertEquals(50, histogram.getNumValuesBetween(-50, 45));
        assertEquals(1, histogram.getNumValuesBetween(-5, 0));
        assertEquals(29, histogram.getNumValuesBetween(6, 13));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNumValuesBetween_illegalBounds_throws() {
        histogram.getNumValuesBetween(50, 45); // min > max
    }

    @Test
    public void getPercentile() {
        assertEquals(-5, histogram.getPercentile(0));
        assertEquals(-5, histogram.getPercentile(1));
        assertEquals(1, histogram.getPercentile(2));
        assertEquals(24, histogram.getPercentile(95));
        assertEquals(25, histogram.getPercentile(96));
        assertEquals(25, histogram.getPercentile(99));
        assertEquals(25, histogram.getPercentile(100));
    }

    @Test
    public void getStDev() {
        assertEquals(6.469, histogram.getStDev(), 0.001);
    }

    @Test
    public void getVariance() {
        // Checked using http://www.mathsisfun.com/data/standard-deviation-calculator.html
        assertEquals(41.847, histogram.getVariance(), 0.001);
    }

}
