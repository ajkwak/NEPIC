package nepic.image;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 * @param <C>
 * @param <RoiImpl>
 */
public abstract class RoiFinder<RoiImpl extends Roi> {
    protected ImagePage img = null;

    public RoiFinder() {
        // Dummy constructor
    }

    // public abstract void initialize(RoiImpl roi); // Initialize for tracking and verification

    public void setImage(ImagePage img) {
        // Verify.notNull(img);
        this.img = img;
    }

    public abstract RoiImpl createFeature(ConstraintMap constraints);

    public abstract boolean editFeature(RoiImpl roi, ConstraintMap constraints);

    public abstract void removeFeature(RoiImpl roi);

    public abstract void acceptFeature(RoiImpl roi);

    /**
     *
     * @param validRoi
     * @since AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    public abstract boolean restoreFeature(RoiImpl validRoi);

    protected List<Point> getAllPixelsInRoi(int roiNum) {
        List<Point> roiPixs = new LinkedList<Point>();
        for (int x = 0; x < img.width; x++) {
            for (int y = 0; y < img.height; y++) {
                if (img.getId(x, y) == roiNum) {
                    roiPixs.add(new Point(x, y));
                }
            }
        }
        return roiPixs;
    }

}
