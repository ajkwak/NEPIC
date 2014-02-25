package nepic.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import nepic.data.Histogram;
import nepic.geo.BoundedRegion;
import nepic.geo.BoundingBox;
import nepic.roi.ConflictingRoisException;
import nepic.util.Verify;

// assumes 32-bit processor
public class ImagePage implements IdTaggedImage {
    private static final int ID_LENGTH = 4;
    private static final int ID_OFFSET = 28;
    private static final int MAX_ID = (1 << ID_LENGTH) - 1;
    private static final int PI_LENGTH = 8;
    private static final int MAX_PI = (1 << PI_LENGTH) - 1;

    /**
     * The width of this {@link ImagePage}.
     */
    public final int width;
    /**
     * The height of this {@link ImagePage}.
     */
    public final int height;

    private final RoiIds roiIds = new RoiIds();
    /**
     * <pre>
     *  0000 0000 00000000 00000000 00000000
     * |-ID-|----------------------|---PI---|
     * </pre>
     */
    private int[][] imgToAnal;// NOTE: ordered [x][y]

    /**
     * Creates an {@link ImagePage} with the given dimensions.
     *
     * @param width the width of the constructed image page
     * @param height the height of the constructed image page
     */
    public ImagePage(int width, int height) {
        imgToAnal = new int[width][height];
        this.width = width;
        this.height = height;
    }

    public BufferedImage asImage(boolean equalizeHistogram) {
        BufferedImage toDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double multiplier = 1;
        int offset = 0;
        if (equalizeHistogram) {
            Histogram imgHist = makeHistogram();
            offset = imgHist.getMin();
            multiplier = ((double) MAX_PI) / (imgHist.getMax() - offset);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pi = (int) Math.round(multiplier * (this.getPixelIntensity(x, y) - offset));
                toDisplay.setRGB(x, y, piToRgb(pi));
            }
        }
        return toDisplay;
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        return region.getMinX() >= this.getMinX()
                && region.getMaxX() <= this.getMaxX()
                && region.getMinY() >= this.getMinY()
                && region.getMaxY() <= this.getMaxY();
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public boolean contains(int x, int y) {
        return boundsContain(x, y);
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(0, width - 1, 0, height - 1);
    }

    public Dimension getDimensions() {
        return new Dimension(width, height);
    }

    @Override
    public int getId(int x, int y) {
        return MAX_ID & (imgToAnal[x][y] >> ID_OFFSET);
    }

    @Override
    public int getMinX() {
        return 0;
    }

    @Override
    public int getMaxX() {
        return width - 1;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMaxY() {
        return height - 1;
    }

    public int getNumPixels() {
        return width * height;
    }

    public int getPixelIntensity(int x, int y) {
        return MAX_PI & imgToAnal[x][y];
    }

    public Histogram makeHistogram() {
        Histogram.Builder imgHistBuilder = new Histogram.Builder(0, MAX_PI);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                imgHistBuilder.addValues(getPixelIntensity(x, y));
            }
        }
        return imgHistBuilder.build();
    }

    public String printDraw() {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < height; x++) {
                builder.append(Integer.toHexString(getId(x, y))).append(" ");
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }

    RoiIdHandle requestIdHandle(Roi caller) {
        return roiIds.requestIdHandle(caller);
    }

    public void associatePixelWithRoi(int x, int y, Roi roi){
        //
    }

    public void dissociatePixelWithRoi(int x, int y, Roi roi){
        int id = roi.getId();
        if (getId(x, y) == id) {
            // Assumes ID includes most significant bit.
            int setIdToZeroMask = -1 >>> (32 - ID_OFFSET);
            imgToAnal[x][y] = imgToAnal[x][y] & setIdToZeroMask;
        }
    }

    public void setId(int x, int y, Roi roi) throws ConflictingRoisException {
        RoiIdHandle roiHandle = roi.getIdHandle();
        Verify.argument(roiHandle.owner == roi, "RoiIdHandle owner is " + roiHandle.owner
                + ", not " + roi + ".  Unable to set ID.");
        int newRoiNum = roiHandle.id;
        int currentRoiNum = getId(x, y);
        if (newRoiNum == currentRoiNum) {
            return;
        } else if (currentRoiNum != 0) {
            throw new ConflictingRoisException(new StringBuilder("Unable to set roiID of pixel (")
                    .append(x)
                    .append(", ")
                    .append(y)
                    .append(") in image page to the ROI ")
                    .append(roi)
                    .append(".  This point is already part of another ROI (id = ")
                    .append(currentRoiNum)
                    .append(").")
                    .toString());
        }
        imgToAnal[x][y] = (imgToAnal[x][y] | (newRoiNum << ID_OFFSET));// set new cand num
    }

    public void setRGB(int x, int y, byte relLum) {
        imgToAnal[x][y] = MAX_PI & relLum;
    }

    private int piToRgb(int pi) {
        pi = pi > MAX_PI ? MAX_PI : pi < 0 ? 0 : pi;
        int rgbVal = 0;// sets alpha of RGB
        rgbVal = (rgbVal << 8) | pi;// sets red of rgb
        rgbVal = (rgbVal << 8) | pi;// sets green of rgb
        rgbVal = (rgbVal << 8) | pi;// sets blue of rgb
        return rgbVal;
    }

    public class RoiIdHandle {
        public final int id;
        private Roi owner;
        private int pos;

        private RoiIdHandle(int id, int pos) {
            this.id = id;
            this.pos = pos;
            owner = null;
        }

        public void release(Roi caller) throws IllegalAccessException {
            roiIds.releaseIdHandle(caller, this);
        }

        public boolean isOwnedBy(Roi caller) {
            Verify.notNull(caller, "Owner of RoiIdHandle cannot be null.");
            return caller == owner;
        }
    }

    private class RoiIds {
        private int nxtAvailable; // First available ID handle
        private final RoiIdHandle[] handles;

        private RoiIds() {
            handles = new RoiIdHandle[MAX_ID];
            for (int id = 1; id <= MAX_ID; id++) {
                int pos = id - 1;
                handles[pos] = new RoiIdHandle(id, pos);
            }
            nxtAvailable = 0;
        }

        private RoiIdHandle requestIdHandle(Roi caller) {
            Verify.state(nxtAvailable < handles.length, "No more ROI ID handles available!");
            RoiIdHandle requestedHandle = handles[nxtAvailable];
            requestedHandle.owner = caller;
            nxtAvailable++;
            return requestedHandle;
        }

        private void releaseIdHandle(Object caller, RoiIdHandle handle)
                throws IllegalAccessException {
            Verify.argument(caller != null && handle != null,
                    "Handle to release cannot be null, and null caller cannot release a handle");
            if (caller != handle.owner) {
                throw new IllegalAccessException("Only the owner of the RoiIdHandle ("
                        + handle.owner + ") can release the handle.");
            }
            handle.owner = null;
            nxtAvailable--;
            swap(handle, handles[nxtAvailable]);
        }

        private void swap(RoiIdHandle h1, RoiIdHandle h2) {
            int pos1 = h1.pos;
            int pos2 = h2.pos;
            Verify.state(handles[pos1] == h1, "Handle at position of h1.pos (=" + pos1
                    + ") is NOT " + h1 + ".  Instead, is " + handles[pos1]);
            Verify.state(handles[pos2] == h2, "Handle at position of h2.pos (=" + pos2
                    + ") is NOT " + h2 + ".  Instead, is " + handles[pos2]);

            handles[pos1] = h2;
            h2.pos = pos1;
            handles[pos2] = h1;
            h1.pos = pos2;
        }
    }
}