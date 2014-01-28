package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
        // findTrendsInRawData();

        // Bucketize the raw data (does the initial smoothing / simplifying of the data)
        bucketSet = new BucketizedDataSet(rawData);
        graphBucketizedData();
        // graphCrossingsInformation();

        // Process the bucketized data.
        processBucketizedData();
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
        data.setDataSet("01 Raw Data", rawDataPoints, 0x0000ff /* Blue */);
        // System.out.println();
    }

    private void findTrendsInRawData() {
        List<Point> trends = new LinkedList<Point>();
        int prevPI = -1; // Initialize as invalid.
        int trendLength = 0;
        int trendDelta = 0;
        int startPos = 0;
        for (int pos = startPos; pos < rawData.size(); pos++) {
            int currPI = rawData.get(pos);
            System.out.print("rawData[" + pos + "] = " + currPI);
            if (prevPI > -1) { // If prevPI is valid.
                int delta = currPI - prevPI;
                if (delta != 0 && delta * trendDelta >= 0) { // Continuing current trend.
                    trendDelta += delta;
                    trendLength++;
                    System.out.print((trendDelta > 0 ? " UP  ," : " DOWN,")
                            + " trendLength = " + trendLength);
                } else {
                    // First determine if the current trend is significant.
                    if (Math.abs(trendDelta) >= 20) { // Is significant trend.
                        // TODO: now separate this trend from preceding trends/flat regions
                        // System.out.println("Trend: " + (pos - trendLength) + "-" + pos + " (PI "
                        // + rawData.getPI(pos - trendLength) + "-" + rawData.getPI(pos)
                        // + ")");
                        // processFlatRegion(rawData, startPos, pos - trendLength);
                        System.out.print(" ADD TREND ");
                        for (int i = pos - trendLength - 1; i < pos; i++) {
                            trends.add(new Point(i, rawData.get(i)));
                            System.out.print("(" + i + ", " + rawData.get(i) + ") ");
                        }
                        startPos = pos;// + 1;
                    }

                    // Begin a new trend.
                    if (delta == 0) {
                        trendDelta = 0;
                        trendLength = 0;
                    } else {
                        trendDelta = delta;
                        trendLength = 1;
                        System.out.print((trendDelta > 0 ? " UP  ," : " DOWN,")
                                + " trendLength = " + trendLength);
                    }
                }
            }
            prevPI = currPI;
            System.out.println();
        }
        if (!trends.isEmpty()) {
            graphRawDataTrends(trends);
        }
    }

    private void graphRawDataTrends(List<Point> rawDataTrends) {
        List<Point> rawTrendsDataSet = new ArrayList<Point>(rawData.size());
        int nonTrendY = data.getMinY();
        int lastGraphedPos = -1;
        for (Point trendPt : rawDataTrends) {
            // for (int pos = lastGraphedPos + 1; pos < trendPt.x; pos++) {
            // rawTrendsDataSet.add(new Point(pos, nonTrendY));
            // }
            lastGraphedPos = trendPt.x;
            rawTrendsDataSet.add(new Point(lastGraphedPos, trendPt.y));
        }
        // for (int pos = lastGraphedPos + 1; pos < rawData.size(); pos++) {
        // rawTrendsDataSet.add(new Point(pos, nonTrendY));
        // }
        data.setDataSet("05 Raw Data Trends", rawTrendsDataSet, 0xffff00 /* Yellow */);
    }

    private void graphCrossingsInformation() {
        int nonSigY = data.getMinY();
        LinkedList<Point> crossingsDataSet = new LinkedList<Point>();
        for (int bucketIdx = 0; bucketIdx < bucketSet.size(); bucketIdx++) {
            Range bucketDomain = bucketSet.getDomainForBucket(bucketIdx);
            int bucketPI = bucketSet.getPI(bucketIdx);
            int numCross = getNumberRawDataCrossings(bucketPI, bucketDomain.min, bucketDomain.max);
            if (numCross <= 2) {
                System.out.println("numCrossings = " + numCross);
                crossingsDataSet.add(new Point(bucketDomain.min, nonSigY));
                crossingsDataSet.add(new Point(bucketDomain.min, bucketPI));
                crossingsDataSet.add(new Point(bucketDomain.max, bucketPI));
                crossingsDataSet.add(new Point(bucketDomain.max, nonSigY));
            }
        }
        data.setDataSet("00 Crossing Information", crossingsDataSet, 0xffff00 /* Yellow */);
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
        data.setDataSet("02 Bucket Set 1", bucketDataSet1, 0x008800 /* Dark Green */);
        data.setDataSet("03 Bucket Set 2", bucketDataSet2, 0x00aa00 /* Green */);
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
        graphProcessedData();
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
        data.setDataSet("04 Processed Bucket Set", processedBucketSet, 0xff0000 /* Red */);
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

    public static void main(String[] args) {
        DataScanner dataScanner = new DataScanner(Lists.newArrayList(
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
                35, 31, 31, 32, 28, 30, 33, 32, 29, 33, 33, 31, 31));
        JOptionPane.showMessageDialog(null,
                new Graph(800, 600, 0x000000)
                        .setData(dataScanner.getGraphData()).setYGridlineInterval(5),
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
