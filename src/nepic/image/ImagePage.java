package nepic.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import nepic.roi.model.BoundingBox;
import nepic.roi.model.Histogram;
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

    // DISPLAY IMAGE

    /*
     * FOR THE displayImg() METHOD: bits 0-4 describe 31 - the number of pixels to grab for the
     * displayed image (doing this subtraction decreases calculation of bitsToGrab by 1 flop bits
     * 5-9 describe how many pixels to shift to get bits corresponding to img to display
     */
    public static final int ORIG_IMG = 0x17;// shift 0, grab 8 bits; binary = 00000 10111 = 00 0001
                                            // 0111
    public static final int GAUS_IMG = 0x117;// shift 8, grab 8 bits; binary = 01000 10111 = 01 0001
                                             // 0111
    public static final int EDGE_IMG = 0x215;// shift 16, grab 10 bits; binary = 10000 10101 = 10
                                             // 0001 0101

    public BufferedImage displayImg(int whichImg) {
        int bitsToGrab = ~(0x80000000 >> (31 & whichImg));// Gives 1's only in the last (number of
                                                          // bits to grab) positions
        int bitsToShft = 31 & (whichImg >> 5);
        BufferedImage toDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                toDisplay.setRGB(x, y, Pixel
                        .relLumToRGB((bitsToGrab & (imgToAnal[x][y] >> bitsToShft))));
            }// for all y
        }// for all x
        return toDisplay;
    }// displayImg

    protected BufferedImage displayImgSec(int[] corners, int magFactor, int whichImg) {
        int bitsToGrab = ~(0x80000000 >> (31 & whichImg));// Gives 1's only in the last (number of
                                                          // bits to grab) positions
        int bitsToShft = 31 & (whichImg >> 5);
        BufferedImage toDisplay = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                toDisplay.setRGB(x, y, Pixel
                        .relLumToRGB((bitsToGrab & (imgToAnal[x][y] >> bitsToShft))));
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

    // REL LUM (gauss)

    private static final short[] GAUSSIAN_FILTER = new short[] {
            1,
            4,
            7,
            4,
            1,
            4,
            16,
            26,
            16,
            4,
            7,
            26,
            41,
            26,
            7,
            4,
            16,
            26,
            16,
            4,
            1,
            4,
            7,
            4,
            1 };

    protected int getSmoothedRL(int x, int y) {
        if ((1 & (imgToAnal[x][y] >> 16)) == 0)// if Gauss dirty bit still dirty
            setSmoothedRL(x, y, applyGauss5Filter(x, y));
        return (0xff & (imgToAnal[x][y] >> 8));
    }// getSmoothedRL

    private void setSmoothedRL(int x, int y, int rl) {
        rl = 0x100 | rl;// set dirty bit for Gauss image to 1
        imgToAnal[x][y] = (imgToAnal[x][y] & 0xffff00ff) | ((0x1ff & rl) << 8);
    }// setSmoothedRL

    public int applyGauss5Filter(int x, int y) {
        int newVal = 0;
        for (int row = 0; row < 5; row++) {
            int yLoc = y + row - 2;
            if (yLoc > -1 && yLoc < height) {
                for (int column = 0; column < 5; column++) {
                    int xLoc = x + column - 2;
                    if (xLoc > -1 && xLoc < width) {
                        newVal = newVal + GAUSSIAN_FILTER[row * 5 + column]
                                * getPixelIntensity(xLoc, yLoc);
                    }// if should include element
                }// for all columns in filter
            }// if yLoc is within range
        }// for all rows in filter
        return newVal / 273;// divide by the sum of the elements in the filter
    }// applyFilter

    protected int[] generateGausHistogram(int[] corners) {
        int[] hist = new int[256];
        for (int x = corners[0]; x <= corners[1]; x++) {
            for (int y = corners[2]; y <= corners[3]; y++) {
                hist[getSmoothedRL(x, y)]++;
            }// for all y in img
        }// for all x in img
        return hist;
    }// generateHistogram

    // SOBEL IMG

    public int getEdgeMag(int x, int y) {
        if ((1 & (imgToAnal[x][y] >> 27)) == 0)// if Sobel dirty bit still dirty
            setEdgeMag(x, y, applySobelFilter(x, y));
        return 0x3ff & (imgToAnal[x][y] >> 17);// returns bits 17-26
    }// getEdgeMag

    private void setEdgeMag(int x, int y, int edgeMag) {
        edgeMag = 0x400 | edgeMag;// put 1 in Sobel dirty bit section
        imgToAnal[x][y] = ((imgToAnal[x][y] & 0xf801ffff) | ((0x7ff & edgeMag) << 17));// also sets
                                                                                       // dirty bit
                                                                                       // to one
    }// setEdgeMag

    public void makeSobelImg(int[] corners) {
        for (int x = corners[0]; x <= corners[1]; x++) {
            for (int y = corners[2]; y <= corners[3]; y++) {
                setEdgeMag(x, y, applySobelFilter(x, y));
            }// for all non-edge y in image
        }// for all non-edge x in image
    }// makeGaussAndSorbellImgs

    private int applySobelFilter(int x, int y) {
        if (x < 1 || x > width - 1 || y < 1 || y > height - 1)// if given invalid parameter (can't
                                                              // apply Sobel filter to edgePix)
            return 0;
        int gradX = -1 * getSmoothedRL(x - 1, y - 1) + 1 * getSmoothedRL(x - 1, y + 1) + -2
                * getSmoothedRL(x, y - 1) + 2 * getSmoothedRL(x, y + 1) + -1
                * getSmoothedRL(x + 1, y - 1) + 1 * getSmoothedRL(x + 1, y + 1);
        int gradY = -1 * getSmoothedRL(x - 1, y - 1) + -2 * getSmoothedRL(x - 1, y) + -1
                * getSmoothedRL(x - 1, y + 1) + 1 * getSmoothedRL(x + 1, y - 1) + 2
                * getSmoothedRL(x + 1, y) + 1 * getSmoothedRL(x + 1, y + 1);
        return (Math.abs(gradX) + Math.abs(gradY)) / 2;// since result will always be even
    }// makeEdgeImage

    protected int[] generateEdgeHistogram(int[] corners) {
        int[] hist = new int[256];
        for (int x = corners[0]; x <= corners[1]; x++) {
            for (int y = corners[2]; y <= corners[3]; y++) {
                int edgeMag = getEdgeMag(x, y);
                if (edgeMag > 255)
                    edgeMag = 255;
                hist[edgeMag]++;
            }// for all y in img
        }// for all x in img
        return hist;
    }// generateHistogram

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

        // private Roi<?> getHandleOwner(int roiId) {
        // Verify.argument(candNumLegal(roiId), "Given ROI ID is illegal");
        // for (int i = 0; i < nxtAvailable; i++) {
        // if (handles[i].id == roiId) {
        // return handles[i].owner;
        // }
        // }
        // return null;
        // }

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

    // public Roi<?> getRoiAt(Point pt) {
    // int roiId = getId(pt.x, pt.y);
    // return roiIds.getHandleOwner(roiId);
    // }

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

    public void setId(int x, int y, Roi<?> roi) {
        RoiIdHandle roiHandle = roi.getIdHandle();
        Verify.argument(roiHandle.owner == roi); // TODO: Really, should throw
                                                 // IllegalAccessException
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
        Histogram imgHist = Histogram.newPixelIntensityHistogram();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                imgHist.addData(getPixelIntensity(x, y));
            }
        }
        return imgHist;
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