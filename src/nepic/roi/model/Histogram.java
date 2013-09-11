package nepic.roi.model;

import nepic.io.Label;
import nepic.util.CsvFormattable;
import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since
 * @version Nepic_Alpha_v1-1-2013-03-13
 * 
 */
public class Histogram implements CsvFormattable { // TODO: improve, so have private / public
                                                   // methods for everything, so don't have to
                                                   // constantly switch back and forth because of
                                                   // offset
    private final int[] hist;
    private int n = 0; // num data points
    private final int offset;

    // Calculated Histogram information
    private int sum = 0;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;

    public Histogram(int size, int offset) {
        Verify.argument(size > 0, "Histogram size must be positive.");
        hist = new int[size];
        this.offset = offset;
    }

    public static Histogram newPixelIntensityHistogram() {
        return new Histogram(256, 0);
    }

    public static Histogram newEdgeHistogram() {
        return new Histogram(511, -255);
    }

    public Histogram addData(Iterable<Integer> data) {
        Verify.notNull(data, "Data to add cannot be null!");
        for (int datum : data) {
            addDataPoint(datum);
        }
        return this;
    }

    public Histogram addData(int... data) {
        Verify.notNull(data, "Data to add cannot be null!");
        for (int i = 0; i < data.length; i++) {
            addDataPoint(data[i]);
        }
        return this;
    }

    private void addDataPoint(int datum) {
        int datumPos = datum - offset; // convert to position in hist[] matrix
        Verify.argument(datumPos >= 0 && datumPos < hist.length, "Illegal datum (value = " + datum
                + ") added to hist.  Datum must be between " + offset + " and "
                + (offset + hist.length - 1));
        hist[datumPos]++;
        sum += datumPos;
        n++;
        if (datumPos < min) {
            min = datumPos;
        }
        if (datumPos > max) {
            max = datumPos;
        }
    }

    public int legalizeDatumVal(int datum) {
        if (datum < offset) {
            return offset;
        }
        int maxVal = offset + hist.length - 1;
        if (datum > maxVal) {
            return maxVal;
        }
        return datum;
    }

    public int getSize() {
        return hist.length;
    }

    public int getOffset() {
        return offset;
    }

    public int getLowerBound() {
        return offset;
    }

    public int getUpperBound() {
        return offset + hist.length - 1;
    }

    public double getAverage() {
        return ((double) sum) / n + offset;
    }

    public int getValue(int percentile) {
        Verify.argument(percentile >= 0 && percentile <= 100);
        int elPos = n * percentile / 100;
        int numPassed = 0;
        int elVal = min;
        while (numPassed < elPos) {
            numPassed += hist[elVal];
            elVal++;
        }// while
        return elVal + offset;
    }

    public int getMedian() {
        return getValue(50);
    }

    public int getMode() {
        int mode = min;
        int numAtMode = hist[min];
        for (int i = min + 1; i <= max; i++) {
            int numAt = hist[i];
            if (numAt > numAtMode) {
                numAtMode = numAt;
                mode = i;
            }
        }
        return mode + offset;
    }

    public int getNumDataPoints() {
        return n;
    }

    public int getMin() {
        return min + offset;
    }

    public int getMax() {
        return max + offset;
    }

    public double getVariance() {
        double var = 0;
        double mean = getAverage() - offset;
        for (int i = min; i <= max; i++) {
            double diff = i - mean;
            var += hist[i] * (diff * diff); // take sum of square of diffs
        }
        return var / (n - 1); // Variance for sample (for entire population, divide by n, not n-1)
    }

    public double getStDev() {
        return Math.sqrt(getVariance());
    }

    /**
     * 
     * @param pi the pixel intensity for which to get the number of pixels
     * @return the number of pixels in the image section at the provided pixel intensity
     * @throws ArrayIndexOutOfBoundsException
     */
    public int numDataAt(int pi) {
        pi -= offset;
        return hist[pi];
    }

    // Finds num data between given floor and ceiling INCLUSIVELY
    public int numDataBetween(int floor, int ceiling) {
        floor -= offset;
        ceiling -= offset;

        if (floor < min) {
            floor = min;
        }
        if (ceiling > max) {
            ceiling = max;
        }
        int numData = 0;
        for (int i = floor; i <= ceiling; i++) {
            numData += numDataAt(i);
        }
        return numData;
    }

    // Finds num data between given floor and ceiling INCLUSIVELY
    public int numDataBetween(double start, double end) {
        int floor = (int) Math.ceil(start);
        int ceiling = (int) Math.floor(end);

        int min = getMin();
        if (floor < min) {
            floor = min;
        }
        int max = getMax();
        if (ceiling > max) {
            ceiling = max;
        }
        int numData = 0;
        for (int i = floor; i <= ceiling; i++) {
            numData += numDataAt(i);
        }
        return numData;
    }

    @Override
    public String toString() {
        double avg = getAverage();
        double stDev = getStDev();
        return "Hist (n = " + n + ", range " + min + "-" + max + "): \n\tmed = " + getMedian()
                + "\n\tmode = " + getMode() + "\n\tmean = " + avg + " +/- " + stDev + " stDev";
    }

    // public static String labelCsvFormat() {
    // return "n,min,max,med,mode,mean,stDev,% within 1 stDev,% withing 2 stDev,% within 3 stDev";
    // }

    public static Label[] getCsvLabels() {
        return new Label[] {
                new Label("n"),
                new Label("min"),
                new Label("max"),
                new Label("med"),
                new Label("mode"),
                new Label("mean"),
                new Label("stDev"),
                new Label("within_1_stDev"),
                new Label("within_2_stDev"),
                new Label("within_3_stDev"), };
    }

    @Override
    public Object[] getCsvData() {
        double avg = getAverage();
        double stDev = getStDev();
        return new Object[] {
                getNumDataPoints(),
                getMin(),
                getMax(),
                getMedian(),
                getMode(),
                avg,
                stDev,
                numDataBetween(avg - stDev, avg + stDev),
                numDataBetween(avg - 2 * stDev, avg + 2 * stDev),
                numDataBetween(avg - 3 * stDev, avg + 3 * stDev) };
    }

    // @Override
    // public String recordCsvFormat() {
    // double avg = getAverage();
    // double stDev = getStDev();
    // return new StringBuilder().append(n).append(",").append(min).append(",").append(max)
    // .append(",").append(getMedian()).append(",").append(getMode()).append(",").append(
    // avg).append(",").append(stDev).append(",")
    // .append(percentWithin(avg, stDev)).append(",")
    // .append(percentWithin(avg, 2 * stDev)).append(",").append(
    // percentWithin(avg, 3 * stDev)).toString();
    // }

}
