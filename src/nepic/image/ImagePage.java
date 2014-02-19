package nepic.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import nepic.data.Histogram;
import nepic.geo.BoundedRegion;
import nepic.geo.BoundingBox;
import nepic.roi.ConflictingRoisException;
import nepic.util.Verify;

// assumes 32-bit processor
public class ImagePage implements IdTaggedImage {

    /*
     * Pixel Properties in imgToAnal: ASSUMING BIT 0 IS LEAST SIGNIFICANT BIT
     *
     * Bit 00-07 = RL in original image Bit 08-15 = RL in gaussian image Bit 16 = Dirty bit for
     * Gaussian image Bit 17-26 = Sobel gradient Bit 27 = Dirty bit for Sobel gradient Bit 28-31 =
     * CandNum
     */
    private int[][] imgToAnal;// NOTE: ordered [x][y]
    public final int width;
    public final int height;

    // CONSTRUCTOR AND ASSOCIATED METHODS

    public ImagePage(int thePgWidth, int thePgHeight) {
        imgToAnal = new int[thePgWidth][thePgHeight];
        width = thePgWidth;
        height = thePgHeight;
    }

    @Override
    public boolean contains(int x, int y) {
        return x > -1 && x < width && y > -1 && y < height;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(0, width - 1, 0, height - 1);
    }

    @Override
    public boolean boundsContain(BoundedRegion region) {
        return region.getMinX() >= this.getMinX() && region.getMaxX() <= this.getMaxX()
                && region.getMinY() >= this.getMinY() && region.getMaxY() <= this.getMaxY();
    }

    @Override
    public boolean boundsContain(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
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

    // Set RGB values initially

    public void setRGB(int x, int y, byte relLum) { // TODO: don't really like this
        imgToAnal[x][y] = 255 & relLum;
    }

    // DISPLAY IMAGE

    public BufferedImage asImage(boolean equalizeHistogram) {
        BufferedImage toDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double multiplier = 1;
        int offset = 0;
        if(equalizeHistogram){
            Histogram imgHist = makeHistogram();
            offset = imgHist.getMin();
            multiplier = 255.0 / (imgHist.getMax() - offset);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pi = (int) Math.round(multiplier * (this.getPixelIntensity(x, y) - offset));
                toDisplay.setRGB(x, y, piToRgb(pi));
            }
        }
        return toDisplay;
    }

    /**
     * Converts the given pixel intensity (PI) to an RGB-formatted color. If the pixel intensity is
     * outside the range {@code 0-255}, it will be converted to this range before being converted
     * into a RGB color.
     *
     * @param pi the pixel intensity to convert
     * @return the RGB-formatted color corresponding to the given pixel intensity
     */
    private int piToRgb(int pi) {
        pi = pi > 255 ? 255 : pi < 0 ? 0 : pi;
        int rgbVal = 0;// sets alpha of RGB
        rgbVal = (rgbVal << 8) | pi;// sets red of rgb
        rgbVal = (rgbVal << 8) | pi;// sets green of rgb
        rgbVal = (rgbVal << 8) | pi;// sets blue of rgb
        return rgbVal;
    }

    // REL LUM (original)

    public static final int MIN_PIXEL_INTENSITY = 0;
    public static final int MAX_PIXEL_INTENSITY = 255;

    public int getPixelIntensity(int x, int y) {
        return (255 & imgToAnal[x][y]);
    }

    // CAND NUM

    // private static final int NOT_CAND_ID = 0; // This MUST remain 0
    public static final int MIN_CAND_ID = 1;
    public static final int MAX_CAND_ID = 15;
    private final RoiIds roiIds = new RoiIds();

    private class RoiIds {
        private int nxtAvailable; // First available ID handle
        private final RoiIdHandle[] handles;

        private RoiIds() {
            handles = new RoiIdHandle[MAX_CAND_ID - MIN_CAND_ID + 1];
            for (int id = MIN_CAND_ID; id <= MAX_CAND_ID; id++) {
                int pos = id - MIN_CAND_ID;
                handles[pos] = new RoiIdHandle(id, pos);
            }
            nxtAvailable = 0;
        }

        private RoiIdHandle requestIdHandle(Roi<?> caller) {
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
                throw new IllegalAccessException(
                        "Only the owner of the RoiIdHandle can release the handle.");
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

    RoiIdHandle requestIdHandle(Roi<?> caller) {
        return roiIds.requestIdHandle(caller);
    }

    public class RoiIdHandle {
        public final int id;
        private Roi<?> owner;
        private int pos;

        private RoiIdHandle(int id, int pos) {
            this.id = id;
            this.pos = pos;
            owner = null;
        }

        public void release(Roi<?> caller) throws IllegalAccessException {
            roiIds.releaseIdHandle(caller, this);
        }

        public boolean isOwnedBy(Roi<?> caller) {
            Verify.notNull(caller, "Owner of RoiIdHandle cannot be null.");
            return caller == owner;
        }
    }

    public static boolean candNumLegal(int candNum) {
        return candNum >= MIN_CAND_ID && candNum <= MAX_CAND_ID;
    }

    @Override
    public int getId(int x, int y) {
        return (15 & imgToAnal[x][y] >> 28);
    }

    public void setId(int x, int y, Roi<?> roi) throws ConflictingRoisException {
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
        imgToAnal[x][y] = (imgToAnal[x][y] | (newRoiNum << 28));// set new cand num
    }

    public void noLongerCand(int x, int y) {
        imgToAnal[x][y] = (imgToAnal[x][y] & 0x0fffffff);// set cand num back to zero
    }

    public Histogram makeHistogram() {
        Histogram.Builder imgHistBuilder = new Histogram.Builder(0, 255);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                imgHistBuilder.addValues(getPixelIntensity(x, y));
            }
        }
        return imgHistBuilder.build();
    }

    public int[] getDimensions() {
        return new int[] { width, height };
    }

    public boolean contains(Point pt) {
        int x = pt.x;
        int y = pt.y;
        return x > -1 && x < width && y > -1 && y < height;
    }

    public String printDraw() {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < height; x++) {
                builder.append(Integer.toHexString(getId(x, y)));
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }
}