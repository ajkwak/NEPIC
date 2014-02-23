
package nepic.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nepic.data.DataSet;
import nepic.data.GraphData;
import nepic.data.MutableDataSet;
import nepic.geo.LineSegment;
import nepic.geo.LineSegment.IncludeEnd;
import nepic.geo.LineSegment.IncludeStart;
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
     * The interval between the gridlines on the y-axis (the horizontal gridlines).
     */
    private int yGridlineInterval = -1; // Initialize to invalid number.
    /**
     * The interval between the gridlines on the x-axis (the vertical gridlines).
     */
    private int xGridlineInterval = -1; // Initialize to invalid number.
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

    /**
     * Sets the data underlying this {@link Graph} to the given {@link GraphData}.
     *
     * @param data the {@link GraphData} to set
     * @return {@code this}, for chaining
     */
    public Graph setData(GraphData data) {
        this.data = data;
        return this;
    }

    /**
     * Sets the interval between the gridlines on the x-axis (the vertical gridlines). If the given
     * gridline interval is invalid (non-positive), no vertical gridlines are drawn on this
     * {@link Graph}.
     *
     * @param xGridlineInterval the gridline interval to set on the x-axis
     * @return {@code this}, for chaining
     */
    public Graph setXGridlineInterval(int xGridlineInterval) {
        this.xGridlineInterval = xGridlineInterval;
        return this;
    }

    /**
     * Sets the interval between the gridlines on the y-axis (the horizontal gridlines). If the
     * given gridline interval is invalid (non-positive), no horizontal gridlines are drawn on this
     * {@link Graph}.
     *
     * @param xGridlineInterval the gridline interval to set on the y-axis
     * @return {@code this}, for chaining
     */
    public Graph setYGridlineInterval(int yGridlineInterval) {
        this.yGridlineInterval = yGridlineInterval;
        return this;
    }

    /**
     * Sets whether or not data points of the same {@link DataSet} in the underlying
     * {@link GraphData} should be connected in the graph.
     *
     * @return {@code this}, for chaining
     */
    public Graph connectDataPoints(boolean connect) {
        this.connectTheDots = connect;
        return this;
    }

    /**
     * Refreshes the graph after some change in the underlying {@link GraphData} was made or after a
     * control parameter of the graphing process was changed.
     *
     * @return {@code this}, for chaining
     */
    public Graph refresh() {
        img.clear();
        if (data != null) {
            ConvolutionData cData = determineCurrentConvolutionData();
            drawGridlines(cData);
            drawDataSets(cData);
        }
        return this;
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

    private ConvolutionData determineCurrentConvolutionData() {
        // Determine the multiplier and offset to graph this data set properly in the x direction.
        int minX = data.getMinX();
        int maxX = data.getMaxX();
        double xMultiplier;
        long xOffset;
        if (minX == maxX) {
            xMultiplier = 1;
            xOffset = img.getWidth() / 2;
        } else {
            xMultiplier = (double) (img.getWidth() - 1) / (maxX - minX);
            xOffset = 0 - Math.round(xMultiplier * minX);
        }

        // Determine the multiplier and offset to graph this data set properly in the y direction.
        int minY = data.getMinY();
        int maxY = data.getMaxY();
        int imgMaxY = img.getHeight() - 1;
        double yMultiplier;
        long yOffset;
        if (minY == maxY) {
            yMultiplier = 1;
            yOffset = img.getHeight() / 2;
        } else {
            yMultiplier = (double) imgMaxY / (maxY - minY);
            yOffset = 0 - Math.round(yMultiplier * minY);
        }

        return new ConvolutionData(xMultiplier, xOffset, yMultiplier, yOffset);
    }

    private void drawGridlines(ConvolutionData cData) {
        // Draw the x-gridlines.
        DataSet gridlines = new MutableDataSet().setRgb(0x444444 /* Light Gray */);
        if (xGridlineInterval > 0) { // If the xGridlineInterval is valid.
            for (int xGridline = determineMinimumGridline(data.getMinX(), xGridlineInterval); xGridline <= data
                    .getMaxX(); xGridline += xGridlineInterval) {
                int convolvedX = convolveX(xGridline, cData);
                for (int y = 0; y < img.getHeight(); y++) {
                    gridlines.add(new Point(convolvedX, y));
                }
            }
        }

        // Draw the y-gridlines
        if (yGridlineInterval > 0) {
            for (int yGridline = determineMinimumGridline(data.getMinY(), yGridlineInterval); yGridline <= data
                    .getMaxY(); yGridline += yGridlineInterval) {
                int convolvedY = convolveY(yGridline, cData);
                for (int x = 0; x < img.getWidth(); x++) {
                    gridlines.add(new Point(x, convolvedY));
                }
            }
        }

        // Annotate the gridlines onto the image.
        if (!gridlines.isEmpty()) {
            img.annotate(0, gridlines);
        }
    }

    private int determineMinimumGridline(int graphMin, int gridlineInterval) {
        int mod = graphMin % gridlineInterval;
        return mod == 0 ? graphMin : graphMin + (gridlineInterval - mod);
    }

    private void drawDataSets(ConvolutionData cData) {
        int id = 1;
        for (Pair<String, ? extends DataSet> dataEntry : data) {
            // Redraws the data set.
            DataSet convolvedData = convolveDataset(dataEntry.second, cData);
            if (connectTheDots) {
                DataSet connectedData = new MutableDataSet();
                connectedData.setRgb(convolvedData.getRgb());
                Point startPt = null;
                for (Point datum : convolvedData) {
                    Point endPt = datum;
                    if (startPt != null) {
                        List<Point> linePoints = new LineSegment(startPt, endPt)
                                .draw(IncludeStart.YES, IncludeEnd.NO);
                        for (Point point : linePoints) {
                            for (int x = Math.max(0, point.x - 1); x < Math.min(img.getWidth(), point.x + 2); x++) {
                                for (int y = Math.max(0, point.y - 1); y < Math.min(img.getHeight(), point.y + 2); y++) {
                                    connectedData.add(new Point(x, y));
                                }
                            }
                        }
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

    private DataSet convolveDataset(DataSet dataSet, ConvolutionData cData) {
        // Convolve the actual data set to fit on the graph when drawn.
        DataSet convolvedData = new MutableDataSet();
        convolvedData.setRgb(dataSet.getRgb());
        for (Point datum : dataSet) {
            Point convolvedDatum = new Point(convolveX(datum.x, cData), convolveY(datum.y, cData));
            convolvedData.add(convolvedDatum);
        }
        return convolvedData;
    }

    private int convolveX(int x, ConvolutionData cData) {
        return (int) Math.round(cData.xMultiplier * x + cData.xOffset);
    }

    private int convolveY(int y, ConvolutionData cData) {
        return (img.getHeight() - 1) - (int) Math.round(cData.yMultiplier * y + cData.yOffset);
    }

    /**
     * A simple class made for storing convolution data, which maps the data in the current
     * {@link GraphData} to pixels on the {@link Graph} image.
     *
     * @author AJ Parmidge
     */
    private class ConvolutionData {
        private final double xMultiplier;
        private final double yMultiplier;
        private final long xOffset;
        private final long yOffset;

        private ConvolutionData(double xMultiplier, long xOffset, double yMultiplier, long yOffset) {
            this.xMultiplier = xMultiplier;
            this.xOffset = xOffset;
            this.yMultiplier = yMultiplier;
            this.yOffset = yOffset;
        }
    }
}
