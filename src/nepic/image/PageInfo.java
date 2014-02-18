package nepic.image;

import nepic.io.ComplexLabel;
import nepic.io.Label;
import nepic.roi.Background;
import nepic.roi.CellBody;
import nepic.data.Histogram;
import nepic.util.CsvFormattable;
import nepic.util.Validatable;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9_2013-01-08
 * @version AutoCBFinder_Alpha_v0-9_2013-01-08
 *
 */
public class PageInfo implements CsvFormattable, Validatable {
    // Page-specific info
    private final String imgName;
    private final int pgNum;
    private final Histogram imgHist;

    // Analysis-specific info
    private CellBody cb = null;
    private Background bk = null;

    public PageInfo(String imgName, int pgNum, ImagePage img) {
        Verify.notNull(imgName, "Name of image cannot be null");
        Verify.argument(pgNum > -1, "Image page number cannot be negative");
        Verify.notNull(img, "ImagePage to find info for cannot be null");
        this.imgName = imgName;
        this.pgNum = pgNum;
        this.imgHist = img.makeHistogram();
    }

    public String getImageName() {
        return imgName;
    }

    public int getPageNum() {
        return pgNum;
    }

    public CellBody getCB() {
        return cb;
    }

    public boolean hasValidCB() {
        return cb != null && cb.isValid();
    }

    public void setCB(CellBody cb) {
        this.cb = cb;
    }

    public Background getBK() {
        return bk;
    }

    public boolean hasValidBK() {
        return bk != null && bk.isValid();
    }

    public void setBK(Background bk) {
        this.bk = bk;
    }

    public Histogram getPiHist() {
        return imgHist;
    }

    public double getPiRatio() {
        Verify.state(cb != null && bk != null,
                "Neither the cell body nor the background for this PageInfo can be null!");
        return cb.getPiHist().getMean() / bk.getPiHist().getMean();
    }

    public double getHistogramOverlap() {
        return cb.getPiHist().getOverlapWith(bk.getPiHist());
    }

    public static Label[] getCsvLabels() {
        return new Label[] {
                new Label("Name"),
                new Label("Pg_Num"),
                new Label("PI Ratio (Cell Body/Background)"),
                new Label("CB:BK Histogram Overlap"),
                new ComplexLabel("ImgHist", Histogram.getCsvLabels()),
                new ComplexLabel("Cell Body", CellBody.getCsvLabels()),
                new ComplexLabel("Background", Background.getCsvLabels()), };
    }

    @Override
    public Object[] getCsvData() {
        return new Object[] {
                imgName,
                pgNum,
                getPiRatio(),
                getHistogramOverlap(),
                imgHist.getCsvData(),
                cb.getCsvData(),
                bk.getCsvData() };
    }

    @Override
    public boolean isValid() {
        return cb != null && bk != null; // TODO: check if cb and bk are valid???
    }

    public boolean hasValidRois() {
        return hasValidBK() && hasValidCB();
    }

}
