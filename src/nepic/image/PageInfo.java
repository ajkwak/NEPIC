package nepic.image;

import nepic.io.ComplexLabel;
import nepic.io.Label;
import nepic.data.Histogram;
import nepic.util.CsvFormattable;
import nepic.util.Validatable;
import nepic.util.Verify;

/**
 * @author AJ Parmidge
 */
public class PageInfo implements CsvFormattable, Validatable {
    // Page-specific info
    private final String imgName;
    private final int pgNum;
    private final Histogram imgHist;
    private Histogram calibrationBkHist;
    private int minGroupSize = -1; // Initialize to invalid value
    private int maxGroupSize = -1; // Initialize to invalid value
    private int optimalGroupSize = -1; // Initialize to invalid value

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

    public Histogram getPiHist() {
        return imgHist;
    }

    public void setCalibrationBkHist(Histogram hist) {
        this.calibrationBkHist = hist;
    }

    public static Label[] getCsvLabels() {
        return new Label[] {
                new Label("Name"),
                new Label("Pg_Num"),
                new ComplexLabel("Img Hist", Histogram.getCsvLabels()),
                new ComplexLabel("Calibration BK Hist", Histogram.getCsvLabels()),
                new Label("Min Group Size"),
                new Label("Max Group Size"),
                new Label("Optimal Group Size") };
    }

    @Override
    public Object[] getCsvData() {
        return new Object[] {
                imgName,
                pgNum,
                imgHist.getCsvData(),
                calibrationBkHist.getCsvData(),
                minGroupSize,
                maxGroupSize,
                optimalGroupSize, };
    }

    @Override
    public boolean isValid() {
        return calibrationBkHist != null;
    }
}
