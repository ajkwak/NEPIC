package nepic.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Stack;

import nepic.data.Histogram;
import nepic.geo.BoundedRegion;
import nepic.geo.BoundingBox;
import nepic.roi.ConflictingRoisException;
import nepic.util.Verify;

// assumes 32-bit processor
public class ImagePage implements IdTaggedImage {
    /**
     * The ID of pixels that are not associated with a particular {@link Roi}.
     */
    public static final int NON_ROI_ID = 0;

    private static final int ID_LENGTH = 4; // The number of bits in the 'ID' field.
    private static final int ID_OFFSET = 28;
    private static final int MAX_ID = (1 << ID_LENGTH) - 1;
    private static final int PI_LENGTH = 8; // The number of bits in the 'Pixel Intensity' field.
    private static final int MAX_PI = (1 << PI_LENGTH) - 1;

    /**
     * The width of this {@link ImagePage}.
     */
    public final int width;
    /**
     * The height of this {@link ImagePage}.
     */
    public final int height;

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

    public void associatePixelWithRoi(int x, int y, Roi roi) throws ConflictingRoisException {
        int newId = roi.getId();
        int currentId = getId(x, y);
        if (currentId == newId) {
            return; // This pixel is already associated with the given Roi.
        } else if (currentId != 0) { // Then pixel is already associated with a DIFFERENT Roi.
            throw new ConflictingRoisException(new StringBuilder("Unable to associate pixel (")
                    .append(x)
                    .append(", ")
                    .append(y)
                    .append(") with Roi ")
                    .append(roi)
                    .append(".  This pixel is already associated with Roi (id = ")
                    .append(currentId)
                    .append(").")
                    .toString());
        }
        imgToAnal[x][y] = imgToAnal[x][y] | (newId << ID_OFFSET);
    }

    public void dissociatePixelWithRoi(int x, int y, Roi roi){
        int id = roi.getId();
        if (getId(x, y) == id) {
            // Assumes ID includes most significant bit.
            int setIdToZeroMask = -1 >>> (32 - ID_OFFSET);
            imgToAnal[x][y] = imgToAnal[x][y] & setIdToZeroMask;
        }
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

    // private Roi[] rois = new Roi[MAX_ID]; // TODO: keep this??
    private Stack<Integer> availableIds = initializeAvailableIds();

    int requestId() {
        Verify.state(!availableIds.isEmpty(), "No more Roi IDs available on this ImagePage.");
        return availableIds.pop();
    }

    void releaseId(int id) {
        Verify.argument(id >= 1 && id <= MAX_ID, "Cannot release invalid ID " + id);
        Verify.state(!availableIds.contains(id), "ID is already released");
        availableIds.push(id);
    }

    private Stack<Integer> initializeAvailableIds() {
        Stack<Integer> availableIds = new Stack<Integer>();
        for (int id = 1; id <= MAX_ID; id++) {
            availableIds.push(id);
        }
        return availableIds;
    }
}