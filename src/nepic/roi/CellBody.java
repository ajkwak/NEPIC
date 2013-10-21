package nepic.roi;

import java.awt.Point;
import java.util.List;

import nepic.image.ImagePage;
import nepic.image.Roi;
import nepic.io.ComplexLabel;
import nepic.io.Label;
import nepic.roi.model.Blob;
import nepic.data.GraphData;
import nepic.data.Histogram;
import nepic.util.Pixel;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9-122212 (Called CellBody3 until AutoCBFinder_Alpha_v0-9_122212)
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 *
 */
public class CellBody extends Roi<CellBodyConstraint<?>> {
    private Pixel seedPixel;
    private Blob cbArea;
    private Histogram piHist;
    private GraphData[] graphData;

    public enum GraphDataAngle {
        ZERO(0),
        PI_OVER_FOUR(1),
        PI_OVER_TWO(2),
        NEGATIVE_PI_OVER_FOUR(3);

        private final int id;

        private GraphDataAngle(int id) {
            this.id = id;
        }
    }

    private int minPi;
    private int eThresh;

    CellBody(ImagePage img) {
        super(img);
        graphData = new GraphData[4];
    }

    @Override
    public List<Point> getEdges() {
        return cbArea.getEdges();
    }

    @Override
    public List<Point> getInnards() {
        return cbArea.getInnards();
    }

    public Pixel getSeedPixel() {
        return seedPixel;
    }

    public CellBody setSeedPixel(Pixel newVal) {
        seedPixel = newVal;
        return this;
    }

    public Blob getArea() {
        return cbArea;
    }

    public CellBody setEdges(Blob newVal) {
        cbArea = newVal;
        return this;
    }

    public Histogram getPiHist() {
        return piHist;
    }

    public CellBody setPiHist(Histogram newVal) {
        piHist = newVal;
        return this;
    }

    public int getMinPi() {
        return minPi;
    }

    public void setMinPi(Integer newVal) {
        minPi = newVal;
    }

    public Integer getEdgeThresh() {
        return eThresh;
    }

    public void setEdgeThresh(Integer newVal) {
        eThresh = newVal;
    }

    public void setGraphData(GraphDataAngle angle, GraphData data) {
        Verify.notNull(angle, "GraphDataAngle");
        Verify.notNull(data, "GraphData");
        graphData[angle.id] = data;
    }

    public GraphData getGraphData(GraphDataAngle angle) {
        Verify.notNull(angle, "GraphDataAngle");
        return graphData[angle.id];
    }

    @Override
    public boolean isValid() {
        return cbArea != null && piHist != null;
    }

    public static Label[] getCsvLabels() {
        Label[] histLabels = Histogram.getCsvLabels();
        return new Label[] {
                new Label("minX"),
                new Label("maxX"),
                new Label("minY"),
                new Label("maxY"),
                new Label("Length"),
                new ComplexLabel("PI_Hist", histLabels) };
    }

    @Override
    public Object[] getCsvData() {
        return new Object[] {
                cbArea.getMinX(),
                cbArea.getMaxX(),
                cbArea.getMinY(),
                cbArea.getMaxY(),
                cbArea.getMaxDiameter().getLength(),
                piHist.getCsvData() };
    }

}
