package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.image.ImagePage;
import nepic.data.GraphData;
import nepic.roi.model.Line;
import nepic.util.Verify;

public class DataScanner {
    // Bucketized Data.
    private int bucketOffset;
    private int halfBucketSize = 6;
    private int[] bucketSet;

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
        bucketizeRawData(rawData);
    }

    public DataScanner(Collection<Integer> rawData) {
        Verify.nonEmpty(rawData, "rawData");
        graphRawData(rawData);
        bucketizeRawData(rawData);
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
            rawData.add(new Point(idx, datum));
            idx++;
        }
        data.setDataSet("Raw Data", rawData, 0x0000ff /* Blue */);
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
    private void bucketizeRawData(Collection<Integer> rawData) {
        int numPts = rawData.size();
        bucketOffset = (numPts % halfBucketSize) / 2; // Center the sets of buckets in the raw data.
        int numSemiGroups = numPts / halfBucketSize;
        bucketSet = new int[numSemiGroups - 1];

        int[] prevSemiGroup = null;
        Iterator<Integer> itr = getIteratorStartingAtPos(rawData, bucketOffset);
        for (int semiGroupNumber = 0; semiGroupNumber < numSemiGroups; semiGroupNumber++) {
            int[] semiGroup = new int[halfBucketSize];
            for (int i = 0; i < halfBucketSize; i++) {
                semiGroup[i] = itr.next();
            }
            Arrays.sort(semiGroup);
            if (prevSemiGroup != null) {
                bucketSet[semiGroupNumber - 1] = getDblMedian(prevSemiGroup, semiGroup);
            }
            prevSemiGroup = semiGroup;
        }
        graphBucketizedData();
    }

    private void graphBucketizedData(){
        // Graph the first bucketSet
        int numBuckets = bucketSet.length;
        ArrayList<Point> bucketDataSet1 = new ArrayList<Point>(numBuckets); // TODO
        ArrayList<Point> bucketDataSet2 = new ArrayList<Point>(numBuckets); // TODO
        boolean isFirstBucketSet = true;
        for(int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++){
            int bucketStartIdx = halfBucketSize * bucketIdx + bucketOffset;
            int bucketPI = bucketSet[bucketIdx] / 2;
            if (isFirstBucketSet) {
                bucketDataSet1.add(new Point(bucketStartIdx, bucketPI));
                bucketDataSet1.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
            } else {
                bucketDataSet2.add(new Point(bucketStartIdx, bucketPI));
                bucketDataSet2.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
            }
            isFirstBucketSet = !isFirstBucketSet;
        }
        data.setDataSet("Bucket Set 1", bucketDataSet1, 0x008800 /* Dark Green */);
        data.setDataSet("Bucket Set 2", bucketDataSet2, 0x00aa00 /* Green */);
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
    private void processBucketizedData() {
        // TODO: This is a replacement for determineCurrentRegionType
        // TODO: do sth to do with window size (do initial preprocessing into FLAT, UP, and DOWN,
        // then smooth the flat (via the modes method), sharpen the up/down where need be
    }

    private void processBucketizedData(int startPos) {
        // ONLY LOOK FOR UPs, DOWNs: FLATS ARE THE THINGS THAT ARE NEITHER
        // SHALLOWER THE UP/DOWN, THE MORE NOISE THERE WILL BE (allow up to 1 consecutive datum of
        // noise per UP or DOWN)
    }

    private void determineCurrentRegionType(int startPos) {
        // Use # times change up/down, total up, total down, largest up, largest down, etc.
        // DON"T FORGET ADJACENT REGIONS OVERLAP. NEED TO LOOK AT EVERY OTHER, AS WELL AS EVERY ONE
        int numUps = 0;
        int numDowns = 0;
        int numFlats = 0;

        int numChanges = 0;

        int totalUp = 0;
        int totalDown = 0;

        int maxUp = 0;
        int maxDown = 0;

        int prevPI = -1;
        int prevDelta = 0;
        for (int i = startPos; i < startPos + 6; i++) {
            if (i >= bucketSet.length) {
                break;
            }
            int currPI = bucketSet[i];
            if (i > startPos) {
                int currDelta = currPI - prevPI;
                if ((prevDelta < 0 && currDelta > 0) || (prevDelta > 0 && currDelta < 0)) {
                    numChanges++;
                }
                if (currDelta == 0) {
                    numFlats++;
                } else { // We're looking changes pos <--> neg (zeros don't count)
                    prevDelta = currDelta;
                    if (currDelta > 0) {
                        numUps++;
                        totalUp += currDelta;
                        if (currDelta > maxUp) {
                            maxUp = currDelta;
                        }
                    } else { // currDelta < 0
                        numDowns++;
                        totalDown += currDelta;
                        if (currDelta < maxDown) {
                            maxDown = currDelta;
                        }
                    }
                }
            }
            prevPI = currPI;
        }
    }

    // return endPos (also possibly include the 'type' of the next region)
    private void processFlatRegion(int startPos) {
        int pos = startPos;

        int[] histogram = new int[256];
        int modeMagnitude = 1; // numOccurrencesMode
        List<Integer> modes = new LinkedList<Integer>();

        int prevDblPI = -1;
        while (true /* TODO */) {
            int dblPI = bucketSet[pos];
            modeMagnitude = adjustMode(dblPI / 2, histogram, modes, modeMagnitude);

            // If the bucketized value is between two pixel intensities, add to second PI.
            if (dblPI % 2 == 1) {
                modeMagnitude = adjustMode(dblPI / 2 + 1, histogram, modes, modeMagnitude);
            }

            // Single mode continues until the last time that mode appears or is crossed.

            // TODO

            pos++;
        }
    }

    private int adjustMode(int pi, int[] histogram, List<Integer> modes, int modeMagnitude) {
        int numOccurrences = histogram[pi] + 1;
        histogram[pi] = numOccurrences;
        if (numOccurrences >= modeMagnitude) {
            if (numOccurrences > modeMagnitude) {
                modes.clear();
                modeMagnitude = numOccurrences;
            }
            modes.add(pi);
        }
        return modeMagnitude;
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
}
