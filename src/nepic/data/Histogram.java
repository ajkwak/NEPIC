package nepic.data;

import nepic.io.Label;
import nepic.util.CsvFormattable;
import nepic.util.Verify;

/**
 * A class representing a histogram of single-dimensional data within a given range of values.
 *
 * <p>
 * Note: This class is optimized for sets of single-dimensional data with relatively small domains
 * (the number of values in the data set is immaterial in the final {@link Histogram}).
 *
 * @author AJ Parmidge
 */
public class Histogram implements CsvFormattable {
    /**
     * The actual histogram data.
     */
    private final int[] hist;
    /**
     * The number of values in this {@link Histogram}.
     */
    private final int n;
    /**
     * The difference between the actual lower bound of the histogram's domain and zero (relates the
     * actual histogram domain to the positions in the hist array).
     */
    private final int offset;
    /**
     * The sum of the magnitudes of all columns in the histogram.
     */
    private final int sum;

    /**
     * Creates a histogram with the given information passed from the {@link Builder}.
     *
     * @param hist the actual data for the {@link Histogram}
     * @param n the number of values included in the histogram data
     * @param offset the offset of the minimum value in the histogram's actual range from zero
     * @param sum the sum of all values in the given histogram data
     * @param minPos the minimum position in the histogram data at which there is at least one value
     * @param maxPos the maximum position in the histogram data at which there is at least one value
     */
    private Histogram(int[] hist, int n, int offset, int sum, int minPos, int maxPos) {
        Verify.argument(minPos >= maxPos, "Invalid bounds given.  maxPos (= " + maxPos
                + ") < minPos (= " + minPos + ")");
        this.n = n;
        this.offset = offset;
        this.sum = sum;

        // Make a defensive copy of the Histogram in case the Builder is modified after this
        // Histogram is made.
        this.hist = new int[maxPos - minPos + 1];
        for (int pos = minPos; pos <= maxPos; pos++) {
            this.hist[pos - minPos] = hist[pos];
        }
    }

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
        double mean = getMean();
        double stDev = getStDev();
        return new Object[] {
                getNumValues(),
                getMin(),
                getMax(),
                getMedian(),
                getMode(),
                mean,
                stDev,
                getNumValuesBetween((int) Math.ceil(mean - stDev), (int) Math.floor(mean + stDev)),
                getNumValuesBetween(
                        (int) Math.ceil(mean - 2 * stDev), (int) Math.floor(mean + 2 * stDev)),
                getNumValuesBetween(
                        (int) Math.ceil(mean - 2 * stDev), (int) Math.floor(mean + 2 * stDev)) };
    }


    /**
     * Get the size of the domain of this histogram (the range of values over which this histogram
     * has data).
     */
    public int getDomainSize() {
        return hist.length;
    }

    /**
     * Gets the magnitude of the column in this {@link Histogram} with the given value.
     *
     * @param value the value of the column for which to get the magnitude
     * @return the magnitude of the specified column
     */
    public int getMagnitudeAt(int value) {
        int pos = value - offset;
        if (pos < 0 || pos >= hist.length) { // If the given value is outside the Histogram's
                                             // bounds.
            return 0;
        }
        return hist[pos];
    }

    /**
     * Gets the maximum value of the data in this {@link Histogram}.
     */
    public int getMax() {
        return offset + hist.length - 1;
    }

    /**
     * Gets the mean of the data in this {@link Histogram}.
     */
    public double getMean() {
        return ((double) sum) / n + offset;
    }

    /**
     * Gets the median of the data in this {@link Histogram}.
     */
    public int getMedian() {
        return getPercentile(50);
    }

    /**
     * Gets the minimum value of the data in this {@link Histogram}.
     */
    public int getMin() {
        return offset;
    }

    /**
     * Gets the mode of the data in this {@link Histogram}.
     */
    public int getMode() {
        int modePos = 0;
        int modeMagnitude = hist[0];
        for (int pos = 1; pos < hist.length; pos++) {
            int magnitude = hist[pos];
            if (magnitude > modeMagnitude) {
                modeMagnitude = magnitude;
                modePos = pos;
            }
        }
        return modePos + offset;
    }

    /**
     * Gets the number of data points that this {@link Histogram} represents.
     */
    public int getNumValues() {
        return n;
    }

    /**
     * Gets the magnitudes of all of all columns in the {@link Histogram} between the given
     * {@code floor} and {@code ceiling} inclusively.
     *
     * @param floor the lower bound of the range
     * @param ceiling the upper bound of the range
     * @return the sum of all column magnitudes inclusively contained within the given domain
     */
    public int getNumValuesBetween(int floor, int ceiling) {
        Verify.argument(floor <= ceiling,
                "Given floor (=" + floor + ") > given ceiling (= " + ceiling + ")");
        int floorPos = floor - offset;
        int ceilingPos = ceiling - offset;

        if(ceilingPos < 0 || floorPos >= hist.length){
            // If the given values are outside the bounds of this histogram.
            return 0;
        }

        // Bound the given range to hist.
        floorPos = Math.max(floorPos, 0);
        ceilingPos = Math.min(ceilingPos, hist.length - 1);

        // Get the magnitude of each column between the floor and ceiling inclusively.
        int numValues = 0;
        for (int pos = floorPos; pos <= ceilingPos; pos++) {
            numValues += hist[pos];
        }
        return numValues;
    }

    /**
     * Gets the value of the given percentile of this {@link Histogram}'s data. For example,
     * <ul>
     * <li>The 0<sup>th</sup> percentile is the minimum value in the histogram.</li>
     * <li>The 50<sup>th</sup> percentile is the median value in the histogram.</li>
     * <li>The 100<sup>th</sup> percentile is the maximum value in the histogram</li>
     * </ul>
     *
     * @param percentile the percentile to get (must be between 0 and 100, inclusive
     * @return the value of the given percentile of the histogram data
     */
    public int getPercentile(double percentile) {
        Verify.argument(percentile >= 0 && percentile <= 100, "Illegal percentile value "
                + percentile + ".  Percentiles MUST be between 0 and 100 (inclusive).");
        long elPos = Math.round(n * percentile / 100);
        int numPassed = 0;
        int elVal = 0;
        while (numPassed < elPos) {
            numPassed += hist[elVal];
            elVal++;
        }// while
        return elVal + offset;
    }

    /**
     * Gets the standard deviation of the data in this {@link Histogram}.
     */
    public double getStDev() {
        return Math.sqrt(getVariance());
    }

    /**
     * Gets the variance of the data in this {@link Histogram}.
     */
    public double getVariance() {
        double var = 0;
        double mean = getMean() - offset;
        for (int pos = 0; pos <= hist.length; pos++) {
            double diff = pos - mean;
            var += hist[pos] * (diff * diff); // take sum of square of diffs
        }
        return var / (n - 1); // Variance for sample (for entire population, divide by n, not n-1)
    }

    @Override
    public String toString() {
        return "Hist (n = " + n + ", range " + getMin() + "-" + getMax() + "): med = "
                + getMedian() + ", mode = " + getMode() + ", mean = " + getMean() + " +/- "
                + getStDev() + " stDev";
    }

    /**
     * The mutable builder for the immutable {@link Histogram} object.
     *
     * @author AJ Parmidge
     */
    public static class Builder implements nepic.util.Builder<Histogram> {
        private final int[] histogram;
        private final int offset;

        private int n = 0;
        private int sum = 0;
        /**
         * The position of the minimum value in the histogram array.
         */
        private int minPos = Integer.MAX_VALUE;
        /**
         * The position of the maximum value in the histogram array.
         */
        private int maxPos = Integer.MIN_VALUE;

        /**
         * Creates an object that builds a {@link Histogram} with the given bounds.
         *
         * @param lowerBound the lower bound of the domain over which the built {@link Histogram}
         *        must extend.
         * @param upperBound the upper bound of the domain over which the built {@link Histogram}
         *        must extend.
         */
        public Builder(int lowerBound, int upperBound) {
            Verify.argument(lowerBound <= upperBound,
                    "Lower bound greater than upper bound!  Given lowerBound = " + lowerBound
                            + ", upperBound = " + upperBound);
            this.histogram = new int[upperBound - lowerBound + 1];
            this.offset = lowerBound;
        }

        /**
         * Add the given values to the {@link Histogram} being built.
         *
         * @param values the values to add
         * @return {@code this}, for chaining
         */
        public Histogram.Builder addValues(int... values) {
            Verify.notNull(values, "values");
            for (int value : values) {
                addValue(value);
            }
            return this;
        }

        /**
         * Add the given values to the {@link Histogram} being built.
         *
         * @param values the values to add
         * @return {@code this}, for chaining
         */
        public Histogram.Builder addValues(Iterable<Integer> values) {
            Verify.notNull(values, "values");
            for (int value : values) {
                addValue(value);
            }
            return this;
        }

        @Override
        public Histogram build() {
            Verify.state(minPos <= maxPos, "Cannot instantiate an empty Histogram");
            return new Histogram(histogram, n, offset, sum, minPos, maxPos);
        }

        /**
         * Add the given value to the {@link Histogram} being built.
         *
         * @param value the value to add
         */
        private void addValue(int value) {
            int pos = value - offset; // The position of 'value' in the histogram matrix.
            Verify.argument(pos >= 0 && pos < histogram.length, "Cannot add illegal value " + value
                    + " to to the Histogram being built.  Acceptable values range from " + offset
                    + " to " + (offset + histogram.length - 1));
            histogram[pos]++;
            n++;
            sum += value; // TODO: this varies from previous version of Histogram
            if (pos < minPos) {
                minPos = pos;
            }
            if (pos > maxPos) {
                maxPos = pos;
            }
        }
    }
}
