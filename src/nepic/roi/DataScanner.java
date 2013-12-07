package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nepic.image.ImagePage;
import nepic.data.GraphData;
import nepic.roi.model.Line;
import nepic.util.Verify;

public class DataScanner {
    // Bucketized Data.
    private int bucketOffset;
    private int halfBucketSize = 6;
    private int[] bucketSet1;
    private int[] bucketSet2;

    // For graphing all the intermediate steps of processing the raw data.
    private final GraphData data = new GraphData();

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

    private void graphRawData(Collection<Integer> initialData) {
        List<Point> rawData = new ArrayList<Point>(initialData.size());
        int idx = 0;
        for (int datum : initialData) {
            rawData.add(new Point(idx, datum));
            idx++;
        }
        data.setDataSet("Raw Data", rawData, 0x0000ff /* Blue */);
    }

    private <E> Iterator<E> getIteratorStartingAtPos(Iterable<E> iterable, int pos) {
        Iterator<E> itr = iterable.iterator();

        // Note: this will throw an exception if the given 'pos' to which to iterate is PAST the
        // last element of the iterable.
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
        bucketSet1 = new int[numSemiGroups / 2];
        bucketSet2 = new int[(numSemiGroups - 1) / 2];

        int[] prevSemiGroup = null;
        boolean addToMedians1 = true;
        Iterator<Integer> itr = getIteratorStartingAtPos(rawData, bucketOffset);
        for (int semiGroupNumber = 0; semiGroupNumber < numSemiGroups; semiGroupNumber++) {
            int[] semiGroup = new int[halfBucketSize];
            for (int i = 0; i < halfBucketSize; i++) {
                semiGroup[i] = itr.next();
            }
            Arrays.sort(semiGroup);
            if (prevSemiGroup != null) {
                int dblMed = getDblMedian(prevSemiGroup, semiGroup);
                // int groupStartPos = (semiGroupNumber - 1) * halfBucketSize;
                if (addToMedians1) {
                    bucketSet1[(semiGroupNumber - 1) / 2] = dblMed;
                    // medians1.add(new Point(groupStartPos, dblMed / 2));
                    // medians1.add(new Point(groupStartPos + 2 * halfBucketSize - 1, dblMed / 2));
                } else {
                    bucketSet2[(semiGroupNumber - 1) / 2] = dblMed;
                    // medians2.add(new Point(groupStartPos, dblMed / 2));
                    // medians2.add(new Point(groupStartPos + 2 * halfBucketSize - 1, dblMed / 2));
                }
                addToMedians1 = !addToMedians1;
            }
            prevSemiGroup = semiGroup;
        }
        graphBucketizedData();
    }

    private void graphBucketizedData(){
        // Graph the first bucketSet
        int numBuckets = bucketSet1.length;
        ArrayList<Point> bucketDataSet = new ArrayList<Point>(2 * numBuckets);
        for(int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++){
            int bucketStartIdx = 2 * halfBucketSize * bucketIdx + bucketOffset;
            int bucketPI = bucketSet1[bucketIdx] / 2;
            bucketDataSet.add(new Point(bucketStartIdx, bucketPI));
            bucketDataSet.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
        }
        data.setDataSet("Bucket Set 1", bucketDataSet, 0x008800 /* Dark Green */);

        // Graph the second bucketSet
        numBuckets = bucketSet2.length;
        bucketDataSet = new ArrayList<Point>(2 * numBuckets);
        for (int bucketIdx = 0; bucketIdx < numBuckets; bucketIdx++) {
            int bucketStartIdx = (2 * bucketIdx + 1) * halfBucketSize + bucketOffset;
            int bucketPI = bucketSet2[bucketIdx] / 2;
            bucketDataSet.add(new Point(bucketStartIdx, bucketPI));
            bucketDataSet.add(new Point(bucketStartIdx + 2 * halfBucketSize - 1, bucketPI));
        }
        data.setDataSet("Bucket Set 2", bucketDataSet, 0x00aa00 /* Green */);
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
