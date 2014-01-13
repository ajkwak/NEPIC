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

import nepic.gui.ScannerGroupSizeVarierPanel;
import nepic.image.ImagePage;
import nepic.data.GraphData;
import nepic.data.HistogramPositionMap;
import nepic.roi.model.Line;
import nepic.util.Lists;
import nepic.util.Verify;

public class DataScanner {
    // Bucketized Data.
    private int bucketOffset;
    private int halfBucketSize = 6;

    // For graphing all the intermediate steps of processing the raw data.
    private final GraphData data = new GraphData();

    // TODO: convert to static method (probably in different class)????
    public DataScanner(ImagePage pg, Line scanline) {
        Verify.notNull(pg, "Page on which to scan the line cannot be null!");
        Verify.notNull(scanline, "line to scan cannot be null!");

        List<Point> scanlinePoints = scanline.boundTo(pg).draw();
        List<Integer> rawData = new ArrayList<Integer>(scanlinePoints.size());
        for (Point pt : scanlinePoints) {
            rawData.add(pg.getPixelIntensity(pt.x, pt.y));
        }
        graphRawData(rawData);
        int[] bucketSet = bucketizeRawData(rawData);
        processBucketizedData(bucketSet);
    }

    public DataScanner(Collection<Integer> rawData) {
        Verify.nonEmpty(rawData, "rawData");
        graphRawData(rawData);
        int[] bucketSet = bucketizeRawData(rawData);
        processBucketizedData(bucketSet);
    }

    /**
     * Adds the given raw data to this class's {@link GraphData} object.
     *
     * @param initialData the raw data to graph
     */
    private void graphRawData(Collection<Integer> initialData) {
        List<Point> rawData = new ArrayList<Point>(initialData.size());
        int idx = 0;
        for (int datum : initialData) {
            System.out.print(datum + ", ");
            rawData.add(new Point(idx, datum));
            idx++;
        }
        data.setDataSet("01 Raw Data", rawData, 0x0000ff /* Blue */);
        System.out.println();
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

    // Does the initial smoothing / simplifying of the data to get rid of most of the noise.
    private int[] bucketizeRawData(Collection<Integer> rawData) {
        int numPts = rawData.size();
        bucketOffset = (numPts % halfBucketSize) / 2; // Center the sets of buckets in the raw data.
        int numSemiGroups = numPts / halfBucketSize;
        int[] bucketSet = new int[numSemiGroups - 1];

        int[] prevSemiGroup = null;
        Iterator<Integer> itr = getIteratorStartingAtPos(rawData, bucketOffset);
        for (int semiGroupNumber = 0; semiGroupNumber < numSemiGroups; semiGroupNumber++) {
            int[] semiGroup = new int[halfBucketSize];
            for (int i = 0; i < halfBucketSize; i++) {
                semiGroup[i] = itr.next();
            }
            Arrays.sort(semiGroup);
            if (prevSemiGroup != null) {
                bucketSet[semiGroupNumber - 1] = getDblMedian(prevSemiGroup, semiGroup) / 2;
            }
            prevSemiGroup = semiGroup;
        }
        graphBucketizedData(bucketSet);
        return bucketSet;
    }

    private void graphBucketizedData(int[] bucketSet) {
        // Graph the first bucketSet
        int numBuckets = bucketSet.length;
        ArrayList<Point> bucketDataSet1 = new ArrayList<Point>(numBuckets); // TODO
        ArrayList<Point> bucketDataSet2 = new ArrayList<Point>(numBuckets); // TODO
        boolean isFirstBucketSet = true;
        for(int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++){
            int bucketStartIdx = halfBucketSize * bucketIdx + bucketOffset;
            int bucketPI = bucketSet[bucketIdx];// / 2;
            if (isFirstBucketSet) {
                bucketDataSet1.add(new Point(bucketStartIdx, bucketPI));
                bucketDataSet1.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
            } else {
                bucketDataSet2.add(new Point(bucketStartIdx, bucketPI));
                bucketDataSet2.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
            }
            isFirstBucketSet = !isFirstBucketSet;
        }
        data.setDataSet("02 Bucket Set 1", bucketDataSet1, 0x008800 /* Dark Green */);
        data.setDataSet("03 Bucket Set 2", bucketDataSet2, 0x00aa00 /* Green */);
    }

    /**
     * IDEA:
     *
     * There are 2 bucketized lists. Each 'bucket' overlaps 1/2 the previous 'bucket' and 1/2 the
     * next 'bucket.' Thus, if noise were perfect and uniform, this means that for flat sections,
     * the value (median) of each consecutive bucket would be the same.
     *
     * diff = 1 (same value) diff = 2-4 (probably same value)
     *
     */
    // private void processBucketizedData() {
        // TODO: This is a replacement for determineCurrentRegionType
        // TODO: do sth to do with window size (do initial preprocessing into FLAT, UP, and DOWN,
        // then smooth the flat (via the modes method), sharpen the up/down where need be

    // }

    // private void processBucketizedData(int startPos) {
        // ONLY LOOK FOR UPs, DOWNs: FLATS ARE THE THINGS THAT ARE NEITHER
        // SHALLOWER THE UP/DOWN, THE MORE NOISE THERE WILL BE (allow up to 1 consecutive datum of
        // noise per UP or DOWN)
    // }

    private void processBucketizedData(int[] bucketSet) {
        int[] processedBucketSet = new int[bucketSet.length];

        int prevDblPI = -1; // Initialize as invalid.
        int trendLength = 0;
        int trendDblDelta = 0;
        int startPos = 0;
        for (int pos = startPos; pos < bucketSet.length; pos++) {
            int currDblPI = bucketSet[pos];
            if (prevDblPI > -1) { // If prevPI is valid.
                int dblDelta = currDblPI - prevDblPI;
                if (dblDelta != 0 && dblDelta * trendDblDelta >= 0) { // Continuing current trend.
                    trendDblDelta += dblDelta;
                    trendLength++;
                } else {
                    // First determine if the current trend is significant.
                    if (trendLength > 4 || Math.abs(trendDblDelta) >= 10) { // Is significant trend.
                        // TODO: now separate this trend from preceding trends/flat regions
                        System.out.println("Trend: " + (pos - trendLength) + "-" + pos + " (PI "
                                + bucketSet[pos - trendLength] + "-" + bucketSet[pos] + ")");
                        processFlatRegion(bucketSet, startPos, pos - trendLength);
                        startPos = pos + 1;
                    } // else if (trendDblDelta >= 10) { // Then is MAYBE a significant trend.
                        // TODO: determine if this is actually a trend. If isn't, then continue
                        // (i.e. begin new trend without saving current one); otherwise, do what did
                        // in previous 'if' statement
                    // }

                    // Begin a new trend.
                    if (dblDelta == 0) {
                        trendDblDelta = 0;
                        trendLength = 0;
                    }else{
                        trendDblDelta = dblDelta;
                        trendLength = 1;
                    }
                }
            }
            prevDblPI = currDblPI;
            // System.out.println("pos = " + pos);
        }
        processFlatRegion(bucketSet, startPos, bucketSet.length - 1);
        graphProcessedData(bucketSet);
    }

    private void graphProcessedData(int[] bucketSet) {
        // Graph the first bucketSet
        int numBuckets = bucketSet.length;
        System.out.println("bucketSet length = " + numBuckets);
        ArrayList<Point> processedBucketSet = new ArrayList<Point>(numBuckets);
        for (int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++) {
            int bucketPos = halfBucketSize * (bucketIdx + 1) + bucketOffset;
            int bucketPI = bucketSet[bucketIdx];// / 2;
            processedBucketSet.add(new Point(bucketPos, bucketPI));
        }
        data.setDataSet("04 Processed Bucket Set", processedBucketSet, 0xff0000 /* Red */);
    }

    private void processFlatRegion(int[] bucketSet, int startPos, int endPos) {
        // ALGORITHM:
        // Go for the first five values in the flat region. Find the mode of those 5. Continue from
        // the first time that mode appears to the last time the mode appears. Then begin again with
        // new mode. The mode must ALWAYS appear within 4 of the last appearance of the mode.
        System.out.println("FLAT: " + startPos + "-" + endPos);
        int pos = startPos;
        while (pos <= endPos) {
            pos = flattenSingleModeRegion(bucketSet, pos, endPos);
            System.out.println("Final position after flattening single mode region = " + pos);
        }
    }

    // Commences at startPos (although there may be a region that starts before the mode begins, at
    // which point, this will have more than a single mode. HOWEVER, this will have
    /**
     *
     * @param startPos
     * @param maxEndPos
     * @return the last position in this single mode region.
     */
    private int flattenSingleModeRegion(int[] bucketSet, int startPos, int maxEndPos) {
        int prevDblPI = -1; // Initialize to invalid value.
        HistogramPositionMap histogram = new HistogramPositionMap(0, 255);

        // Determine the initial mode.
        int pos;
        for (pos = startPos; pos <= Math.min(startPos + 4, maxEndPos); pos++) {
            int currDblPI = bucketSet[pos];
            // System.out.println("bucketSet[" + pos + "] = " + currDblPI);
            adjustToDblPI(histogram, currDblPI, pos);
            if (prevDblPI > -1) { // If previous dblPI is valid (i.e. if i > startPos).
                // Add the average of the currDblPI and the prevDblPI to the Histogram, so the mode
                // not only includes actual values, but also values that are crossed.
                // adjustToDblPI(histogram, (prevDblPI + currDblPI) / 2, pos);
            }
            prevDblPI = currDblPI;
        }

        // Continue until the last time the mode is crossed within range.
        // Only keep going if 1+ of these are/cross the mode. (don't add these to the
        // HistogramPositionMap UNLESS 1+ of them are/cross the mode)
        Queue<Integer> nextFourDblPIs = new ArrayBlockingQueue<Integer>(4);
        int queueSize = 0;
        for (int trialPos = pos; trialPos <= maxEndPos; trialPos++) {
            // Determine if current dblPI matches any of the current modes.
            int currDblPI = bucketSet[trialPos];
            // System.out.print("\tbucketSet[" + trialPos + "] = " + currDblPI);
            if (queueSize == 4) {
                // System.out.println("\tqueueSize == 4");
                break; // Now just need to combine the region into one mode.
            } else {
                nextFourDblPIs.add(currDblPI);
                queueSize++;
                if (valueMatchesCurrentMode(currDblPI, histogram)) {
                    // System.out.print(" value matches current mode. Add ");
                    for (int i = 0; i < queueSize; i++) {
                        currDblPI = nextFourDblPIs.remove();
                        adjustToDblPI(histogram, currDblPI, pos);
                        // System.out.print("(pos = " + pos + ", PI = " + currDblPI + "), ");
                        // adjustToDblPI(histogram, (prevDblPI + currDblPI) / 2, pos);
                        // System.out.print("(pos = " + pos + ", PI = " + (prevDblPI + currDblPI) /
                        // 2 + "), ");
                        pos++;
                        prevDblPI = currDblPI;
                    }
                    queueSize = 0;
                }
            }
            // System.out.println("");
        }

        // Get the average of all the current modes;
        int mode = 0;
        int firstModeAppearancePos = pos;
        List<Integer> modes = histogram.getModes();
        for(int modeComponent : modes){
            mode += modeComponent;
            int firstAppearance = histogram.getPositionsAssociatedWith(modeComponent).get(0);
            if (firstAppearance < firstModeAppearancePos) {
                firstModeAppearancePos = firstAppearance;
            }
        }
        mode /= modes.size();

         //TODO: this next piece should be altered (not in place)
        for (int replacePos = firstModeAppearancePos; replacePos < pos; replacePos++) {
            bucketSet[replacePos] = mode;
        }

        return pos; // Returns the last position in this single mode region
    }

    // Alters the histogram array, mode list IN PLACE
    private void adjustToDblPI(HistogramPositionMap histogram, int dblPI, int position) {
        // histogram.addValueAtPosition(dblPI / 2, position);
        // if (dblPI % 2 == 1) {
        // histogram.addValueAtPosition(dblPI / 2 + 1, position);
        // }
        histogram.addValueAtPosition(dblPI, position);
    }

    private boolean valueMatchesCurrentMode(int dblPI, HistogramPositionMap histogram) {
        int pi1 = dblPI;// / 2;
        // int pi2 = dblPI % 2 == 1 ? pi1 + 1 : pi1;
        // System.out.print(" modes = " + histogram.getModes() + " ");
        for (int mode : histogram.getModes()) {
            if (mode == pi1) {// || mode == pi2) {
                return true;
            }
        }
        return false;
    }

    // return endPos (also possibly include the 'type' of the next region)
    private void processRisingRegion(int startPos) {
    }

    // return endPos (also possibly include the 'type' of the next region)
    private void processFallingRegion(int startPos) {

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
        JOptionPane.showMessageDialog(null, new ScannerGroupSizeVarierPanel(dataScanner),
                "Scanline Graph",
                JOptionPane.PLAIN_MESSAGE, null);
        // System.out.println("Bucket Set originally = " + arrayToList(dataScanner.bucketSet));
        // dataScanner.processFlatRegion(0, dataScanner.bucketSet.length - 1);
        // System.out.println("Bucket Set is finally = " + arrayToList(dataScanner.bucketSet));
    }

    private static List<Integer> arrayToList(int[] array) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int element : array) {
            list.add(element);
        }
        return list;
    }
}
