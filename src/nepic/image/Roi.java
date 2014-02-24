package nepic.image;

import java.awt.Point;
import java.util.List;

import nepic.Nepic;
import nepic.image.ImagePage;
import nepic.image.ImagePage.RoiIdHandle;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.util.CsvFormattable;
import nepic.util.Validatable;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 * @param <C>
 */
public abstract class Roi implements CsvFormattable, Validatable {
    /**
     * The unique ID number of this {@link Roi}.
     */
    private RoiIdHandle idHandle; // should correspond to candNum
    private boolean modifiedSinceAccepted = true;

    protected Roi(ImagePage img) {
        Verify.notNull(img, "Cannot make ROI for null image page.");
        idHandle = img.requestIdHandle(this);
    }

    public int getId() {
        return idHandle.id;
    }

    RoiIdHandle getIdHandle() {
        return idHandle;
    }

    /**
     * Releases the ID of this ROI. Note that this makes the ROI invalid, so this should only be
     * done when the ROI is about to be deleted.
     */
    public void release() {
        try {
            idHandle.release(this);
        } catch (IllegalAccessException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to release handle with id =",
                    idHandle.id, this, "is NOT the owner of the handle",
                    EventLogger.formatException(e));
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

    /**
     *
     * @param img
     * @since AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    public void revalidate(ImagePage img) {
        // Revalidate only if the ROI currently has an invalid handle.
        if (!idHandle.isOwnedBy(this)) {
            idHandle = img.requestIdHandle(this);
        }
    }

    public boolean invalidate() {
        try {
            idHandle.release(this);
            return true;
        } catch (IllegalAccessException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to invalidate ROI with id",
                    idHandle, EventLogger.formatException(e));
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "(" + getId() + ")";
    }

}