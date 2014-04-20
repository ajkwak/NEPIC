package nepic.roi;

// Need methods from CBIdentifier.java from AutoCBFinder_Alpha_v0-3

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import nepic.Nepic;
import nepic.geo.Blob;
import nepic.geo.BoundingBox;
import nepic.geo.Line;
import nepic.geo.LineSegment;
import nepic.geo.Polygon;
import nepic.geo.RoiEdgeTracer;
import nepic.image.ConstraintMap;
import nepic.image.Roi;
import nepic.image.RoiFinder;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.data.Histogram;
import nepic.util.DoubleLinkRing;
import nepic.util.Pair;
import nepic.util.Pixel;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 */
public class CellBodyFinder extends RoiFinder<CellBody> {
    public static final String SEED_POLYGON = "Seed Polygon";
    public static final String DESIRED_SIZE = "Desired Size";

    public enum SizeEdgeCase {
        BIGGER,
        SMALLER,
        AS_CLOSE_AS_POSSIBLE;
    }

    @Override
    public CellBody createFeature(ConstraintMap constraints) { // XXX
        CellBody roi = new CellBody(img);

        // SeedPolygon constraint
        Polygon seedPolygon = (Polygon) constraints.getConstraint(SEED_POLYGON);
        if (seedPolygon == null) {
            // Use the whole image to find the seedPixel
            seedPolygon = img.getBoundingBox().asPolygon();
        }
        try{
            adjustToSeedPolygon(roi, seedPolygon);
        }catch(ConflictingRoisException e){
            Nepic.log(EventType.WARNING, "Seed pixel of CellBody conflicts with another ROI", e
                    .getMessage());
            return null;
        }

        // Determine if CellBody can be found in this area
        Point seedPixel = roi.getSeedPixel();

        // System.out.println("img width = " + img.width + ", height  = " + img.height);


        List<Point> edges = new LinkedList<Point>();
        edges.add(seedPixel);
        try {
            roi.setEdgeFinders(processScanlines(seedPixel));
            int minPi = smoothAndDeterminePiThreshold(seedPixel);

            // Extend edges to MinPi
            extendEdges(roi, minPi);
            generateNewCellBodyHistogram(roi);

            // DesiredSize constraint
            @SuppressWarnings("unchecked")
            Pair<Integer, SizeEdgeCase> desiredSize =
                    (Pair<Integer, SizeEdgeCase>) constraints.getConstraint(DESIRED_SIZE);
            if (desiredSize != null) {
                adjustToDesiredSize(roi, desiredSize.first, desiredSize.second);
            }
        } catch (NoSuchFieldException e) {
            Nepic.log(EventType.WARNING, "Edges of CellBody not found.");
            // TODO: in future, simply return null, but for now, have this workaround
            roi.setEdges(Blob.newBlobFromTracedEdges(edges)); // Only includes the seed pixel.
        } catch (ConflictingRoisException e) {
            return roi;
        }
        return roi;
    }

    @Override
    public boolean editFeature(CellBody roi, ConstraintMap constraints) {
        // Only adjustable CellBody Constraint is currently desiredSize
        @SuppressWarnings("unchecked")
        Pair<Integer, SizeEdgeCase> desiredSize =
                (Pair<Integer, SizeEdgeCase>) constraints.getConstraint(DESIRED_SIZE);
        if (desiredSize != null) {
            adjustToDesiredSize(roi, desiredSize.first, desiredSize.second);
        }
        return true;
    }

    @Override
    public void removeFeature(CellBody roi) {
        removeFeatureFromImage(roi);
        // roi.setPiHist(null);
        // roi.clear();
    }

    @Override
    public void acceptFeature(CellBody roi) {
        roi.setModified(false);
        removeFeatureFromImage(roi);
    }

    @Override
    public boolean restoreFeature(CellBody validRoi) {
        Verify.notNull(validRoi, "ROI to restore cannot be null.");
        Verify.argument(validRoi.isValid(), "ROI to restore must be valid");
        validRoi.revalidate(img); // Give valid ROI an Id handle for this image
        Blob roiArea = validRoi.getArea();
        List<Point> restoredPts = new LinkedList<Point>();
        try {
            for (Point edgePt : roiArea.getEdges()) {
                img.associatePixelWithRoi(edgePt.x, edgePt.y, validRoi);
                restoredPts.add(edgePt);
            }
            for (Point innardPt : roiArea.getInnards()) {
                img.associatePixelWithRoi(innardPt.x, innardPt.y, validRoi);
                restoredPts.add(innardPt);
            }
            return true;
        } catch (ConflictingRoisException e) {
            return false;
        }
    }

    private void adjustToSeedPolygon(CellBody roi, Polygon seedPolygon)
            throws ConflictingRoisException {
        Pixel seedPixel = getMostIntensePixel(seedPolygon);
        if (seedPixel == null) {
            throw new ConflictingRoisException("Entire seedPolygon is within another Roi");
        }

        // Set seed pixel
        img.associatePixelWithRoi(seedPixel.x, seedPixel.y, roi);
        roi.setSeedPixel(seedPixel);
        roi.setMinPi(seedPixel.color);

        // Set edges
        List<Point> initEdges = new LinkedList<Point>();
        initEdges.add(seedPixel);
        roi.setEdges(Blob.newBlobFromTracedEdges(initEdges));
    }

    /**
     * Adjusts the size of the ROI to be as close to the indicated desired size as possible
     *
     * @param roi the ROI to adjust
     * @param desiredSize the desired size of the ROI
     */
    private void adjustToDesiredSize(CellBody roi, int desiredSize, SizeEdgeCase edgeCase) {
        // Get current ROI info
        final Point seedPix = roi.getSeedPixel();
        Verify.state(seedPix != null,
                "CellBody ROI must have a seed pixel before the desired size can be set.");
        if (roi.getArea() == null) {
            List<Point> edges = new LinkedList<Point>();
            edges.add(seedPix);
            roi.setEdges(Blob.newBlobFromTracedEdges(edges));
        }

        int currSize = roi.getArea().getSize();
        if (currSize < desiredSize || (currSize == desiredSize && edgeCase == SizeEdgeCase.BIGGER)) {
            enlargeToDesiredSize(roi, desiredSize, edgeCase);
        } else if (currSize != desiredSize || edgeCase != SizeEdgeCase.AS_CLOSE_AS_POSSIBLE) {
            shrinkToDesiredSize(roi, desiredSize, edgeCase);
        }
    }

    // private static final int changePiIncrement = 5;

    private int determineChangePiIncrementForImg() {
        int imgPiRangeLength = img.makeHistogram().getRange().length();
        int changePiIncrement = imgPiRangeLength / 25;
        if (changePiIncrement < 1) {
            changePiIncrement = 1;
        }
        return changePiIncrement;
    }

    private boolean enlargeToDesiredSize(CellBody roi, int desiredSize, SizeEdgeCase edgeCase) {
        int changePiIncrement = determineChangePiIncrementForImg();
        int minPi = roi.getMinPi();
        int imgSize = img.getNumPixels();
        int initCbSize = roi.getArea().getSize();
        int size = initCbSize;
        if (size == imgSize) {
            Nepic.log(EventType.INFO, "Unable to enlarge candidate further.");
            return false;
        }
        int prevSize = size;
        Pixel prevSeedPixel = roi.getSeedPixel(); // Kept in case need to revert last enlargement.

        try {
            while (size < desiredSize && size < imgSize) {
                prevSize = size;
                prevSeedPixel = roi.getSeedPixel();
                minPi -= changePiIncrement;
                extendEdges(roi, minPi);
                size = roi.getArea().getSize();
            }
        } catch (ConflictingRoisException e) {
            Nepic.log(EventType.WARNING, e.getMessage());
            return false;
        }


        try {
            // Re-shrink once, if necessary
            if (edgeCase == SizeEdgeCase.SMALLER || (edgeCase == SizeEdgeCase.AS_CLOSE_AS_POSSIBLE
                    && (desiredSize - prevSize < size - desiredSize))) {
                removeFeatureFromImage(roi);
                minPi += changePiIncrement;
                roi.setSeedPixel(prevSeedPixel); // Don't retain seed pixel from latest enlargement.
                img.associatePixelWithRoi(prevSeedPixel.x, prevSeedPixel.y, roi);
                roi.setEdges(Blob.newBlobFromTracedEdges(Lists.newArrayList(prevSeedPixel)));
                if (prevSize > 1) {
                    extendEdges(roi, minPi);
                }
            }
        } catch (ConflictingRoisException e) {
            // This should never happen!!
            Nepic.log(EventType.FATAL_ERROR, "Shrink during candidate enlargement failed!",
                    EventLogger.formatException(e));
            return false;
        }

        roi.setMinPi(minPi);
        generateNewCellBodyHistogram(roi);
        Nepic.log(EventType.VERBOSE, "Candidate enlarged.  Size of candidate now: "
                + roi.getArea().getSize() + ".  MinPI = " + roi.getMinPi());
        return true;
    }

    private boolean shrinkToDesiredSize(CellBody roi, int desiredSize, SizeEdgeCase edgeCase) {
        int changePiIncrement = determineChangePiIncrementForImg();
        final Pixel seedPix = roi.getSeedPixel();
        final int seedPixPi = seedPix.color;
        int minPi = roi.getMinPi();
        int size = roi.getArea().getSize();
        int prevSize = size;
        if (minPi == seedPixPi || size == 1) {
            Nepic.log(EventType.INFO, "Unable to shrink candidate further.");
            return false;
        }

        try {
            while (size > desiredSize) {
                // Remove feature so can extend to shrink
                removeFeatureFromImage(roi);
                img.associatePixelWithRoi(seedPix.x, seedPix.y, roi);
                roi.setEdges(Blob.newBlobFromTracedEdges(Lists.newArrayList(seedPix)));

                // Extend cell body edges to a higher minPi (net shrink action)
                prevSize = size;
                minPi += changePiIncrement;
                if (minPi > seedPixPi) {
                    minPi = seedPixPi;
                    System.out.println("minPI should now be " + seedPixPi);
                }
                extendEdges(roi, minPi);
                size = roi.getArea().getSize();
                if (minPi == seedPixPi) {
                    break;
                }
            }

            // Re-enlarge once, if necessary
            if (edgeCase == SizeEdgeCase.BIGGER
                    || (edgeCase == SizeEdgeCase.AS_CLOSE_AS_POSSIBLE && (desiredSize - prevSize > size
                            - desiredSize))) {
                minPi -= changePiIncrement;
                extendEdges(roi, minPi);
            }
            generateNewCellBodyHistogram(roi);
            Nepic.log(EventType.VERBOSE, "Candidate shrunk.  Size of candidate now: "
                    + roi.getArea().getSize() + ". MinPI now " + roi.getMinPi());
            return true;
        } catch (ConflictingRoisException e) {
            // THIS SHOULD NEVER HAPPEN.
            Nepic.log(EventType.FATAL_ERROR, "Edge extention during CB shrinkage failed!",
                    EventLogger.formatException(e));
            return false;
        }
    }

    /**
     * Sets the edges of the ROI to the specified {@link Blob}, and initializes the histograms of
     * the ROI based upon the pixels contained in the {@link Blob}.
     *
     * @param roi the ROI whose edges need to e set
     * @param newEdges the new edges of the ROI
     */
    private void generateNewCellBodyHistogram(CellBody roi) {
        // Make histogram for cb
        Histogram.Builder cbPiHistBuilder = new Histogram.Builder(0, 255);
        for (Point cbPt : roi.getInnards()) {
            // For all points in the cell body
            cbPiHistBuilder.addValues(img.getPixelIntensity(cbPt.x, cbPt.y));
        }
        for (Point cbPt : roi.getEdges()) {
            // For all points in the cell body
            cbPiHistBuilder.addValues(img.getPixelIntensity(cbPt.x, cbPt.y));
        }
        roi.setPiHist(cbPiHistBuilder.build());
    }

    /**
     * Clears the specified ROI from the image. More specifically, clears the ROI number from all
     * pixels in the image previously in the ROI.
     *
     * @param roiNum the ID number of the ROI to remove from the image
     * @param roiEdges the blob indicating the location of the ROI in the image
     */
    private void removeFeatureFromImage(CellBody roi) {
        for (Point edgePix : roi.getEdges()) {
            img.dissociatePixelWithRoi(edgePix.x, edgePix.y, roi);
        }
        for (Point innardPix : roi.getInnards()) {
            img.dissociatePixelWithRoi(innardPix.x, innardPix.y, roi);
        }

        // Check : TODO remove this code!
        List<Point> unclearedPixs = getAllPixelsInRoi(roi.getId());
        int numUnclearedPixs = unclearedPixs.size();
        if (numUnclearedPixs > 0) {
            System.out.println(roi.getArea().toString());
        }
        Verify.argument(numUnclearedPixs == 0, "Not all pixels in ROI (ID = " + roi.getId()
                + ") removed! " + numUnclearedPixs + " uncleared pixels remain:\n\t"
                + unclearedPixs);
    }

    /**
     * Finds the most intense pixel in the innards of the {@link Polygon} parameter.
     *
     * @param secCorners the polygon in which to find the most intense pixel
     * @return the most intense pixel in the innards of the polygon
     */
    private Pixel getMostIntensePixel(Polygon secCorners) {
        List<Point> pixs = secCorners.getInnards();
        int centerPosX = (secCorners.getMaxX() + secCorners.getMinX()) / 2;
        int centerPosY = (secCorners.getMaxY() + secCorners.getMinY()) / 2;
        int cDiff = -1;
        int maxLum = -1;
        int xPos = -1;
        int yPos = -1;
        for (Point pix : pixs) {
            int x = pix.x;
            int y = pix.y;
            if (img.getId(x, y) == 0) {
                int ndiffX = Math.abs(x - centerPosX);
                int ndiffY = Math.abs(y - centerPosY);
                int nDiff = ndiffX * ndiffX + ndiffY * ndiffY;
                int rl = img.getPixelIntensity(x, y);
                if (rl > maxLum || (rl == maxLum && nDiff < cDiff)) {
                    maxLum = rl;
                    xPos = x;
                    yPos = y;
                    cDiff = nDiff;
                }// if found new possible max
            }// if not already in a previous candidate
        }
        if (maxLum < 0) { // If no seed pixel found.
            return null;
        }
        return new Pixel(xPos, yPos, maxLum);
    }// findMostIntensePixClump

    private int smoothAndDeterminePiThreshold(Point seedPixel) throws NoSuchFieldException {
        BoundingBox upperLeftQuadrant = new BoundingBox(0 /* minX */,
                seedPixel.x /* maxX */, 0 /* minY */, seedPixel.y /* maxY */);
        BoundingBox upperRightQuadrant = new BoundingBox(
                seedPixel.x /* minX */, img.width - 1 /* maxX */, 0 /* minY */,
                seedPixel.y /* maxY */);
        BoundingBox lowerLeftQuadrant = new BoundingBox(0 /* minX */,
                seedPixel.x /* maxX */, seedPixel.y /* minY */, img.height - 1 /* maxY */);
        BoundingBox lowerRightQuadrant = new BoundingBox(
                seedPixel.x /* minX */, img.width - 1 /* maxX */,
                seedPixel.y /* minY */, img.height - 1 /* maxY */);

        Line verticalLine = new Line(seedPixel, -Math.PI / 2);
        Line fortyFiveDegreeLine = new Line(seedPixel, Math.PI / 4);
        Line horizontalLine = new Line(seedPixel, 0);
        Line negFortyFiveDegreeLine = new Line(seedPixel, -Math.PI / 4);

        return getPiThreshForScanlines(
                seedPixel,
                horizontalLine.boundTo(upperRightQuadrant), // 0 Degrees.
                fortyFiveDegreeLine.boundTo(lowerRightQuadrant), // 45 Degrees.
                verticalLine.boundTo(lowerRightQuadrant), // 90 Degrees.
                negFortyFiveDegreeLine.boundTo(lowerLeftQuadrant), // 135 Degrees.
                horizontalLine.boundTo(lowerLeftQuadrant), // 180 Degrees.
                fortyFiveDegreeLine.boundTo(upperLeftQuadrant), // -135 Degrees.
                verticalLine.boundTo(upperLeftQuadrant), // -90 Degrees.
                negFortyFiveDegreeLine.boundTo(upperRightQuadrant)); // -45 Degrees.
    }

    private DataScanner[] processScanlines(Point seedPixel){
        DataScanner[] scanners = new DataScanner[4];
        scanners[0] = new DataScanner(
                getImgPixsForScanline(new Line(seedPixel, 0).boundTo(img))); // Horizontal.
        scanners[1] = new DataScanner(
                getImgPixsForScanline(new Line(seedPixel, Math.PI / 4).boundTo(img))); // 45 Deg.
        scanners[2] = new DataScanner(
                getImgPixsForScanline(new Line(seedPixel, -Math.PI / 2).boundTo(img))); // Vertical.
        scanners[3] = new DataScanner(
                getImgPixsForScanline(new Line(seedPixel, -Math.PI / 4).boundTo(img))); // -45 Deg.
        return scanners;
    }

    private int getPiThreshForScanlines(Point seedPix, LineSegment... scanlines)
            throws NoSuchFieldException {
        List<Integer> threshPis = new ArrayList<Integer>(8);
        for (int i = 0; i < scanlines.length; i++) {
            DataScanner scanner = new DataScanner(getImgPixsForScanline(scanlines[i]));
            int threshPi = getProcessedThreshPiForData(scanner);
            if (threshPi >= 0 && threshPi <= 255) { // If is valid.
                threshPis.add(threshPi);
            }
        }

        if (threshPis.size() < 2) {
            throw new NoSuchFieldException(
                    "No CellBody found.  Unable to find PI threshold.");
        }

        double avgThreshPi = 0;
        for(int threshPi : threshPis){
            avgThreshPi+= threshPi;
        }
        avgThreshPi /= threshPis.size();

        return (int) Math.round(
                avgThreshPi + 0.25 * (img.getPixelIntensity(seedPix.x, seedPix.y) - avgThreshPi));

        //return threshPis.get((int) Math.round(0.75 * (threshPis.size() - 1)));
    }

    private List<Integer> getImgPixsForScanline(LineSegment scanline) {
        List<Point> scanlinePoints = scanline.draw();
        List<Integer> pis = new ArrayList<Integer>(scanlinePoints.size());
        for (Point pt : scanlinePoints) {
            pis.add(img.getPixelIntensity(pt.x, pt.y));
        }
        return pis;
    }

    private int getProcessedThreshPiForData(DataScanner scanner) {
        List<Integer> processedData = scanner.getProcessedData();

        // The threshPI is the first pixel intensity with 4 consecutive buckets.
        int currPI = -1; // invalid PI
        int numConsecutiveBuckets = 0;
        for (int bucketPI : processedData) {
            if (bucketPI == currPI) {
                numConsecutiveBuckets++;
                if (numConsecutiveBuckets == 4) {
                    return currPI;
                }
            } else {
                currPI = bucketPI;
                numConsecutiveBuckets = 1;
            }
        }

        return -1; // invalid value
    }

    // returns maxXPoint
    private void extendEdges(CellBody roi, int minPi) throws ConflictingRoisException {
        DoubleLinkRing<Point> candEdges = new DoubleLinkRing<Point>();
        int roiId = roi.getId();
        for (Point edgePt : roi.getEdges()) {
            Verify.argument(img.getId(edgePt.x, edgePt.y) == roiId,
                    "edgePt is not in ROI!  Expected RoiNum = " + roiId + ", actual RoiNum of "
                            + edgePt + " = " + img.getId(edgePt.x, edgePt.y));
            candEdges.add(edgePt);
        }
        List<Point> extendedBy = new LinkedList<Point>();
        try {
            Point maxXPix = null;
            Iterator<Point> edgePixItr = candEdges.iterator();
            int numErrors = 0; // TODO: eventually remove, currently used for tracing
            while (edgePixItr.hasNext() && numErrors < 20) {
                Point ePixel = edgePixItr.next();
                int ePixX = ePixel.x;
                int ePixY = ePixel.y;
                if (ePixX > 0 && img.getId(ePixX - 1, ePixY) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX - 1, ePixY);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX - 1, ePixY, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                        if (checkIfAdd > roi.getSeedPixel().color) { // Then ePixel is new seedPix.
                            roi.setSeedPixel(new Pixel(ePixX, ePixY, checkIfAdd));
                        }
                    }// if add checkIfAdd to cand
                }// if pixel to left of pixel being examined is not already in cand
                if (ePixX < img.width - 1 && img.getId(ePixX + 1, ePixY) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX + 1, ePixY);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX + 1, ePixY, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                        if (checkIfAdd > roi.getSeedPixel().color) { // Then ePixel is new seedPix.
                            roi.setSeedPixel(new Pixel(ePixX, ePixY, checkIfAdd));
                        }
                    }// if add checkIfAdd to cand
                }// if pixel to the right of pixel being examined is not already in cand
                if (ePixY > 0 && img.getId(ePixX, ePixY - 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY - 1);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX, ePixY - 1, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                        if (checkIfAdd > roi.getSeedPixel().color) { // Then ePixel is new seedPix.
                            roi.setSeedPixel(new Pixel(ePixX, ePixY, checkIfAdd));
                        }
                    }// if add checkIfAdd to cand
                }// if pixel above pixel being examined not already in cand
                if (ePixY < img.height - 1 && img.getId(ePixX, ePixY + 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY + 1);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX, ePixY + 1, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                        if (checkIfAdd > roi.getSeedPixel().color) { // Then ePixel is new seedPix.
                            roi.setSeedPixel(new Pixel(ePixX, ePixY, checkIfAdd));
                        }
                    }// if add checkIfAdd to cand
                }// if pixel below pixel being examined not already in cand
                if (maxXPix == null || ePixel.x > maxXPix.x) {
                    maxXPix = ePixel;
                }
                edgePixItr.remove();
            }// while: check all pixels in clump's edges
            if (numErrors > 0) {
                Nepic.log(EventType.ERROR,
                        "Unable to extend edges of candidate; too many errors detected.");
            }
            if (!extendedBy.isEmpty()) {
                roi.setEdges(Blob.newBlobFromTracedEdges((RoiEdgeTracer.traceOuterEdges(img, roiId,
                        maxXPix))));
                roi.setModified(true); // CellBody is ONLY modified if edges were extended.
            }
            roi.setMinPi(minPi);
        } catch (ConflictingRoisException e) {
            // If enlarge into another ROI
            for (Point pt : extendedBy) {
                img.dissociatePixelWithRoi(pt.x, pt.y, roi);
            }
            Nepic.log(EventType.VERBOSE, "Unable to extend CellBody edges to " + minPi, ":",
                    e.getMessage());
            throw new ConflictingRoisException("Unable to further extend CellBody edges.  "
                    + "Enlarged CellBody conflicts with an existing ROI.");
        }
    }// extendEdges

    private boolean shouldAddPixel(Point toAdd, int minPI) {
        // Only add if at least four of the surrounding pixels are ALSO over the minPI.
        if (img.getPixelIntensity(toAdd.x, toAdd.y) >= minPI) {
            // Then check the surrounding pixels.
            int numSurroundingPixelsOverThresh = 0;
            if (toAdd.x > 0) {
                if (img.getPixelIntensity(toAdd.x - 1, toAdd.y) >= minPI) {
                    numSurroundingPixelsOverThresh++;
                }
            }
            if (toAdd.x < img.width - 1) {
                if (img.getPixelIntensity(toAdd.x + 1, toAdd.y) >= minPI) {
                    numSurroundingPixelsOverThresh++;
                }
            }
            if (toAdd.y > 0) {
                if (img.getPixelIntensity(toAdd.x, toAdd.y - 1) >= minPI) {
                    numSurroundingPixelsOverThresh++;
                }
            }
            if (toAdd.y < img.height - 1) {
                if (img.getPixelIntensity(toAdd.x, toAdd.y + 1) >= minPI) {
                    numSurroundingPixelsOverThresh++;
                }
            }
            if (numSurroundingPixelsOverThresh >= 3) {
                return true;
            }
        }
        return false;
    }

    private int tryToAdd(Pixel toAdd, DoubleLinkRing<Point> candEdges, Roi roi)
            throws ConflictingRoisException {
        int numErrors = 0;

        if (img.getId(toAdd.x, toAdd.y) == roi.getId()) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "CB Cand already contains", toAdd);
            numErrors += 1;
        } else {
            img.associatePixelWithRoi(toAdd.x, toAdd.y, roi);
        }

        if (candEdges.contains(toAdd)) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "candEdges already contains", toAdd);
            numErrors += 1;
        } else {
            candEdges.addLast(toAdd);
        }

        return numErrors;
    }
}
