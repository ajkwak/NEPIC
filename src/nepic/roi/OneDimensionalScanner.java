package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nepic.image.ImagePage;
import nepic.roi.model.Line;
import nepic.roi.model.LineSegment;
import nepic.util.Verify;

public class OneDimensionalScanner {

    public OneDimensionalScanner(ImagePage pg, Line scanline) {
        Verify.notNull(pg, "Page on which to scan the line cannot be null!");
        Verify.notNull(scanline, "line to scan cannot be null!");
        // TODO: verify line crosses image page, so when bound and draw, dont't get
        // NullPointerException

        LineSegment segment = scanline.boundTo(pg);
        List<Point> scanlinePoints = segment.draw();
        List<Integer> smoothedScanLine = preprocessScanLine(pg, scanlinePoints);

    }

    private List<Integer> preprocessScanLine(ImagePage img, List<Point> scanlinePts) {
        // TODO: eventually want this to be based on the histogram
        int medianGroupingSize = 20; // Note: For now, must be even
        int groupSize = medianGroupingSize / 2;

        // Break into groups
        int numPts = scanlinePts.size();
        int numGroups = numPts / groupSize;

        // Now need to decide where to start the groups so that the number of pixels on each side of
        // the groups (at the ends of the scanline) is minimized
        int startPos = (numPts % groupSize) / 2;
        Iterator<Point> scanlineItr = scanlinePts.iterator();
        for (int i = 0; i < startPos; i++) {
            scanlineItr.next(); // ignore all points before the start point
        }

        // Make all of the groups
        ArrayList<int[]> groups = new ArrayList<int[]>(numGroups);
        for (int i = 0; i < numGroups; i++) { // for each group
            int[] group = new int[groupSize];
            for (int j = 0; j < groupSize; j++) {
                Point pt = scanlineItr.next();
                group[j] = img.getPixelIntensity(pt.x, pt.y);
            }
            Arrays.sort(group);
            groups.add(group);
        }

        // Now find all of the medians
        ArrayList<Integer> medians = new ArrayList<Integer>(groups.size() - 1);
        for (int i = 1; i < groups.size(); i++) {
            int dblMed = getDblMedian(groups.get(i - 1), groups.get(i));
            medians.add(dblMed);
        }
        // System.out.println(medians);
        // System.out.println();
        // for (Integer median : medians) {
        // System.out.print("\t" + median);
        // }
        // System.out.println();
        return medians;
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

    // public static void main(String[] args) {
    // int[] list1 = new int[] { 1, 2, 3, 4 };
    // int[] list2 = new int[] { 5, 6, 7, 8 };
    // int[] sortedOverallList = new int[list1.length + list2.length];
    // for (int i = 0; i < list1.length; i++) {
    // sortedOverallList[i] = list1[i];
    // }
    // for (int i = 0; i < list2.length; i++) {
    // sortedOverallList[i + list1.length] = list2[i];
    // }
    // Arrays.sort(sortedOverallList);
    // System.out.print("[");
    // for (int i = 0; i < sortedOverallList.length; i++) {
    // if (i == sortedOverallList.length / 2 - 1) {
    // System.out.print("*** ");
    // }
    // System.out.print(sortedOverallList[i] + ", ");
    // if (i == sortedOverallList.length / 2) {
    // System.out.print("*** ");
    // }
    // }
    // System.out.println("]");
    //
    // System.out.print(getDblMedian(list1, list2));
    // }

}
