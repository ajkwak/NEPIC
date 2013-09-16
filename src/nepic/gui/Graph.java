package nepic.gui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import nepic.roi.model.LineSegment;
import nepic.roi.model.LineSegment.IncludeEnd;
import nepic.roi.model.LineSegment.IncludeStart;
import nepic.util.ColoredPointList;
import nepic.util.DataSet;
import nepic.util.Verify;

public class Graph {
    private final int[] testDataSet = new int[]{
            32,33,31,34,33,34,36,31,33,32,31,31,33,33,33,36,35,35,35,36,32,35,33,36,35,37,32,36,37,
            35,37,34,38,36,39,41,49,58,57,65,64,61,58,62,62,57,48,47,44,42,42,36,35,37,31,32,32,32,
            32,32,31,31,33,31,32,33,32,36,32,32,29,33,29,31,31,33,33,31,33,28,30,31,30,30};

    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private final DataSet[] dataSets;

    private final AnnotatableImage img;

    public Graph(int width, int height, int bkColor, int numDataSets) {
        Verify.argument(width > 0 && height > 0, "Graph must have a positive width and height");
        Verify.argument(numDataSets > 0,
                "The number of data sets expected in the graph must be greater than zero");
        dataSets = new DataSet[numDataSets];
        img = new AnnotatableImage(numDataSets).setImage(newGraphImage(width, height, bkColor));
    }

    public BufferedImage newGraphImage(int width, int height, int bkColor) {
        BufferedImage graphImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                graphImage.setRGB(x, y, bkColor);
            }
        }
        return graphImage;
    }

    public void newDataSet(int id, List<? extends Point> newVals, int rgb) {
        Verify.argument(img.isValidId(id), "Invalid dataset id " + id);
        Verify.nonEmpty(newVals);

        // Make a DataSet out of the newVals
        DataSet newDataSet = new DataSet(newVals);
        dataSets[id] = newDataSet;
        if (newDataSet.getMinX() < minX) {
            minX = newDataSet.getMinX();
            System.out.println("minX now = " + minX);
        }
        if (newDataSet.getMaxX() > maxX) {
            maxX = newDataSet.getMaxX();
        }
        if (newDataSet.getMinY() < minY) {
            minY = newDataSet.getMinY();
        }
        if (newDataSet.getMaxY() > maxY) {
            maxY = newDataSet.getMaxY();
        }
    }

    public void redraw() {
        // For now, ONLY have option of not in-scale
        for (DataSet dataSet : dataSets) { // TODO: invert y values so that CB is right side up.
            double ratioX = (double) (img.getWidth() - 1) / (dataSet.getMaxX() - dataSet.getMinX());
            long offsetX = 0 - Math.round(ratioX * dataSet.getMinX());
            double ratioY = (double) (img.getHeight() - 1)
                    / (dataSet.getMaxY() - dataSet.getMinY());
            long offsetY = 0 - Math.round(ratioY * dataSet.getMinY());
            List<Point> convolvedData = new ArrayList<Point>(dataSet.size());
            Point startPt = null;
            for (Point datum : dataSet) {
                Point endPt = new Point(
                        (int) Math.round(ratioX * datum.x + offsetX),
                        (int) Math.round(ratioY * datum.y + offsetY));
                if (startPt != null) {
                    convolvedData.addAll(
                            new LineSegment(startPt, endPt).draw(IncludeStart.YES, IncludeEnd.NO));
                }
                startPt = endPt;
            }
            convolvedData.add(startPt); // add the end point
            img.redraw(0, new ColoredPointList(convolvedData, 0x000000));
        }
    }

    /* TEST STUFF */

    private static List<Point> testDataSet() {
        int[] testData = new int[]{
                32,33,31,34,33,34,36,31,33,32,31,31,33,33,33,36,35,35,35,36,32,35,33,36,35,37,32,36,37,
                35,37,34,38,36,39,41,49,58,57,65,64,61,58,62,62,57,48,47,44,42,42,36,35,37,31,32,32,32,
                32,32,31,31,33,31,32,33,32,36,32,32,29,33,29,31,31,33,33,31,33,28,30,31,30,30};
        List<Point> testDataSet = new ArrayList<Point>(testData.length);
        for (int i = 0; i < testData.length; i++) {
            testDataSet.add(new Point(i + 13, testData[i]));
        }
        return testDataSet;
    }

    public static void main(String[] args) {
        Graph graph = new Graph(300, 300, 0xcc99ff, 1);
        graph.newDataSet(0, testDataSet(), 0x000000);
        graph.redraw();
        JLabel picLabel = new JLabel(new ImageIcon(graph.img.getImage()));
        JOptionPane.showMessageDialog(null, picLabel, "About", JOptionPane.PLAIN_MESSAGE, null);
    }
}
