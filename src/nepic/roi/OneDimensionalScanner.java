package nepic.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nepic.image.ImagePage;
import nepic.data.DataSet;
import nepic.data.GraphData;
import nepic.data.Histogram;
import nepic.roi.model.Line;
import nepic.util.Verify;

public class OneDimensionalScanner {
    private final GraphData data; // For displaying all intermediate steps of 'scanning' the line.
    private int rawDataId = -1;
    private int smoothedDataId = -1;
    private int slopesId = -1;

    // private int edgeCategoriesId = -1;

    public OneDimensionalScanner(ImagePage pg, Line scanline) {
        Verify.notNull(pg, "Page on which to scan the line cannot be null!");
        Verify.notNull(scanline, "line to scan cannot be null!");
        // TODO: verify line crosses image page, so when bound and draw, dont't get
        // NullPointerException

        Histogram hist = pg.makeHistogram();
        System.out.println("BK bound = " + hist.getPercentile(50));
        System.out.println("ROI bound = " + hist.getPercentile(99.99));

        data = new GraphData(4);
        List<Point> scanlinePoints = scanline.boundTo(pg).draw();
        List<Point> rawData = new ArrayList<Point>(scanlinePoints.size());
        int i = 0;
        for (Point pt : scanlinePoints) {
            rawData.add(new Point(i, pg.getPixelIntensity(pt.x, pt.y)));
            i++;
        }
        rawDataId = data.addDataSet("Raw Data", rawData, 0x0000ff /* Blue */);
    }

    // Does the initial smoothing / simplifying of the data to get rid of most of the noise.
    public void smoothData(int groupSize) {
        DataSet rawData = data.getDataSet(rawDataId);

        // Break into groups
        int numPts = rawData.size();
        int numGroups = numPts / groupSize;
        ArrayList<Point> medians = new ArrayList<Point>(2 * numGroups - 1); // TODO: verify size

        // Now need to decide where to start the groups so that the number of pixels on each side of
        // the groups (at the ends of the scanline) is minimized
        int startPos = (numPts % groupSize) / 2;
        Iterator<Point> scanlineItr = rawData.iterator();
        for (int i = 0; i < startPos; i++) {
            scanlineItr.next(); // ignore all points before the start point
        }

        // Find the median of each group.
        int currentPos = startPos;
        int[] prevGroup = null;
        for (int groupNum = 0; groupNum < numGroups; groupNum++) { // for each group
            int[] group = new int[groupSize];
            int medianPos = 0;
            for (int groupIdx = 0; groupIdx < groupSize; groupIdx++) {
                medianPos += currentPos;
                Point pt = scanlineItr.next();
                currentPos++;
                group[groupIdx] = pt.y;
            }
            Arrays.sort(group);
            medianPos = medianPos / groupSize;
            if (prevGroup != null) {
                // Then find the median of the two groups.
                int dblMed = getDblMedian(prevGroup, group);
                medians.add(new Point(medianPos, dblMed));
            }
            prevGroup = group;
        }

        // Set the smoothed data to this new value.
        if (data.datasetExists(smoothedDataId)) {
            data.redefineDataSetValues(smoothedDataId, medians);
        } else {
            smoothedDataId = data.addDataSet("Smoothed Data", medians, 0x00cc00 /* Green */);
        }
        List<Point> dataSlopes = getFirstDerivative(medians);
        if (data.datasetExists(slopesId)) {
            data.redefineDataSetValues(slopesId, dataSlopes);
        } else {
            slopesId = data.addDataSet("Slopes at Data Points", dataSlopes, 0x8800ff /* Purple */);
        }
    }

    public GraphData getGraphData() {
        Verify.argument(data.getValidIds().size() > 0);
        return data;
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

    // Gets the slope at each data point.
    private List<Point> getFirstDerivative(List<Point> data) {
        int numDataPoints = data.size();
        int[] x = new int[numDataPoints];
        int[] y = new int[numDataPoints];
        int[] xSquared = new int[numDataPoints];
        int[] xy = new int[numDataPoints];

        // Find all of the intermediate values (used to calculate the slopes).
        int i = 0;
        for (Point datum : data) {
            int xVal = datum.x;
            int yVal = datum.y;
            x[i] = xVal;
            y[i] = yVal;
            xSquared[i] = xVal * xVal;
            xy[i] = xVal * yVal;
            i++;
        }

        int n = 5; // Needs to be odd (actually, can't be changed until change the next lines)
        List<Point> slopes = new ArrayList<Point>(numDataPoints - 4);
        for (i = 2; i < numDataPoints - 2; i++) {
            int sumX = 0, sumY = 0, sumXSquared = 0, sumXY = 0;

            // Calculate all the above sums.
            for (int delta = -2; delta <= 2; delta++) {
                sumX += x[i + delta];
                sumY += y[i + delta];
                sumXSquared += xSquared[i + delta];
                sumXY += xy[i + delta];
            }

            // Calculate the slope of the line at x[i]
            double m = ((double) (n * sumXY - sumX * sumY)) / (n * sumXSquared - sumX * sumX);
            int slope_x100 = (int) Math.round(100 * m);
            slopes.add(new Point(x[i], slope_x100));
        }
        return slopes;
    }

    // private List<Point> getEdgeCategories(List<Point> dataSlopes) {
    // List<Point> edgeCats = new ArrayList<Point>(dataSlopes.size());
    // for (Point slopePt : dataSlopes) {
    // int slope = slopePt.y;
    // int edgeCategory = slope >= 200 ? 1 : slope <= -200 ? -1 : 0;
    // edgeCats.add(new Point(slopePt.x, edgeCategory));
    // }
    // return edgeCats;
    // }
}
