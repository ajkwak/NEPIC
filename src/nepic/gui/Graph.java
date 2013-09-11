package nepic.gui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import nepic.roi.model.LineSegment;
import nepic.util.ColoredPointList;
import nepic.util.DataSet;
import nepic.util.Verify;

public class Graph {
    private boolean drawToScale = false;
    // private boolean drawConnectingLines = true;
    // private int dataPointSize = 3;
    private int minX, maxX, minY, maxY;
    private final DataSet[] dataSets;

    private final AnnotatableImage img;
    private final int backgroundColor = 0x000000; // Black

    public Graph(int width, int height, int maxNumDataSets) {
        Verify.argument(width > 0 && height > 0, "Graph must have a positive width and height");
        Verify.argument(maxNumDataSets > 0,
                "The number of data sets expected in the graph must be greater than zero");
        dataSets = new DataSet[maxNumDataSets];
        img = new AnnotatableImage(maxNumDataSets).setImage(new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB));
    }

    public void draw(int id, List<? extends Point> newVals, int rgb) {
        Verify.argument(img.isValidId(id), "Invalid dataset id " + id);
        Verify.nonEmpty(newVals);

        // Make a DataSet out of the newVals
        dataSets[id] = new DataSet(newVals);
    }

    private void drawDataSet(int id, int rgb) {
        DataSet toDraw = dataSets[id];

        Iterator<Point> itr = toDraw.iterator();
        Point startPt = itr.next(); // Must be non-empty list.
        while (itr.hasNext()) {
            Point endPt = itr.next();// TODO: convolve so fits img. (also need do with start point)
            img.addPoints(id, new ColoredPointList(new LineSegment(startPt, endPt).draw(), rgb));
            startPt = endPt;
        }
    }

    public void recolorDataSet(int id, int rgb) {
        img.recolor(id, rgb);
    }

    public void eraseDataSet(int id) {
        img.erase(id);
    }

    private BufferedImage getBufferedImage() {
        return img.getImage();
    }

}
