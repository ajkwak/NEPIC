package nepic.roi;

import java.util.ArrayList;
import java.util.Arrays;
import nepic.util.Verify;

public class OneDimFeatureList { // TODO: rename
    public static final int DEFAULT_MEDIAN_GROUP_SIZE = 20;
    public static final int DEFAULT_SLOPE_GROUP_SIZE = 5;

    int[] origPts;
    DoubleMedian[] medians;

    public OneDimFeatureList(int[] graphPts, int medianGroupingSize) {
        // Copy the original points on the graph
        origPts = new int[graphPts.length];
        for (int i = 0; i < graphPts.length; i++) {
            origPts[i] = graphPts[i];
        }

        // Preprocess the points
        preprocessScanLine(medianGroupingSize);

    }

    private void preprocessScanLine(int medianGroupingSize) {
        int halfGroupSize = medianGroupingSize / 2;

        // Break into groups
        int numPts = origPts.length;
        int numGroups = numPts / halfGroupSize;

        // Now need to decide where to start the groups so that the number of pixels on each side of
        // the groups (at the ends of the scanline) is minimized
        int pos = (numPts % halfGroupSize) / 2;

        // Make all of the groups
        ArrayList<int[]> groups = new ArrayList<int[]>(numGroups);
        for (int i = 0; i < numGroups; i++) { // for each group
            int[] group = new int[halfGroupSize];
            for (int j = 0; j < halfGroupSize; j++) {
                group[j] = origPts[pos];
                pos++;
            }
            Arrays.sort(group);
            groups.add(group);
        }

        // Now find all of the medians
        medians = new DoubleMedian[groups.size() - 1];
        for (int i = 0; i < groups.size() - 1; i++) {
            int startPos = i * medianGroupingSize;
            int endPos = startPos + medianGroupingSize - 1;
            int dblMed = getDblMedian(groups.get(i), groups.get(i + 1));

            medians[i] = new DoubleMedian(startPos, endPos, dblMed);
        }

        // Now find the slope at each median
        int lineGroupingSize = DEFAULT_SLOPE_GROUP_SIZE;
        for (int i = (lineGroupingSize / 2); i < medians.length - (lineGroupingSize / 2); i++) {
            medians[i].slope = getLeastSquareLineSlope(i, lineGroupingSize);
        }
    }

    private static int getDblMedian(int[] sortedList1, int[] sortedList2) {
        Verify.argument(sortedList1.length == sortedList2.length); // For now
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

    private double getLeastSquareLineSlope(int centerPos, int n) {
        Verify.argument(n > 0 && n % 2 == 1, "n must be a positive odd integer");

        int sumXY = 0;
        int sumY = 0;
        int sumX = 0;
        int sumXSquared = 0;

        for (int x = centerPos - (n / 2); x <= centerPos + (n / 2); x++) {
            int y = medians[x].doubleMed;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXSquared += x * x;
        }

        double m = ((double) (n * sumXY - sumX * sumY)) / (n * sumXSquared - sumX * sumX);
        return m;
    }

    private class DoubleMedian {
        int startPos;
        int endPos;
        int doubleMed;
        double slope = Integer.MIN_VALUE; // Impossible value

        public DoubleMedian(int startPos, int endPos, int doubleMed) {
            Verify.argument(startPos <= endPos,
                    "The start pos of a double median must be less than its end pos");
            Verify.argument(doubleMed >= 0 && doubleMed < 256 * 2);
            this.startPos = startPos;
            this.endPos = endPos;
            this.doubleMed = doubleMed;
        }

    }

}
