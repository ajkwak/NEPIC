package nepic.image;

import java.awt.Point;
import java.util.List;

import nepic.image.ImagePage;
import nepic.util.CsvFormattable;
import nepic.util.Validatable;
import nepic.util.Verify;

/**
 * @author AJ Parmidge
 */
public abstract class Roi implements CsvFormattable, Validatable {
    /**
     * The unique ID number of this {@link Roi}.
     */
    private int id; // should correspond to candNum
    private boolean modifiedSinceAccepted = true;
    private ImagePage img;

    protected Roi(ImagePage img) {
        Verify.notNull(img, "Cannot make ROI for null image page.");
        this.img = img;
        id = img.requestId();
    }

    public int getId() {
        return id;
    }

    /**
     * Releases the ID of this ROI. Note that this makes the ROI invalid, so this should only be
     * done when the ROI is about to be deleted.
     */
    public void release() {
        if (img != null) {
            img.releaseId(id);
            id = 0; // Invalid value.
            img = null;
        }
    }

    public boolean isModified() {
        return modifiedSinceAccepted;
    }

    public void setModified(boolean modified) {
        modifiedSinceAccepted = modified;
    }

    public abstract List<Point> getEdges();

    public abstract List<Point> getInnards();

    public void revalidate(ImagePage img) {
        // Release this ROI on its old ImagePage, if applicable.
        release();

        // Get an ID for the current image.
        this.img = img;
        id = img.requestId();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "(" + getId() + ")";
    }

}