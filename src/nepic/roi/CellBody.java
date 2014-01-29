package nepic.roi;

import java.awt.Point;
import java.util.List;

import nepic.geo.Blob;
import nepic.image.ImagePage;
import nepic.image.Roi;
import nepic.io.ComplexLabel;
import nepic.io.Label;
import nepic.data.Histogram;
import nepic.util.Pixel;

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
    private DataScanner[] cbEdgeFinders = new DataScanner[8];

    private int minPi;
    private int eThresh;

    CellBody(ImagePage img) {
        super(img);
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

    public DataScanner[] getEdgeFinders() {
        return cbEdgeFinders;
    }

    public DataScanner getEdgeFinder(int idx) {
        return cbEdgeFinders[idx];
    }

    public void setEdgeFinders(DataScanner[] edgeFinders) {
        cbEdgeFinders = edgeFinders;
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
