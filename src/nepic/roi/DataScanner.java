package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JOptionPane;

import com.google.common.collect.Lists;

import nepic.gui.Graph;
import nepic.data.GraphData;
import nepic.data.HistogramPositionMap;
import nepic.util.Range;
import nepic.util.Verify;

public class DataScanner {
    private final GraphData data = new GraphData();
    List<Integer> rawData;
    BucketizedDataSet bucketSet;

    public DataScanner(Collection<Integer> rawData) {
        Verify.nonEmpty(rawData, "rawData");
        processRawData(rawData);
    }

    private void processRawData(Collection<Integer> rawData) {
        // Look through the actual raw data.
        copyRawData(rawData);
        graphRawData();

        // Bucketize the raw data (does the initial smoothing / simplifying of the data)
        bucketSet = new BucketizedDataSet(rawData);
        // graphBucketizedData();

        // Process the bucketized data.
        processBucketizedData();
        graphProcessedData();

        graphMinPi();
    }

    public List<Integer> getProcessedData() { // TODO: this is a hack!
        int numBuckets = bucketSet.size();
        List<Integer> processedData = new ArrayList<Integer>(numBuckets);
        for (int i = 0; i < numBuckets; i++) {
            processedData.add(bucketSet.getPI(i));
        }
        return processedData;
    }

    private void copyRawData(Collection<Integer> rawData) {
        this.rawData = new ArrayList<Integer>(rawData.size());
        for (int datum : rawData) {
            this.rawData.add(datum);
        }
    }

    /**
     * Adds the given raw data to this class's {@link GraphData} object.
     *
     * @param rawData the raw data to graph
     */
    private void graphRawData() {
        List<Point> rawDataPoints = new ArrayList<Point>(rawData.size());
        int idx = 0;
        for (int datum : rawData) {
            // System.out.print(datum + ", ");
            rawDataPoints.add(new Point(idx, datum));
            idx++;
        }
        data.setDataSet("01 Raw Data", rawDataPoints, 0x00cc00 /* lavender */);
        // System.out.println();
    }

    private void graphMinPi() {
        List<Point> minPiPoints = Lists.newArrayList(new Point(data.getMinX(), 19), new Point(data
                .getMaxX(), 19));
        data.setDataSet("06 MinPi", minPiPoints, 0xffaa00);
    }

    /**
     * Gets the {@link Iterator} of the given {@link Iterable}, starting at the given position.
     *
     * @param iterable the iterabble for which to return an iterator
     * @param pos the position in the given iterable at which the returned iterator should start
     * @return the iterator of the given iterable, starting at the specified position
     * @throws NoSuchElementException if the given position is after the last element in the
     *         iterable
     */
    private <E> Iterator<E> getIteratorStartingAtPos(Iterable<E> iterable, int pos) {
        Iterator<E> itr = iterable.iterator();
        for (int i = 0; i < pos - 1; i++) {
            itr.next();
        }
        return itr;
    }

    private void graphBucketizedData() {
        int numBuckets = bucketSet.size();
        ArrayList<Point> bucketDataSet1 = new ArrayList<Point>(numBuckets / 2);
        ArrayList<Point> bucketDataSet2 = new ArrayList<Point>(numBuckets / 2 + numBuckets % 2);
        for(int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++){
            Range bucketDomain = bucketSet.getDomainForBucket(bucketIdx);
            int bucketPI = bucketSet.getPI(bucketIdx);
            if (bucketIdx % 2 == 0) {
                bucketDataSet1.add(new Point(bucketDomain.min, bucketPI));
                bucketDataSet1.add(new Point(bucketDomain.max, bucketPI));
            } else {
                bucketDataSet2.add(new Point(bucketDomain.min, bucketPI));
                bucketDataSet2.add(new Point(bucketDomain.max, bucketPI));
            }
        }
        if (!bucketDataSet1.isEmpty()) {
            data.setDataSet("02 Bucket Set 1", bucketDataSet1, 0x8800ff /* Dark Green */);
        }
        if (!bucketDataSet2.isEmpty()) {
            data.setDataSet("03 Bucket Set 2", bucketDataSet2, 0x8800ff /* Green */);
        }
    }

    private void processBucketizedData() {
        int prevPI = -1; // Initialize as invalid.
        int trendLength = 0;
        int trendDelta = 0;
        int startPos = 0;
        for (int pos = startPos; pos < bucketSet.size(); pos++) {
            int currPI = bucketSet.getPI(pos);
            if (prevPI > -1) { // If prevPI is valid.
                int delta = currPI - prevPI;
                if (delta != 0 && delta * trendDelta >= 0) { // Continuing current trend.
                    trendDelta += delta;
                    trendLength++;
                } else {
                    // First determine if the current trend is significant.
                    if (trendLength > 4 || Math.abs(trendDelta) >= 6) { // Is significant trend.
                        // TODO: now separate this trend from preceding trends/flat regions
                        // System.out.println("Trend: " + (pos - trendLength) + "-" + pos + " (PI "
                        // + bucketSet.getPI(pos - trendLength) + "-" + bucketSet.getPI(pos)
                        // + ")");
                        processFlatRegion(bucketSet, startPos, pos - trendLength); // TODO: correct?
                        startPos = pos + 1;
                    }

                    // Begin a new trend.
                    if (delta == 0) {
                        trendDelta = 0;
                        trendLength = 0;
                    }else{
                        trendDelta = delta;
                        trendLength = 1;
                    }
                }
            }
            prevPI = currPI;
        }
        processFlatRegion(bucketSet, startPos, bucketSet.size() - 1);
    }

    private void graphProcessedData() {
        // Graph the first bucketSet
        int numBuckets = bucketSet.size();
        // System.out.println("bucketSet length = " + numBuckets);
        ArrayList<Point> processedBucketSet = new ArrayList<Point>(numBuckets);
        for (int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++) {
            int bucketPos = bucketSet.getCenterPositionForBucket(bucketIdx);
            int bucketPI = bucketSet.getPI(bucketIdx);
            processedBucketSet.add(new Point(bucketPos, bucketPI));
        }
        if (!processedBucketSet.isEmpty()) {
            data.setDataSet("04 Processed Bucket Set", processedBucketSet, 0xff0000 /* Red */);
        }
    }

    private void processRisingRegion(int startPos, int endPos) {

    }

    private void processFallingRegion(int startPos, int endPos) {

    }

    private void processFlatRegion(BucketizedDataSet bucketSet, int startPos, int endPos) {
        // ALGORITHM:
        // Go for the first five values in the flat region. Find the mode of those 5. Continue from
        // the first time that mode appears to the last time the mode appears. Then begin again with
        // new mode. The mode must ALWAYS appear within 4 of the last appearance of the mode.
        // System.out.println("FLAT: " + startPos + "-" + endPos);
        int pos = startPos;
        while (pos <= endPos) {
            pos = flattenSingleModeRegion(bucketSet, pos, endPos);
            // System.out.println("Final position after flattening single mode region = " + pos);
        }
    }

    /**
     *
     * @param startPos
     * @param maxEndPos
     * @return the last position in this single mode region.
     */
    private int flattenSingleModeRegion(BucketizedDataSet bucketSet, int startPos, int maxEndPos) {
        HistogramPositionMap histogram = new HistogramPositionMap(0, 255);

        // Determine the initial mode.
        int pos;
        for (pos = startPos; pos <= Math.min(startPos + 3, maxEndPos); pos++) {
            Range halfBucketRange = bucketSet.getRangeForHalfBucket(pos);
            histogram.addRangeAtPosition(halfBucketRange, pos);
        }

        // Continue until the last time the mode is crossed within range.
        // Only keep going if 1+ of these are/cross the mode. (don't add these to the
        // HistogramPositionMap UNLESS 1+ of them are/cross the mode)
        int maxQueueSize = 4;
        Queue<Range> piBuffer = new ArrayBlockingQueue<Range>(maxQueueSize);
        int queueSize = 0;
        for (int trialPos = pos; trialPos <= maxEndPos; trialPos++) {
            if (queueSize == maxQueueSize) {
                break; // Now just need to combine the region into one mode.
            } else {
                // Determine if current PI matches any of the current modes.
                Range currPiRange = bucketSet.getRangeForHalfBucket(trialPos);
                piBuffer.add(currPiRange);
                queueSize++;
                if (histogram.currentModeWithinRange(currPiRange)) {
                    // System.out.print(" value matches current mode. Add ");
                    for (int i = 0; i < queueSize; i++) {
                        currPiRange = piBuffer.remove();
                        histogram.addRangeAtPosition(currPiRange, pos);
                        pos++;
                    }
                    queueSize = 0;
                }
            }
        }

        int mode = (int) Math.round(histogram.getMode());
        // TODO: this next piece should be altered (DO NOT do it in place)
        for (int replacePos = histogram.getFirstModePosition(); replacePos < pos; replacePos++) {
            bucketSet.setPI(replacePos, mode);
        }

        return pos; // Returns the last position in this single mode region
    }

    private int getNumberRawDataCrossings(int pi, int startPos, int endPos) {
        int prevDelta = 0;
        int numCrossings = 0;
        for (int pos = startPos; pos <= endPos; pos++) {
            int currDelta = rawData.get(pos) - pi;
            if (currDelta != 0) {
                if (currDelta * prevDelta < 0) {
                    numCrossings++;
                }
                prevDelta = currDelta;
            }
        }
        return numCrossings;
    }

    /**
     * Returns 2 * the median of the two given equal-length sorted lists. Returns twice the median
     * of the lists, because the actual median may be half way between two integers.
     *
     * <p>
     * For Example: Given the sorted lists {@code a = [-13, -5, 2, 3]} and {@code b = [0, 1, 5, 6]}:
     *
     * <p>
     * The overall sorted values of the two lists would be: {@code [-13, -5, 0, 1, 2, 3, 5, 6]} This
     * means that the two medians of the overall list are: 1 and 2 Thus, the overall median of the
     * two sorted lists is 1.5
     *
     * <p>
     * However, this value is not an integer, and so cannot be returned by the method. Thus, the
     * method would in this case return the value 3 (double the actual median, allowing the median
     * to be expressed as an integer.
     *
     * @param sortedList1 the first list of sorted integers (of length <i>l</i>)
     * @param sortedList2 the second list of sorted integers (also of length <i>l</i>)
     * @return 2 * the median of the two sorted lists
     */
    private static int getDblMedian(int[] sortedList1, int[] sortedList2) {
        Verify.argument(sortedList1.length == sortedList2.length,
                "length of first list != length of second list");
        int secondMedPos = sortedList1.length;

        int pos1 = 0; // pos in list 1
        int pos2 = 0; // pos in list 2

        int med1 = -1; // invalid

        while (pos1 + pos2 < secondMedPos) {
            int elAtPos1 = sortedList1[pos1];
            int elAtPos2 = sortedList2[pos2];
            if (elAtPos1 < elAtPos2) {
                med1 = elAtPos1;
                pos1++;
            } else {
                med1 = elAtPos2;
                pos2++;
            }
        }
        // System.out.println("med1 = " + med1);
        int med2;
        if (pos1 >= sortedList1.length) {
            med2 = sortedList2[pos2];
        } else if (pos2 >= sortedList2.length) {
            med2 = sortedList1[pos1];
        } else {
            med2 = Math.min(sortedList1[pos1], sortedList2[pos2]);
        }
        return med1 + med2;
        // System.out.println("med2 = " + med2);
    }

    public GraphData getGraphData() {
        return data;
    }

    private static List<Integer> data1 = Lists.newArrayList(
            43, 44, 39, 47, 42, 44, 44, 45, 45, 45, 46, 44, 45, 42, 40, 42, 42, 44, 45, 42, 45,
            45, 44, 43, 44, 43, 42, 40, 43, 43, 47, 44, 44, 44, 44, 46, 46, 48, 44, 48, 50, 47,
            42, 43, 45, 42, 43, 45, 44, 44, 42, 39, 42, 40, 39, 39, 45, 45, 43, 46, 45, 45, 40,
            42, 39, 44, 46, 45, 45, 47, 44, 44, 43, 45, 43, 46, 47, 48, 42, 47, 48, 46, 45, 44,
            49, 43, 48, 48, 46, 48, 45, 47, 49, 49, 45, 45, 49, 47, 48, 49, 48, 50, 47, 49, 47,
            50, 48, 52, 54, 61, 69, 83, 89, 88, 78, 71, 57, 61, 57, 56, 54, 55, 57, 58, 58, 54,
            64, 61, 61, 61, 58, 60, 60, 62, 64, 62, 64, 70, 70, 70, 74, 73, 78, 80, 80, 86, 92,
            105, 114, 123, 147, 160, 179, 198, 199, 232, 246, 249, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 227, 195, 178, 139, 113, 90, 82, 70, 60, 51, 49, 51,
            52, 51, 45, 46, 46, 46, 47, 50, 45, 45, 44, 44, 43, 46, 46, 43, 42, 43, 42, 40, 42,
            43, 40, 39, 38, 39, 37, 42, 40, 39, 41, 36, 39, 44, 40, 40, 40, 40, 42, 37, 41, 37,
            40, 35, 39, 37, 42, 38, 37, 39, 40, 39, 38, 38, 42, 42, 43, 42, 41, 43, 42, 42, 39,
            43, 42, 48, 45, 45, 45, 42, 44, 43, 47, 45, 42, 48, 46, 47, 45, 51, 43, 45, 48, 47,
            48, 50, 50, 50, 47, 52, 48, 46, 45, 47, 44, 46, 47, 44, 46, 47, 46, 43, 42, 45, 44,
            44, 42, 43, 41, 50, 43, 45, 42, 43, 45, 42, 43, 44, 39, 40, 45, 45, 44, 40, 41, 40,
            40, 38, 42, 42, 41, 40, 41, 40, 41, 44, 43, 40, 40, 48, 41, 46, 43, 47, 46, 49, 49,
            46, 51, 58, 55, 52, 49, 51, 47, 47, 46, 40, 42, 44, 46, 44, 44, 46, 42, 48, 41, 43,
            44, 47, 43, 45, 45, 43, 47, 43, 47, 50, 50, 54, 50, 54, 57, 61, 67, 67, 71, 75, 75,
            74, 64, 57, 52, 51, 48, 47, 44, 45, 46, 44, 43, 45, 45, 45, 42, 48, 43, 47, 42, 45,
            43, 44, 44, 45, 52, 53, 52, 61, 71, 69, 84, 93, 103, 96, 91, 71, 62, 54, 51, 51,
            50, 48, 43, 42, 43, 41, 42, 41, 40, 41, 42, 40, 42, 42, 37, 41, 41, 42, 41, 39, 39,
            41, 42, 41, 40, 40, 39, 42, 44, 41, 42, 38, 41, 38, 40, 41, 42, 38, 36, 40, 37, 36,
            38, 37, 39, 39, 39, 42, 40, 40, 38, 44, 42, 39, 41, 36, 35, 35, 42, 38, 37, 40, 40,
            41, 40, 41, 39, 38, 38, 42, 45, 41, 41, 40, 44, 40, 38, 42, 41, 43, 43, 48, 41, 41,
            42, 44, 46, 42, 40, 42, 43, 40, 44, 41, 37, 42, 38, 40, 44, 37, 40, 40, 41, 36, 37,
            42, 36, 42, 42, 37, 37, 38, 40, 36, 35, 38, 37, 35, 35, 34, 37, 39, 35, 35, 34, 37,
            37, 37, 31, 33, 34, 30, 33, 31, 33, 33, 31, 31, 28, 34, 30, 35, 34, 32, 32, 30, 30,
            34, 33, 35, 32, 35, 32, 29, 31, 32, 31, 33, 35, 30, 33, 34, 29, 31, 31, 32, 31, 31,
            31, 31, 32, 30, 31, 34, 34, 32, 30, 32, 32, 32, 32, 33, 28, 30, 32, 33, 32, 30, 29,
            32, 33, 32, 31, 32, 33, 31, 31, 28, 31, 33, 31, 32, 31, 30, 32, 32, 33, 30, 31, 31,
            30, 34, 31, 32, 31, 30, 31, 32, 30, 33, 31, 31, 29, 30, 31, 29, 29, 33, 31, 31, 30,
            28, 29, 33, 30, 35, 31, 37, 32, 33, 29, 36, 31, 31, 31, 32, 32, 34, 32, 31, 31, 33,
            35, 31, 31, 32, 28, 30, 33, 32, 29, 33, 33, 31, 31);

    private static final List<Integer> IMG_414_1_0DEG = Lists.newArrayList(
            7, 9, 9, 8, 9, 12, 10, 11, 9, 8, 8, 9, 12, 9, 11, 13, 9, 13, 9, 10, 10, 8, 9, 12, 10,
            11, 8, 11, 11, 9, 10, 13, 11, 10, 11, 12, 12, 10, 10, 10, 11, 13, 11, 8, 11, 11,
            12, 11, 11, 11, 13, 11, 14, 10, 11, 10, 13, 11, 12, 12, 11, 13, 11, 12, 13, 13,
            12, 15, 13, 12, 15, 14, 11, 16, 16, 11, 14, 16, 16, 15, 17, 18, 18, 18, 17, 15,
            14, 14, 16, 18, 14, 17, 21, 17, 14, 18, 17, 17, 16, 21, 18, 22, 21, 20, 19, 18,
            19, 21, 27, 24, 21, 20, 23, 23, 26, 23, 24, 23, 29, 28, 30, 30, 31, 33, 31, 34,
            32, 35, 35, 36, 39, 32, 34, 32, 32, 28, 27, 28, 27, 25, 24, 28, 22, 25, 23, 29,
            27, 25, 29, 27, 26, 27, 28, 27, 31, 28, 30, 30, 33, 30, 41, 32, 38, 36, 44, 45,
            47, 49, 53, 63, 60, 75, 76, 79, 81, 91, 97, 98, 97, 105, 113, 112, 123, 128,
            126, 126, 121, 143, 134, 136, 145, 151, 151, 155, 160, 167, 156, 161, 176,
            168, 184, 176, 168, 180, 177, 188, 174, 171, 175, 169, 166, 164, 161, 159,
            157, 163, 162, 154, 153, 162, 150, 156, 161, 152, 156, 158, 157, 148, 154,
            144, 143, 141, 143, 144, 150, 147, 148, 135, 144, 135, 130, 127, 123, 106,
            98, 90, 88, 81, 72, 77, 75, 62, 57, 55, 52, 45, 47, 47, 43, 43, 43, 37, 34, 37,
            39, 36, 32, 35, 32, 32, 37, 39, 40, 32, 36, 33, 38, 33, 36, 36, 29, 32, 29, 26,
            29, 28, 28, 29, 24, 24, 25, 25, 20, 22, 19, 19, 21, 18, 22, 23, 21, 21, 21, 19,
            19, 21, 20, 21, 24, 15, 17, 15, 16, 15, 15, 14, 18, 12, 15, 15, 16, 13, 15, 17,
            16, 16, 15, 18, 14, 13, 18, 12, 16, 15, 15, 16, 13, 13, 16, 13, 14, 13, 14, 14,
            13, 13, 14, 14, 14, 15, 13, 12, 10, 15, 13, 13, 12, 12, 11, 14, 14, 14, 11, 12,
            13, 12, 11, 9, 14, 10, 11, 12, 9, 10, 10, 13, 11, 13, 11, 9, 12, 11, 9, 12, 13,
            12, 11, 9, 10, 9, 11, 8, 12, 11, 10, 10, 9, 11, 9, 11, 9, 10, 9, 11, 12, 12, 9,
            10, 9, 10, 11, 9, 10, 11, 8, 9, 10, 10, 9, 9, 10, 10, 11, 8, 9, 8, 12, 11, 9, 9,
            11, 8, 6, 11, 8, 9, 9, 10, 11, 9, 12, 7, 8, 8, 7, 8, 8, 8, 8, 8, 10, 7, 9, 8, 8, 7, 8,
            8, 9, 8, 11, 8, 8, 8, 10, 9, 8, 8, 9, 8, 8, 8, 9, 10, 9, 9, 7, 6, 8, 8, 7, 9, 8, 7, 7,
            7, 8, 7, 7, 9, 7, 8, 7, 7, 9, 6, 9, 9, 7, 9, 5, 8, 7, 7, 9, 8, 8, 8, 8, 8, 8, 6, 7, 8,
            7, 6, 8, 6, 6, 8, 8, 7, 7, 7, 9, 9, 8, 7, 6, 8, 8, 8, 7, 7, 8, 7, 1, 8, 7, 7, 7, 8, 8,
            8, 8, 9, 7, 6, 8, 9, 7, 8, 7, 7, 6, 13, 7, 9);

    private static final List<Integer> IMG_000000_90DEG = Lists.newArrayList(
            13, 13, 14, 13, 13, 13, 12, 14, 12, 13, 12, 13, 16, 14, 12, 13, 12, 12, 15, 13, 15, 14,
            13, 14, 14, 12, 14, 15, 13, 14, 12, 12, 14, 13, 12, 13, 12, 12, 13, 12, 14, 12, 13, 14,
            12, 15, 14, 16, 12, 13, 14, 14, 14, 12, 14, 13, 14, 14, 13, 12, 14, 14, 13, 15, 12, 14,
            15, 13, 14, 13, 14, 14, 14, 13, 13, 14, 14, 12, 14, 13, 14, 14, 15, 13, 14, 14, 14, 14,
            14, 13, 13, 14, 12, 16, 13, 14, 13, 15, 15, 11, 15, 14, 12, 13, 14, 14, 13, 14, 15, 15,
            13, 11, 13, 14, 13, 13, 12, 13, 13, 13, 14, 15, 15, 14, 14, 12, 14, 14, 15, 15, 15, 16,
            15, 16, 15, 16, 14, 16, 16, 12, 15, 14, 15, 14, 12, 15, 16, 13, 14, 14, 15, 15, 16, 14,
            14, 14, 15, 14, 15, 14, 14, 15, 16, 14, 14, 15, 14, 13, 14, 14, 14, 13, 15, 14, 13, 15,
            25, 14, 14, 16, 14, 14, 11, 13, 15, 10, 14, 15, 13, 17, 16, 12, 15, 14, 15, 15, 18, 13,
            16, 15, 15, 16, 17, 14, 16, 13, 15, 15, 15, 14, 15, 14, 15, 13, 15, 14, 15, 16, 15, 14,
            14, 14, 15, 15, 16, 14, 18, 16, 14, 14, 17, 14, 14, 12, 15, 14, 17, 16, 16, 17, 14, 14,
            12, 15, 17, 13, 16, 18, 18, 17, 18, 17, 13, 15, 15, 16, 16, 16, 16, 16, 15, 14, 16, 16,
            16, 15, 16, 15, 13, 19, 14, 15, 15, 17, 14, 14, 18, 17, 17, 15, 17, 17, 16, 16, 17, 17,
            14, 16, 17, 16, 14, 14, 15, 16, 15, 15, 19, 16, 17, 14, 17, 15, 15, 15, 17, 17, 15, 16,
            16, 18, 17, 17, 16, 18, 16, 16, 17, 15, 19, 16, 16, 15, 14, 17, 15, 16, 16, 15, 16, 14,
            15, 15, 17, 16, 16, 18, 18, 14, 16, 17, 15, 14, 16, 16, 18, 13, 17, 16, 16, 18, 16, 16,
            17, 17, 17, 15, 16, 18, 15, 16, 15, 17, 15, 16, 15, 16, 17, 18, 15, 16, 13, 16, 16, 17,
            16, 15, 16, 15, 16, 16, 14, 16, 16, 19, 18, 18, 17, 18, 16, 17, 18, 19, 18, 15, 17, 14,
            10, 14, 16, 18, 15, 16, 17, 18, 15, 16, 18, 17, 17, 17, 15, 19, 17, 16, 15, 15, 17, 15,
            16, 16, 16, 18, 16, 15, 15, 16, 19, 16, 17, 15, 15, 16, 18, 17, 17, 16, 13, 17, 17, 18,
            19, 18, 17, 19, 20, 15, 17, 17, 17, 18, 18, 16, 16, 17, 18, 19, 19, 18, 16, 19, 16, 20,
            19, 16, 18, 20, 19, 22, 20, 18, 20, 19, 20, 21, 20, 18, 20, 17, 19, 19, 18, 20, 24, 21,
            21, 20, 19, 22, 21, 24, 24, 22, 19, 21, 23, 23, 22, 25, 20, 25, 22, 21, 25, 24, 25, 22,
            27, 26, 25, 21, 25, 25, 24, 30, 23, 25, 26, 28, 26, 23, 25, 28, 28, 26, 25, 28, 29, 26,
            27, 24, 25, 26, 27, 26, 20, 23, 23, 26, 22, 23, 23, 22, 20, 22, 21, 19, 14, 18, 19, 22,
            19, 19, 21, 17, 20, 18, 20, 20, 19, 19, 18, 16, 19, 18, 17, 17, 17, 18, 15, 18, 17, 17,
            16, 17, 15, 16, 17, 17, 20, 18, 15, 17, 16, 16, 16, 18, 15, 17, 16, 17, 19, 14, 14, 15,
            18, 16, 16, 17, 18, 19, 16, 18, 21, 16, 18, 16, 18, 17, 16, 16, 17, 16, 16, 17, 16, 16,
            17, 17, 18, 17, 14, 14, 16, 18, 19, 17, 17, 16, 14, 16, 18, 17, 17, 17, 16, 18, 15, 15,
            18, 17, 18, 16, 16, 15, 15, 16, 17, 15, 16, 14, 17, 16, 16, 15, 16, 15, 16, 15, 17, 14,
            16, 15, 14, 15, 16, 16, 15, 16, 16, 17, 17, 16, 16, 17, 16, 19, 16, 17, 15, 16, 16, 18,
            14, 15, 17, 14, 16, 15, 14, 16, 17, 17, 15, 16, 15, 16, 15, 14, 15, 17, 14, 15, 19, 15,
            17, 15, 17, 15, 16, 13, 15, 14, 15, 16, 15, 15, 16, 16, 15, 16, 14, 15, 16, 17, 17, 16,
            15, 14, 16, 15, 15, 14, 15, 16, 14, 16, 14, 14, 18, 15, 14, 16, 14, 15, 16, 15, 16, 16,
            14, 15, 16, 17, 14, 14, 14, 16, 14, 15, 15, 17, 14, 15, 17, 14, 15, 16, 23, 16, 15, 16,
            16, 14, 14, 15, 15, 15, 14, 16, 15, 15, 17, 13, 16, 13, 16, 18, 16, 15, 15, 14, 14, 16,
            16, 15, 14, 16, 14, 15, 14, 15, 15, 15, 13, 16, 14, 15, 16, 16, 16, 16, 15, 14, 15, 17,
            13, 15, 12, 14, 13, 16, 15, 16, 15, 15, 15, 15, 15, 14, 14, 16, 14, 15, 14, 17, 14, 13,
            14, 13, 15, 14, 15, 15, 13, 16, 14, 15, 14, 14, 15, 15, 13, 15, 15, 14, 14, 13, 15, 12,
            15, 13, 14, 14, 13, 14, 14, 13, 13, 14, 15, 13, 13, 18, 15, 14, 14, 14, 15, 13, 14, 16,
            14, 14, 15, 13, 13, 15, 14, 14, 14, 11, 15, 15, 13, 14, 14, 13, 15, 14, 14, 13, 15, 12,
            13, 12, 14, 15, 15, 15, 15, 15, 13, 13, 14, 14, 15, 14, 14, 14, 12, 12, 14, 13, 15, 14,
            14, 15, 13, 12, 12, 14, 14, 15, 13, 14, 13, 12, 13, 14, 15, 13, 14, 13, 12, 12, 14, 15,
            13, 15, 14, 14, 15, 15, 13, 12, 13, 13, 13, 13, 14, 14, 13, 12, 13, 12, 14, 13, 13, 12,
            14, 13, 12, 14, 12, 13, 13, 14, 13, 14, 13, 13, 14, 13, 13, 13, 14, 14, 13, 14, 14, 13,
            15, 12, 13, 12, 13, 12, 12, 12, 14, 12, 14, 13, 15, 13, 12, 13, 14, 14, 14, 16, 15, 11,
            13, 13, 13, 14, 12, 14, 14, 12, 15, 13, 13, 15, 13, 16, 13, 13, 13, 14, 13, 12, 14, 13,
            13, 13, 13, 13, 12, 14, 13, 13, 14, 13, 13, 13, 13, 13, 13, 13, 13, 13, 14, 12, 14, 11,
            13, 12, 13, 13, 15, 12, 14, 13, 13, 15, 13, 14, 13, 13, 12, 12, 12, 11, 13, 14, 13, 14,
            13, 13, 13, 13, 12, 13, 14, 13, 12, 13, 12, 12, 14, 14, 13, 13, 13, 13, 17, 14, 14, 13,
            14, 13, 13, 13, 12, 13, 14, 13, 12, 11, 13, 12, 13, 12, 13, 13, 14, 14, 13, 13, 12, 13,
            15, 14, 13, 13, 14, 13, 14, 13, 12, 11, 12, 14, 13, 12, 14, 12, 15, 12, 13, 11, 13, 12,
            14, 13, 12, 13, 12, 13, 12, 12, 13, 14, 13, 13, 14, 13, 13, 14, 11, 14, 13, 13, 15, 14,
            13, 12, 12, 13, 13, 13, 14, 15, 13, 12, 13, 14, 11, 12, 11, 14, 13, 13, 12, 12, 11, 14,
            14, 13, 12, 13, 12, 12, 11, 12, 13, 12, 12, 13, 13, 12, 12, 12, 13, 12, 13, 14, 13, 12,
            13, 13, 13, 14, 14, 14, 13, 13, 12, 12, 12, 13, 13, 13, 14, 13, 13, 13, 13, 14, 12, 14,
            13, 12, 13, 13, 14, 11, 14, 13, 12, 13, 12, 12, 15, 12, 12, 11, 12, 14, 12, 12, 13, 12,
            13, 13, 13, 13, 12, 12, 12, 13, 12, 13, 13, 13, 12, 12, 12, 14, 13, 13, 13, 11, 12, 13,
            13, 12, 14, 12, 11, 14, 15, 13, 12, 13, 11, 13, 14, 11, 12, 13, 12, 12, 13, 12, 11, 12,
            12, 11, 12, 11, 13, 12, 10, 13, 12, 12, 14, 12, 13, 13, 13, 14, 11, 12, 14, 13, 12, 13,
            14, 16, 12, 11, 13, 13, 10, 13, 12, 12, 13, 13, 11, 13, 13, 12, 12, 12, 12, 11, 14, 12,
            12, 12, 13, 13, 13, 12, 11, 12, 12, 12, 13, 11, 17, 11, 15, 12, 12, 12, 13, 12, 14, 12,
            12, 12, 11, 12, 12, 11, 12, 12, 13, 12, 13, 17, 13, 12, 13, 14, 12, 12, 12, 12, 15, 13,
            13, 13, 13, 13, 14, 13, 11, 12, 14, 13, 14, 12, 13, 12, 13, 13, 11, 12, 12, 12, 13, 11,
            12, 11, 12, 13, 13, 13, 12, 10, 12, 12, 13, 12, 12, 13, 11, 11, 12, 13, 15, 12, 12, 13,
            10, 10, 14, 12, 13, 12, 12, 14, 10, 13);

    private static final List<Integer> IMG_419_1_0DEG = Lists.newArrayList(11, 10, 13, 11, 11, 11,
            10, 12, 10, 10, 9, 10, 11, 10, 8, 10, 11, 11, 9, 11, 11, 10, 11, 12, 11, 11, 10, 11,
            10, 10, 11, 10, 10, 13, 12, 8, 9, 10, 9, 12, 12, 11, 10, 10, 12, 9, 10, 12, 11, 12, 10,
            11, 12, 11, 12, 11, 12, 11, 10, 10, 12, 9, 11, 10, 11, 10, 12, 11, 12, 9, 10, 10, 10,
            11, 9, 13, 11, 12, 12, 11, 13, 12, 10, 12, 12, 12, 11, 14, 12, 12, 12, 16, 14, 15, 16,
            15, 18, 19, 19, 17, 25, 23, 27, 27, 29, 31, 30, 28, 37, 33, 38, 27, 31, 38, 35, 37, 35,
            35, 35, 33, 37, 39, 37, 36, 32, 41, 38, 29, 36, 32, 36, 31, 33, 33, 31, 30, 32, 27, 27,
            29, 26, 27, 29, 25, 27, 29, 25, 28, 24, 25, 23, 22, 21, 20, 19, 18, 22, 16, 18, 16, 16,
            20, 14, 15, 14, 17, 13, 15, 14, 16, 14, 16, 17, 15, 14, 16, 18, 14, 16, 17, 14, 16, 16,
            16, 16, 17, 16, 17, 15, 14, 16, 17, 17, 18, 17, 18, 17, 17, 19, 16, 18, 17, 15, 15, 15,
            15, 15, 15, 14, 16, 18, 14, 13, 12, 14, 13, 12, 13, 14, 13, 13, 13, 12, 12, 13, 11, 10,
            13, 13, 12, 14, 13, 11, 10, 13, 11, 13, 9, 11, 14, 11, 11, 12, 11, 11, 14, 10, 11, 12,
            12, 12, 10, 11, 11, 12, 11, 10, 11, 11, 12, 12, 11, 12, 10, 10, 12, 11, 13, 11, 9, 11,
            11, 12, 11, 9, 11, 13, 10, 10, 10, 11, 9, 12, 12, 10, 13, 12, 11, 12, 10, 11, 12, 12,
            10, 10, 10, 12, 11, 10, 14, 10, 11, 11, 12, 9, 10, 12, 11, 11, 11, 11, 11, 12, 11, 11,
            11, 10, 13, 9, 10, 10, 11, 10, 11, 10, 9, 12, 12, 10, 11, 10, 9, 12, 12, 10, 11, 11,
            11, 12, 10, 11, 13, 12, 12, 12, 10, 10, 6, 11, 11, 11, 12, 11, 9, 11, 10, 10, 12, 11,
            11, 11, 10, 11, 11, 8, 11);

    public static void main(String[] args) {
        DataScanner dataScanner = new DataScanner(IMG_419_1_0DEG);
        JOptionPane.showMessageDialog(null,
                new Graph(800, 600, 0xffffff)
                        .setData(dataScanner.getGraphData()).setYGridlineInterval(5).refresh(),
                "Scanline Graph",
                JOptionPane.PLAIN_MESSAGE, null);
        // System.out.println("Bucket Set originally = " + arrayToList(dataScanner.bucketSet));
        // dataScanner.processFlatRegion(0, dataScanner.bucketSet.length - 1);
        // System.out.println("Bucket Set is finally = " + arrayToList(dataScanner.bucketSet));
    }

    private class BucketizedDataSet {
        private static final int HALF_BUCKET_SIZE = 6;

        private final int bucketOffset;
        private final int[] bucketSet;

        public BucketizedDataSet(Collection<Integer> rawData) {
            int numPts = rawData.size();
            bucketOffset = (numPts % HALF_BUCKET_SIZE) / 2; // Center the buckets in the raw data.
            int numSemiGroups = numPts / HALF_BUCKET_SIZE;
            Verify.argument(numSemiGroups > 0, "Raw data not large enough to create 1+ buckets");
            bucketSet = new int[numSemiGroups - 1];

            int[] prevSemiGroup = null;
            Iterator<Integer> itr = getIteratorStartingAtPos(rawData, bucketOffset);
            for (int semiGroupNumber = 0; semiGroupNumber < numSemiGroups; semiGroupNumber++) {
                int[] semiGroup = new int[HALF_BUCKET_SIZE];
                for (int i = 0; i < HALF_BUCKET_SIZE; i++) {
                    semiGroup[i] = itr.next();
                }
                Arrays.sort(semiGroup);
                if (prevSemiGroup != null) {
                    bucketSet[semiGroupNumber - 1] = getDblMedian(prevSemiGroup, semiGroup) / 2;
                }
                prevSemiGroup = semiGroup;
            }
        }

        public int getPI(int bucketIdx) {
            return bucketSet[bucketIdx];
        }

        public int setPI(int bucketIdx, int newPI) { // TODO: remove
            return bucketSet[bucketIdx] = newPI;
        }

        // returns smaller end of range, followed by larger end of range
        public Range getRangeForHalfBucket(int halfBucketIdx) {
            int numBuckets = bucketSet.length;
            Verify.argument(halfBucketIdx >= 0 || halfBucketIdx < numBuckets,
                    "halfBucketIdx " + halfBucketIdx + " is out of range");
            int bucketMed1, bucketMed2;
            if(halfBucketIdx == 0){
                bucketMed1 = bucketMed2 = bucketSet[0];
            } else if (halfBucketIdx == numBuckets - 1) {
                bucketMed1 = bucketMed2 = bucketSet[numBuckets - 1];
            }else{
                bucketMed1 = bucketSet[halfBucketIdx];
                bucketMed2 = bucketSet[halfBucketIdx + 1];
            }

            return new Range(bucketMed1, bucketMed2);
        }

        public int getCenterPositionForBucket(int bucketIdx) {
            return HALF_BUCKET_SIZE * (bucketIdx + 1) + bucketOffset;
        }

        public Range getDomainForBucket(int bucketIdx) {
            int bucketStartIdx = HALF_BUCKET_SIZE * bucketIdx + bucketOffset;
            return new Range(bucketStartIdx, bucketStartIdx + 2 * HALF_BUCKET_SIZE - 1);
        }

        public int size() {
            return bucketSet.length;
        }
    }
}
