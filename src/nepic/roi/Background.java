package nepic.roi;

import java.awt.Point;
import java.util.List;

import nepic.image.ImagePage;
import nepic.image.Roi;
import nepic.io.ComplexLabel;
import nepic.io.Label;
import nepic.data.Histogram;
import nepic.roi.model.Polygon;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 */
public class Background extends Roi<BackgroundConstraint<?>> {
    private Histogram piHist = null;
    private Histogram edgeHist = null;

    private Polygon backgroundArea = null;
    private Point origin = null;
    private double theta;

    Background(ImagePage img) {
        super(img);
    }

    @Override
    public List<Point> getEdges() {
        return backgroundArea.getEdges();
    }

    @Override
    public List<Point> getInnards() {
        return backgroundArea.getInnards();
    }

    public Histogram getPiHist() {
        return piHist;
    }

    public Background setPiHist(Histogram piHist) {
        this.piHist = piHist;
        return this;
    }

    public Histogram getEdgeHist() {
        return edgeHist;
    }

    public Background setEdgeHist(Histogram edgeHist) {
        this.edgeHist = edgeHist;
        return this;
    }

    public Polygon getArea() {
        return backgroundArea;
    }

    public void setArea(Polygon newVal) {
        backgroundArea = newVal;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point newVal) {
        origin = newVal;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double newVal) {
        theta = newVal;
    }

    // thresh used for edges; any edge magnitude less than or equal to this is considered to be
    // 'flat'
    public int getEdgeThresh() {
        int threshElPos = edgeHist.getNumValues() * 99 / 100;// == 99th percentile element
        int elNum = 0;
        int eThresh = edgeHist.getMin();
        while (elNum < threshElPos) {
            elNum += edgeHist.getMagnitudeAt(eThresh);
            eThresh++;
        }// while
        return eThresh;
    }

    public static Label[] getCsvLabels() {
        Label[] histLabels = Histogram.getCsvLabels();
        return new Label[] {
                new ComplexLabel("PI_Hist", histLabels),
                new ComplexLabel("Edge_Hist", histLabels) };
    }

    @Override
    public Object[] getCsvData() {
        return new Object[] { piHist.getCsvData(), edgeHist.getCsvData() };
    }

    @Override
    public boolean isValid() {
        return backgroundArea != null && origin != null;
    }
}
