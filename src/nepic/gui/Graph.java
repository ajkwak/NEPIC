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
import nepic.util.Lists;
import nepic.util.Verify;

public class Graph {
    /**
     * Whether or not to draw lines between all the data points in each data set.
     */
    private boolean connectTheDots = true;
    /**
     * Whether or not all of the data sets being graphed should be in-scale with each other on the
     * x-axis.
     */
    private boolean inScaleX = false;
    /**
     * Whether or not all of the data sets being graphed should be in-scale with each other on the
     * y-axis.
     */
    private boolean inScaleY = false;

    /**
     * The global minimum x-value of all the data sets being graphed.
     */
    private int minX = Integer.MAX_VALUE;
    /**
     * The global maximum x-value of all the data sets being graphed.
     */
    private int maxX = Integer.MIN_VALUE;
    /**
     * The global minimum y-value of all the data sets being graphed.
     */
    private int minY = Integer.MAX_VALUE;
    /**
     * The global maximum y-value of all the data sets being graphed.
     */
    private int maxY = Integer.MIN_VALUE;

    /**
     * The image upon which all of the data sets are being graphed.
     */
    private final AnnotatableImage img;
    /**
     * The collection of all the data sets being graphed.
     */
    private final ColoredDataSet[] dataSets;

    /**
     * Creates a {@link Graph} of the given width, height, and background color that can handle and
     * graph the given number of distinct data sets.
     * 
     * @param width the desired width of the graph
     * @param height the desired height of the graph
     * @param bkColor the desired background color of the graph
     * @param numDataSets the number of distinct data sets that must be handled by this
     *        {@link Graph}
     */
    public Graph(int width, int height, int bkColor, int numDataSets) {
        Verify.argument(width > 0 && height > 0, "Graph must have a positive width and height");
        Verify.argument(numDataSets > 0,
                "The number of data sets expected in the graph must be greater than zero");
        dataSets = new ColoredDataSet[numDataSets];
        img = new AnnotatableImage(numDataSets).setImage(newMonochromeImg(width, height, bkColor));
    }

    /**
     * Adds the given values as a new data set on the graph.
     * 
     * @param newVals the data values to add
     * @param rgb the color in which to draw the new data set on the graph
     * @return the unique ID number of the newly added data set
     */
    public int addDataSet(List<? extends Point> newVals, int rgb) {
        Verify.nonEmpty(newVals);

        // Make a DataSet out of the newVals
        int id = findNextAvailableDataSetId();
        redrawDataSet(id, newVals, rgb);
        return id;
    }

    /**
     * Redraws the data set with the given {@code id} to the given list of points and color.
     * 
     * @param id the ID of the data set to redraw
     * @param newVals the new values of the data set
     * @param rgb the RGB color in which to draw the given data set
     */
    public void redrawDataSet(int id, List<? extends Point> newVals, int rgb) {
        ColoredDataSet replacedDataSet = dataSets[id];
        ColoredDataSet newDataSet = new ColoredDataSet(rgb).addPoints(newVals);
        dataSets[id] = newDataSet;
        adjustGlobalBounds(newDataSet);
        if (replacedDataSet != null) {
            reviseGlobalBoundsAfterRemoval(replacedDataSet);
        }
        redrawDataSet(id);
    }

    /**
     * Recolors the dataset with the given {@code id} to the given RGB color.
     * 
     * @param id the ID of the data set to recolor
     * @param rgb the RGB color in which to recolor the specified data set
     */
    public void recolorDataSet(int id, int rgb) {
        ColoredDataSet recoloredDataSet = dataSets[id];
        recoloredDataSet.setRgb(rgb);
        redrawDataSet(id);
    }

    /**
     * Removes the data set with the given {@code id}.
     * 
     * @param id the ID of the data set to remove.
     */
    public void removeDataSet(int id) {
        ColoredDataSet removedDataSet = dataSets[id];
        dataSets[id] = null;
        if (removedDataSet != null) {
            reviseGlobalBoundsAfterRemoval(removedDataSet);
        }
        img.erase(id);
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

    /**
     * Creates a monochromatic {@link BufferedImage} of the given dimensions and color.
     * 
     * @param width the width of the image to create
     * @param height the height of the image to create
     * @param rgb the color of the monochromatic image to create
     * @return the resulting image
     */
    private BufferedImage newMonochromeImg(int width, int height, int rgb) {
        BufferedImage graphImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                graphImage.setRGB(x, y, rgb);
            }
        }
        return graphImage;
    }

    /**
     * Gets a currently-unused unique ID for a data set to be graphed.
     * 
     * @return a currently unused unique data set ID
     * @throws IllegalStateException if all of the unique data set IDs available in the graph are in
     *         use.
     */
    private int findNextAvailableDataSetId() {
        for (int i = 0; i < dataSets.length; i++) {
            if (dataSets[i] == null) { // i.e. if this position is free
                return i;
            }
        }
        throw new IllegalStateException("There are no free data set IDs available.");
    }

    /**
     * Modifies the global bounds of this {@link Graph} to include the given data set.
     * 
     * <p>
     * i.e. if the given data set has a max x-value of 13, but the current bounds max out at 10 in
     * the x-direction, this method modifies the global max x-value to 13 (so that the global bounds
     * now include the bounds of the given data set).
     * 
     * @param dataSet the data set to include in the graph bounds
     */
    private void adjustGlobalBounds(ColoredDataSet dataSet) {
        if (dataSet.getMinX() < minX) {
            minX = dataSet.getMinX();
        }
        if (dataSet.getMaxX() > maxX) {
            maxX = dataSet.getMaxX();
        }
        if (dataSet.getMinY() < minY) {
            minY = dataSet.getMinY();
        }
        if (dataSet.getMaxY() > maxY) {
            maxY = dataSet.getMaxY();
        }
    }

    /**
     * Revises the global bounds of this {@link Graph}, assuming that the given data set has been
     * previously removed from the graph.
     * 
     * @param removedDataSet the data set that has been removed from the graph
     */
    private void reviseGlobalBoundsAfterRemoval(ColoredDataSet removedDataSet) {
        if (removedDataSet.getMinX() == minX || removedDataSet.getMaxX() == maxX
                || removedDataSet.getMinY() == minY || removedDataSet.getMaxY() == maxY) {
            // Then need to adjust the global bounds.
            minX = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            minY = Integer.MAX_VALUE;
            maxY = Integer.MIN_VALUE;
            for (int i = 0; i < dataSets.length; i++) {
                ColoredDataSet dataSet = dataSets[i];
                if (dataSet != null) {
                    adjustGlobalBounds(dataSet);
                }
            }
        }
    }

    /**
     * Redraws (graphs) the ColoredDataSet at the given unique ID on the actual graph image.
     * 
     * @param id the ID of the data set to graph
     */
    private void redrawDataSet(int id) {
        ColoredDataSet dataSet = dataSets[id];
        if (dataSet == null) { // Then no need to redraw the data set.
            return;
        }

        // Determine the maxima and minima for graphing this data set.
        int minX = inScaleX ? this.minX : dataSet.getMinX();
        int maxX = inScaleX ? this.maxX : dataSet.getMaxX();
        int minY = inScaleY ? this.minY : dataSet.getMinY();
        int maxY = inScaleY ? this.maxY : dataSet.getMaxY();

        // Determine the multiplier and offset to graph this data set properly in the x direction.
        double ratioX;
        long offsetX;
        if (minX == maxX) {
            ratioX = 1;
            offsetX = img.getWidth() / 2;
        } else {
            ratioX = (double) (img.getWidth() - 1) / (maxX - minX);
            offsetX = 0 - Math.round(ratioX * minX);
        }

        // Determine the multiplier and offset to graph this data set properly in the y direction.
        int imgMaxY = img.getHeight() - 1;
        double ratioY;
        long offsetY;
        if (minY == maxY) {
            ratioY = 1;
            offsetY = img.getHeight() / 2;
        } else {
            ratioY = (double) imgMaxY / (maxY - minY);
            offsetY = 0 - Math.round(ratioY * minY);
        }

        // Convolve the actual data set to fit on the graph when drawn.
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
        img.redraw(id, new ColoredPointList(convolvedData, dataSet.getRgb()));
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
        int dataId1 = graph.addDataSet(Lists.newArrayList(new Point(0, 0), new Point(1, 0)),
                0x000000);
        // graph.redrawDataSet(dataId, testData(TEST_DATA_2, -13), 0xffffff);
        int dataId2 = graph.addDataSet(testData(TEST_DATA_2, -15), 0xffffff);
        // graph.recolorDataSet(dataId2, 0xff0000);
        // graph.removeDataSet(dataId1);
        // graph.redraw(true, true /* inScaleX */, true /* inScaleY */);
        JLabel picLabel = new JLabel(new ImageIcon(graph.img.getImage()));
        JOptionPane.showMessageDialog(null, picLabel, "About", JOptionPane.PLAIN_MESSAGE, null);
    }
}
