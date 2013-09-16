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
import nepic.util.ColoredDataSet;
import nepic.util.ColoredPointList;
import nepic.util.Verify;

public class Graph {
    private boolean connectTheDots = true;
    private boolean inScaleX = false;
    private boolean inScaleY = false;

    private int minX = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;

    private final AnnotatableImage img;
    private final ColoredDataSet[] dataSets;

    public Graph(int width, int height, int bkColor, int numDataSets) {
        Verify.argument(width > 0 && height > 0, "Graph must have a positive width and height");
        Verify.argument(numDataSets > 0,
                "The number of data sets expected in the graph must be greater than zero");
        dataSets = new ColoredDataSet[numDataSets];
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

    // TODO: don't require the user to give the ID; instead, find the next unused ID # and return it
    // to the user after adding the data set.
    public void addDataSet(int id, List<? extends Point> newVals, int rgb) {
        Verify.argument(img.isValidId(id), "Invalid dataset id " + id);
        Verify.nonEmpty(newVals);

        // Make a DataSet out of the newVals
        ColoredDataSet newDataSet = new ColoredDataSet(rgb).addPoints(newVals);
        dataSets[id] = newDataSet;
        if (newDataSet.getMinX() < minX) {
            minX = newDataSet.getMinX();
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

    /**
     * Redraws the data set with the given {@code id} to the given list of points and color.
     * 
     * @param id the ID of the data set to redraw
     * @param newVals the new values of the data set
     * @param rgb the RGB color in which to draw the given data set
     */
    public void redrawDataSet(int id, List<? extends Point> newVals, int rgb) {

    }

    /**
     * Recolors the dataset with the given {@code id} to the given RGB color.
     * 
     * @param id the ID of the data set to recolor
     * @param rgb the RGB color in which to recolor the specified data set
     */
    public void recolorDataSet(int id, int rgb) {

    }

    /**
     * Removes the data set with the given {@code id}.
     * 
     * @param id the ID of the data set to remove.
     */
    public void removeDataSet(int id) {

    }

    /**
     * Redraws the graph with the given conditions.
     * 
     * @param connectTheDots whether or not the data points in the data sets should be connected
     * @param inScaleX whether or not to draw the graph in-scale in the x direction (if
     *        {@code false}, then all data sets will cover the entirety of the x-axis)
     * @param inScaleY whether or not to draw the graph in-scale in the y direction (if
     *        {@code false}, then all data sets will cover the entirety of the y-axis)
     */
    public void redraw(boolean connectTheDots, boolean inScaleX, boolean inScaleY) {
        this.connectTheDots = connectTheDots;
        this.inScaleX = inScaleX;
        this.inScaleY = inScaleY;
        for (int i = 0; i < dataSets.length; i++) {
            redrawDataSet(i);
        }
    }

    // Redraws the ColoredDataSet at the given index on the actual graph image.
    private boolean redrawDataSet(int idx) {
        ColoredDataSet dataSet = dataSets[idx];
        if (dataSet == null) {
            return false;
        }
        int minX = inScaleX ? this.minX : dataSet.getMinX();
        int maxX = inScaleX ? this.maxX : dataSet.getMaxX();
        int minY = inScaleY ? this.minY : dataSet.getMinY();
        int maxY = inScaleY ? this.maxY : dataSet.getMaxY();
        // TODO: what if minX == maxX or minY == maxY?? : just put the data set in the MIDDLE of the
        // screen for that direction
        int imgMaxY = img.getHeight() - 1;
        double ratioX = (double) (img.getWidth() - 1) / (maxX - minX);
        long offsetX = 0 - Math.round(ratioX * minX);
        double ratioY = (double) imgMaxY / (maxY - minY);
        long offsetY = 0 - Math.round(ratioY * minY);
        List<Point> convolvedData = new ArrayList<Point>(dataSet.size());
        Point startPt = null;
        for (Point datum : dataSet) {
            Point endPt = new Point(
                    (int) Math.round(ratioX * datum.x + offsetX),
                    imgMaxY - (int) Math.round(ratioY * datum.y + offsetY));
            if (startPt != null) {
                if (connectTheDots) {
                    convolvedData.addAll(new LineSegment(startPt, endPt).draw(IncludeStart.YES,
                            IncludeEnd.NO));
                } else {
                    convolvedData.add(startPt);
                }
            }
            startPt = endPt;
        }
        convolvedData.add(startPt); // add the end point
        img.redraw(idx, new ColoredPointList(convolvedData, dataSet.getRgb()));
        return true;
    }

    /* TEST STUFF */

    private static final int[] TEST_DATA_1 = new int[]{
        32,33,31,34,33,34,36,31,33,32,31,31,33,33,33,36,35,35,35,36,32,35,33,36,35,37,32,36,37,
        35,37,34,38,36,39,41,49,58,57,65,64,61,58,62,62,57,48,47,44,42,42,36,35,37,31,32,32,32,
        32,32,31,31,33,31,32,33,32,36,32,32,29,33,29,31,31,33,33,31,33,28,30,31,30,30};
    private static final int[] TEST_DATA_2 = new int[]{
        25,29,21,23,26,24,23,27,26,29,27,25,25,27,28,27,25,24,27,25,28,28,27,27,30,27,28,27,27,26,
        28,26,27,28,30,28,29,27,25,30,31,28,27,25,27,30,27,31,30,29,47,55,71,88,90,75,62,52,43,39,
        30,34,32,32,29,28,28,29,30,29,30,31,30,32,30,30,31,32,29,29,30,32,32,27,29,26,26,29,26,29,
        29,28,27,29,28,29,28,32,27,30,32,30,27,28,30};

    private static List<Point> testData(int[] yVals, int xOffset) {
        List<Point> testDataSet = new ArrayList<Point>(yVals.length);
        for (int i = 0; i < yVals.length; i++) {
            testDataSet.add(new Point(i + xOffset, yVals[i]));
        }
        return testDataSet;
    }

    public static void main(String[] args) {
        Graph graph = new Graph(300, 300, 0xcc99ff, 5);
        graph.addDataSet(0, testData(TEST_DATA_1, 13), 0x000000);
        graph.addDataSet(1, testData(TEST_DATA_2, -13), 0xffffff);
        graph.redraw(true, false /* inScaleX */, true /* inScaleY */);
        JLabel picLabel = new JLabel(new ImageIcon(graph.img.getImage()));
        JOptionPane.showMessageDialog(null, picLabel, "About", JOptionPane.PLAIN_MESSAGE, null);
    }
}
