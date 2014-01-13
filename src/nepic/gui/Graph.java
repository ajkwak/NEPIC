
package nepic.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nepic.data.DataSet;
import nepic.data.GraphData;
import nepic.data.UnorderedDataSet;
import nepic.roi.model.LineSegment;
import nepic.roi.model.LineSegment.IncludeEnd;
import nepic.roi.model.LineSegment.IncludeStart;
import nepic.util.Pair;
import nepic.util.Verify;

public class Graph extends JPanel {
    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -7635043290129019626L;
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
     * The image upon which all of the data sets are being graphed.
     */
    private final AnnotatableImage img;
    /**
     * The collection of all the data sets being graphed.
     */
    private GraphData data = null;

    private JLabel topLabel, bottomLabel;

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
    public Graph(int width, int height, int bkColor) {
        Verify.argument(width > 0 && height > 0, "Graph must have a positive width and height");
        setBackground(new Color(0x888888 /* dark grey */));

        img = new AnnotatableImage(newMonochromeImg(width, height, bkColor));

        topLabel = new JLabel();
        topLabel.setForeground(Color.white);
        add(topLabel);
        topLabel.setLocation(0, 0);
        topLabel.setSize(50, 25);
        topLabel.setVisible(true);

        bottomLabel = new JLabel();
        bottomLabel.setForeground(Color.white);
        add(bottomLabel);
        bottomLabel.setLocation(0, height - 25);
        bottomLabel.setSize(50, 25);
        bottomLabel.setVisible(true);

        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(new ImageIcon(img.getImage()));
        add(imgLabel);
        imgLabel.setLocation(0, 12);
        imgLabel.setSize(width, height - 24);
        imgLabel.setVisible(true);

        setLayout(null);
        setSize(width, height);
        setMinimumSize(getSize());
        setPreferredSize(getSize());
        setVisible(true);
    }

    public Graph setData(GraphData data) {
        // if (this.data != null) {
        // data.removeChangeListener(this);
        // }
        //
        // if (data == null) {
        // img.clear();
        // } else {
        // this.data = data;
        // data.addChangeListener(this);
        // }
        this.data = data;
        if (data != null && !data.isEmpty()) {
            refresh();
        }
        return this;
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
        img.clear();
        if (data != null) {
            // Draw the y-gridlines
            if (inScaleY) {
                int imgWidth = img.getWidth();
                int minY = data.getMinY();
                DataSet yGridlines = new UnorderedDataSet().setRgb(0x444444 /* Light Gray */);
                for (int yGridline = minY + (5 - minY % 5); yGridline <= data.getMaxY(); yGridline += 5) {
                    int convolvedY = convolveY(yGridline);
                    for (int x = 0; x < imgWidth; x++) {
                        yGridlines.add(new Point(x, convolvedY));
                    }
                }
                img.annotate(0, yGridlines);
            }

            // Draw the DataSets in the GraphData
            int id = 1;
            for (Pair<String, ? extends DataSet> dataEntry : data) {
                // Redraws the data set.
                DataSet convolvedData = convolveDatasetForDrawing(dataEntry.second);
                if (connectTheDots) {
                    DataSet connectedData = new UnorderedDataSet();
                    connectedData.setRgb(convolvedData.getRgb());
                    Point startPt = null;
                    for (Point datum : convolvedData) {
                        Point endPt = datum;
                        if (startPt != null) {
                            connectedData.addAll(
                                    new LineSegment(startPt, endPt).draw(IncludeStart.YES,
                                            IncludeEnd.NO));
                        }
                        startPt = endPt;
                    }
                    connectedData.add(startPt); // Adds the last point in the convolved data.
                    img.annotate(id, connectedData);
                } else {
                    img.annotate(id, convolvedData);
                }
                id++;
            }
            topLabel.setText("" + data.getMaxY());
            bottomLabel.setText("" + data.getMinY());
        }
    }

    public void refresh() {
        redraw(connectTheDots, inScaleX, inScaleY);
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


    private int convolveY(int y) { // assumes inScaleY is true
        // Determine the maxima and minima for graphing this data set.
        int minY = data.getMinY();
        int maxY = data.getMaxY();

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

        return imgMaxY - (int) Math.round(ratioY * y + offsetY); // return convolved y
    }

    private DataSet convolveDatasetForDrawing(DataSet dataSet) {
        // Determine the maxima and minima for graphing this data set.
        int minX = inScaleX ? data.getMinX() : dataSet.getMinX();
        int maxX = inScaleX ? data.getMaxX() : dataSet.getMaxX();
        int minY = inScaleY ? data.getMinY() : dataSet.getMinY();
        int maxY = inScaleY ? data.getMaxY() : dataSet.getMaxY();

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
        DataSet convolvedData = new UnorderedDataSet();
        convolvedData.setRgb(dataSet.getRgb());
        for (Point datum : dataSet) {
            Point convolvedDatum = new Point(
                    (int) Math.round(ratioX * datum.x + offsetX),
                    imgMaxY - (int) Math.round(ratioY * datum.y + offsetY));
            convolvedData.add(convolvedDatum);
        }
        return convolvedData;
    }

    /* TEST STUFF */

    // private static final int[] TEST_DATA_1 = new int[]{
    // 32,33,31,34,33,34,36,31,33,32,31,31,33,33,33,36,35,35,35,36,32,35,33,36,35,37,32,36,37,
    // 35,37,34,38,36,39,41,49,58,57,65,64,61,58,62,62,57,48,47,44,42,42,36,35,37,31,32,32,32,
    // 32,32,31,31,33,31,32,33,32,36,32,32,29,33,29,31,31,33,33,31,33,28,30,31,30,30};
    // private static final int[] TEST_DATA_2 = new int[]{
    // 25,29,21,23,26,24,23,27,26,29,27,25,25,27,28,27,25,24,27,25,28,28,27,27,30,27,28,27,27,26,
    // 28,26,27,28,30,28,29,27,25,30,31,28,27,25,27,30,27,31,30,29,47,55,71,88,90,75,62,52,43,39,
    // 30,34,32,32,29,28,28,29,30,29,30,31,30,32,30,30,31,32,29,29,30,32,32,27,29,26,26,29,26,29,
    // 29,28,27,29,28,29,28,32,27,30,32,30,27,28,30};
    //
    // private static List<Point> testData(int[] yVals, int xOffset) {
    // List<Point> testDataSet = new ArrayList<Point>(yVals.length);
    // for (int i = 0; i < yVals.length; i++) {
    // testDataSet.add(new Point(i + xOffset, yVals[i]));
    // }
    // return testDataSet;
    // }

    // public static void main(String[] args) {
    // GraphData data = new GraphData(5);
    // int dataId1 = data.addDataSet("Hello", testData(TEST_DATA_2, -13), 0x000000);
    //
    // Graph graph = new Graph(300, 300, 0xcc99ff, data);
        // // graph.redrawDataSet(dataId, testData(TEST_DATA_2, -13), 0xffffff);
        // int dataId2 = graph.addDataSet("World", testData(TEST_DATA_1, -15), 0xffffff);
        // graph.recolorDataSet(dataId2, 0xff0000);
        // graph.redraw(true, true, true);
        // graph.removeDataSet(dataId1);
        // graph.recolorDataSet(dataId1, 0x008800);
        // graph.redraw(true, true, true);
        // graph.recolorDataSet(dataId2, 0xff0000);
        // graph.removeDataSet(dataId1);
        // // graph.redraw(true, true /* inScaleX */, true /* inScaleY */);
        // JLabel picLabel = new JLabel(new ImageIcon(graph.img.getImage()));
    // JOptionPane.showMessageDialog(null, graph, "About", JOptionPane.PLAIN_MESSAGE, null);
    // }
}
