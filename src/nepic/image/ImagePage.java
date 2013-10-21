package nepic.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import nepic.roi.model.BoundingBox;
import nepic.roi.model.Polygon;
import nepic.data.Histogram;
import nepic.util.BoundedRegion;
import nepic.util.Pixel;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-6_093012 (Called ImgMatrix until AutoCBFinder_Alpha_v0-9-2013-01-29)
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 */
public class ImagePage implements IdTaggedImage {

    /*
     * Sec: |---ROI ID--|_____________________***FREE***__________________________|-Pix Intensity-|
     * Bit: 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0
     */
    private int[][] imgToAnal;// NOTE: ordered [x][y]
    public final int width;
    public final int height;

    // CONSTRUCTOR AND ASSOCIATED METHODS

    /**
     * Creates an empty ImageModel object
     *
     * @param thePgWidth
     * @param thePgHeight
     */
    public ImagePage(int thePgWidth, int thePgHeight) {
        imgToAnal = new int[thePgWidth][thePgHeight];
        width = thePgWidth;
        height = thePgHeight;
    }// PictureModel constructor

    /**
     *
     * @param x
     * @param y
     * @return true if the specified coordinate is within the boundaries of the image; otherwise
     *         false
     */
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

    public void setRGB(int x, int y, byte relLum) {
        imgToAnal[x][y] = 255 & relLum;
    }

    public BufferedImage displayImg() {
        BufferedImage toDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                toDisplay.setRGB(x, y, Pixel.relLumToRGB(imgToAnal[x][y] & 255));
            }// for all y
        }// for all x
        return toDisplay;
    }// displayImg

    // REL LUM (original)

    public static final int MIN_PIXEL_INTENSITY = 0;
    public static final int MAX_PIXEL_INTENSITY = 255;

    public int getPixelIntensity(int x, int y) {
        return (255 & imgToAnal[x][y]);
    }// getRelLum

    // protected int[] generateEdgeHistogram(int[] corners) {
    // int[] hist = new int[256];
    // for (int x = corners[0]; x <= corners[1]; x++) {
    // for (int y = corners[2]; y <= corners[3]; y++) {
    // int edgeMag = getEdgeMag(x, y);
    // if (edgeMag > 255)
    // edgeMag = 255;
    // hist[edgeMag]++;
    // }// for all y in img
    // }// for all x in img
    // return hist;
    // }// generateHistogram

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
            Verify.state(handles[pos1] == h1);
            Verify.state(handles[pos2] == h2);

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
    }// getCandNum

    public void setId(int x, int y, Roi<?> roi) throws IllegalAccessException {
        RoiIdHandle roiHandle = roi.getIdHandle();
        if (roiHandle.owner != roi) {
            throw new IllegalAccessException();
        }

        int newRoiNum = roiHandle.id;
        int currentRoiNum = getId(x, y);
        if (newRoiNum == currentRoiNum) {
            return;
        } else if (currentRoiNum != 0) {
            throw new IllegalStateException(new StringBuilder("Unable to set roiID of pixel (")
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
    }// setCandNum

    public void noLongerCand(int x, int y) {
        imgToAnal[x][y] = (imgToAnal[x][y] & 0x0fffffff);// set cand num back to zero
    }// setCandNum

    public Histogram makeHistogram() {
        Histogram.Builder histBuilder =
                new Histogram.Builder(0 /* Lower Bound */, 255 /* Upper Bound */);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                histBuilder.addValues(getPixelIntensity(x, y));
            }
        }
        return histBuilder.build();
    }

    public Histogram makeHistogram(BoundedRegion region) {
        Verify.argument(boundsContain(region), "Given region not contained within image bounds.");
        Histogram.Builder histBuilder =
                new Histogram.Builder(0 /* Lower Bound */, 255 /* Upper Bound */);
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                histBuilder.addValues(getPixelIntensity(x, y));
            }
        }
        return histBuilder.build();
    }

    public Histogram makeHistogram(Polygon p) {
        Histogram.Builder histBuilder = new Histogram.Builder(0, 255);
        for (Point pt : p.getInnards()) {
            if (contains(pt)) {
                histBuilder.addValues(getPixelIntensity(pt.x, pt.y));
            }
        }
        return histBuilder.build();
    }

    public int[] getDimensions() {
        return new int[] { width, height };
    }// getDimensions

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

}// ImgModel