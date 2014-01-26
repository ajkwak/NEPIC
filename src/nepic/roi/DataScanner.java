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

import nepic.image.ImagePage;
//import nepic.data.GraphData;
import nepic.data.HistogramPositionMap;
import nepic.roi.model.Line;
import nepic.util.Range;
import nepic.util.Verify;

public class DataScanner {
    // private final GraphData data = new GraphData();
    List<Integer> rawData;
    BucketizedDataSet bucketSet;

    // TODO: convert to static method (probably in different class)????
    public DataScanner(ImagePage pg, Line scanline) {
        Verify.notNull(pg, "Page on which to scan the line cannot be null!");
        Verify.notNull(scanline, "line to scan cannot be null!");

        List<Point> scanlinePoints = scanline.boundTo(pg).draw();
        List<Integer> rawData = new ArrayList<Integer>(scanlinePoints.size());
        for (Point pt : scanlinePoints) {
            rawData.add(pg.getPixelIntensity(pt.x, pt.y));
        }
        processRawData(rawData);
    }

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
        // data.setDataSet("01 Raw Data", rawDataPoints, 0x0000ff /* Blue */);
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
