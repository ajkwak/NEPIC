package nepic.roi;

// Need methods from CBIdentifier.java from AutoCBFinder_Alpha_v0-3

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.image.ConstraintMap;
import nepic.image.Roi;
import nepic.image.RoiFinder;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.roi.model.Blob;
import nepic.roi.model.BoundingBox;
import nepic.roi.model.Histogram;
import nepic.roi.model.Line;
import nepic.roi.model.LineSegment;
import nepic.roi.model.Polygon;
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
public class CellBodyFinder extends RoiFinder<CellBodyConstraint<?>, CellBody> {

    @Override
    public CellBody createFeature(ConstraintMap<CellBodyConstraint<?>> constraints) {
        CellBody roi = new CellBody(img);

        // SeedPolygon constraint
        Polygon seedPolygon = constraints.getConstraint(SeedPolygon.class);
        if (seedPolygon == null) {
            // Use the whole image to find the seedPixel
            seedPolygon = img.getBoundingBox().asPolygon();
        }
        adjustToSeedPolygon(roi, seedPolygon);

        // EdgeThresh constraint
        Integer eThresh = constraints.getConstraint(EdgeThresh.class); // TODO: what if no eThresh
        adjustToEdgeThresh(roi, eThresh);

        // Determine if CellBody can be found in this area
        Point seedPixel = roi.getSeedPixel();

        // System.out.println("img width = " + img.width + ", height  = " + img.height);


        List<Point> edges = new LinkedList<Point>();
        edges.add(seedPixel);
        try {
            roi.setEdgeFinders(processScanlines(seedPixel));
            int minPi = determinePiThreshold(eThresh, seedPixel);
            Nepic.log(EventType.VERBOSE, "minPI = " + minPi);

            // Extend edges to MinPi
            edges = extendEdges(edges, roi, minPi);
            roi.setMinPi(minPi);
            setRoiEdges(roi, new Blob(edges));

            // DesiredSize constraint
            Pair<Integer, SizeEdgeCase> desiredSize = constraints.getConstraint(DesiredSize.class);
            if (desiredSize != null) {
                adjustToDesiredSize(roi, desiredSize.first, desiredSize.second);
            }
        } catch (NoSuchFieldException e) {
            Nepic.log(EventType.WARNING, "Edges of CellBody not found.");
            // TODO: in future, simply return null, but for now, have this workaround
            roi.setEdges(new Blob(edges)); // Does NOT make a valid CellBody object
        }
        return roi;
    }

    @Override
    public CellBody editFeature(CellBody roi, ConstraintMap<CellBodyConstraint<?>> constraints) {
        roi.setModified(true);
        // Only adjustable CellBody Constraint is currently desiredSize
        Pair<Integer, SizeEdgeCase> desiredSize = constraints.getConstraint(DesiredSize.class);
        if (desiredSize != null) {
            adjustToDesiredSize(roi, desiredSize.first, desiredSize.second);
        }
        return roi;
    }

    @Override
    public void removeFeature(CellBody roi) {
        removeFeatureFromImage(roi.getId(), roi.getArea());
        // roi.setPiHist(null);
        // roi.clear();
    }

    @Override
    public void acceptFeature(CellBody roi) {
        roi.setModified(false);
        removeFeatureFromImage(roi.getId(), roi.getArea());
    }

    @Override
    public void restoreFeature(CellBody validRoi) {
        Verify.notNull(validRoi, "ROI to restore cannot be null.");
        Verify.argument(validRoi.isValid(), "ROI to restore must be valid");
        validRoi.revalidate(img); // Give valid ROI an Id handle for this image
        Blob roiArea = validRoi.getArea();
        for (Point edgePt : roiArea.getEdges()) {
            img.setId(edgePt.x, edgePt.y, validRoi);
        }
        for (Point innardPt : roiArea.getInnards()) {
            img.setId(innardPt.x, innardPt.y, validRoi);
        }
    }

    private void adjustToSeedPolygon(CellBody roi, Polygon seedPolygon) {
        Pixel seedPixel = getMostIntensePixel(seedPolygon);

        // Set seed pixel
        img.setId(seedPixel.x, seedPixel.y, roi);
        roi.setSeedPixel(seedPixel);
        roi.setMinPi(seedPixel.relLum);

        // Set edges
        List<Point> initEdges = new LinkedList<Point>();
        initEdges.add(seedPixel);
        roi.setEdges(new Blob(initEdges)); // sets edges without setting up histogram, etc
    }

    private void adjustToEdgeThresh(CellBody roi, int edgeThresh) {
        roi.setEdgeThresh(edgeThresh);
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
            roi.setEdges(new Blob(edges));
        }

        int currSize = roi.getArea().getSize();
        if (currSize < desiredSize || (currSize == desiredSize && edgeCase == SizeEdgeCase.BIGGER)) {
            enlargeToDesiredSize(roi, desiredSize, edgeCase);
        } else if (currSize != desiredSize || edgeCase != SizeEdgeCase.AS_CLOSE_AS_POSSIBLE) {
            shrinkToDesiredSize(roi, desiredSize, edgeCase);
        }
    }

    private static final int changePiIncrement = 5;

    private boolean enlargeToDesiredSize(CellBody roi, int desiredSize, SizeEdgeCase edgeCase) {
        Blob cb = roi.getArea();
        int minPi = roi.getMinPi();
        int imgSize = img.getNumPixels();
        int size = cb.getSize();
        if (size == imgSize) {
            Nepic.log(EventType.INFO, "Unable to enlarge candidate further.");
            return false;
        }
        int prevSize = size;
        List<Point> edges = cb.getEdges();
        final int roiNum = roi.getId();

        while (size < desiredSize && size < imgSize) {
            prevSize = size;
            minPi -= changePiIncrement;
            edges = extendEdges(edges, roi, minPi);
            cb = new Blob(edges);
            size = cb.getSize();
        }

        // Re-shrink once, if necessary
        if (edgeCase == SizeEdgeCase.SMALLER
                || (edgeCase == SizeEdgeCase.AS_CLOSE_AS_POSSIBLE && (desiredSize - prevSize < size
                        - desiredSize))) {
            removeFeatureFromImage(roiNum, cb);
            minPi += changePiIncrement;
            edges.clear();
            Point seedPix = roi.getSeedPixel();
            edges.add(seedPix);
            img.setId(seedPix.x, seedPix.y, roi);
            if (prevSize > 1) {
                edges = extendEdges(edges, roi, minPi);
                cb = new Blob(edges);
            }
        }
        roi.setMinPi(minPi);
        setRoiEdges(roi, cb);
        Nepic.log(EventType.VERBOSE, "Candidate enlarged.  Size of candidate now: " + cb.getSize());
        return true;
    }

    private boolean shrinkToDesiredSize(CellBody roi, int desiredSize, SizeEdgeCase edgeCase) {
        final Pixel seedPix = roi.getSeedPixel();
        final int seedPixPi = seedPix.relLum;
        int minPi = roi.getMinPi();
        if (minPi == seedPixPi) {
            Nepic.log(EventType.INFO, "Unable to shrink candidate further.");
            return false;
        }

        Blob cb = roi.getArea();
        List<Point> edges = cb.getEdges();
        final int roiNum = roi.getId();
        int size = roi.getArea().getSize();
        int prevSize = size;

        while (size > desiredSize) {
            // Remove feature so can extend to shrink
            removeFeatureFromImage(roiNum, cb);
            edges.clear();
            edges.add(seedPix);
            img.setId(seedPix.x, seedPix.y, roi);

            // Extend cell body edges to a higher minPi (net shrink action)
            prevSize = size;
            minPi += changePiIncrement;
            if (minPi > seedPixPi) {
                minPi = seedPixPi;
            }
            edges = extendEdges(edges, roi, minPi);
            cb = new Blob(edges);
            size = cb.getSize();
            if (minPi == seedPixPi) {
                break;
            }
        }

        // Re-enlarge once, if necessary
        if (edgeCase == SizeEdgeCase.BIGGER
                || (edgeCase == SizeEdgeCase.AS_CLOSE_AS_POSSIBLE && (desiredSize - prevSize > size
                        - desiredSize))) {
            minPi -= changePiIncrement;
            edges = extendEdges(edges, roi, minPi);
            cb = new Blob(edges);
        }
        roi.setMinPi(minPi);
        setRoiEdges(roi, cb);
        Nepic.log(EventType.VERBOSE, "Candidate shrunk.  Size of candidate now: " + cb.getSize());
        return true;
    }

    /**
     * Sets the edges of the ROI to the specified {@link Blob}, and initializes the histograms of
     * the ROI based upon the pixels contained in the {@link Blob}.
     *
     * @param roi the ROI whose edges need to e set
     * @param newEdges the new edges of the ROI
     */
    private void setRoiEdges(CellBody roi, Blob newEdges) {
        roi.setEdges(newEdges);

        // Make histogram for cb
        Histogram cbPiHist = Histogram.newPixelIntensityHistogram();
        for (Point cbPt : newEdges.getInnards()) {
            // For all points in the cell body
            cbPiHist.addData(img.getPixelIntensity(cbPt.x, cbPt.y));
        }
        roi.setPiHist(cbPiHist);
    }

    /**
     * Clears the specified ROI from the image. More specifically, clears the ROI number from all
     * pixels in the image previously in the ROI.
     *
     * @param roiNum the ID number of the ROI to remove from the image
     * @param roiEdges the blob indicating the location of the ROI in the image
     */
    private void removeFeatureFromImage(int roiNum, Blob roiEdges) {// Does NOT clear the roi
        for (Point edgePix : roiEdges.getEdges()) {
            img.noLongerCand(edgePix.x, edgePix.y);
        }
        for (Point innardPix : roiEdges.getInnards()) {
            img.noLongerCand(innardPix.x, innardPix.y);
        }

        // Check : TODO remove this code!
        List<Point> unclearedPixs = getAllPixelsInRoi(roiNum);
        int numUnclearedPixs = unclearedPixs.size();
        if (numUnclearedPixs > 0) {
            roiEdges.printDraw(unclearedPixs);
        }
        Verify.argument(numUnclearedPixs == 0, "Not all pixels in ROI (ID = " + roiNum
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
        Verify.argument(img.getPixelIntensity(xPos, yPos) == maxLum); // TODO remove
        return new Pixel(xPos, yPos, maxLum);
    }// findMostIntensePixClump

    /**
     *
     * @param threshVals
     * @return
     * @throws NoSuchFieldException
     */
    private int determinePiThreshold(int eThresh, Point seedPix)
            throws NoSuchFieldException {
        // Generate possible threshold values.
        int numThreshVals = 8;
        List<Point> threshVals = new ArrayList<Point>(numThreshVals);
        Point threshPix;
        for (int changeX = -1; changeX <= 1; changeX++) {// find thresh every 45 degrees
            for (int changeY = -1; changeY <= 1; changeY++) {
                threshPix = findThresh(seedPix, eThresh, changeX, changeY);// seed pixel is maxX in
                                                                           // clump,
                // move right
                if (threshPix != null) {
                    threshVals.add(threshPix);
                }
            }// for y
        }// for x

        // Determines the actual pixel intensity threshold
        if (threshVals.size() < numThreshVals / 2) {
            Nepic.log(EventType.VERBOSE,
                    "Unable to find edges of cell body using raw data.  Processing the data");
            return smoothAndDeterminePiThreshold(seedPix) + 1;
        }
        int minRL = 256;
        int secMin = 256;
        for (Point threshPt : threshVals) {
            int threshLum = img.getPixelIntensity(threshPt.x, threshPt.y);
            if (threshLum < minRL)
                minRL = threshLum;
            else if (threshLum > minRL && threshLum < secMin)
                secMin = threshLum;
        }// while
        return secMin;
    }// determineThreshold

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


    /**
     * Finds the threshold value in a single direction away from the seed pixel.
     *
     * @param seedPixel the seed pixel from which to search for the ROI threshold
     * @param eThresh the edge threshold used to search for the pixel intensity threshold
     * @param changeX the x direction to go from the seed pixel in search of the threshold value
     * @param changeY the y direction to go from the seed pixel in search of the threshold value
     * @return the threshold point
     */
    private Point findThresh(Point seedPixel, int eThresh, int changeX, int changeY) {
        if (changeX == 0 && changeY == 0)
            return null;
        // IDEA: keep going until get to downward gradient followed by flat section (~10 pixels).
        // ThreshPix is the last local max downward gradient before the flat section
        int numDown = 0;// must always be at least two pixels before start looking for flat section
        int totalDrop = 0;// use in combo with numDown. Drop must 1) long, or 2) large
        Pixel maxDownPix = null;// threshPix
        int numUp = 0;

        int xToCheck = seedPixel.x + changeX;
        int yToCheck = seedPixel.y + changeY;
        int currentRelLum = img.getPixelIntensity(seedPixel.x, seedPixel.y);
        int currentEdgeMag = img.getEdgeMag(seedPixel.x, seedPixel.y);

        try {
            // FIRST: find first significant edge
            int newRelLum = img.getPixelIntensity(xToCheck, yToCheck);
            int newEdgeMag = img.getEdgeMag(xToCheck, yToCheck);
            boolean possEdgeFound = false;
            while (!possEdgeFound) {
                if (numUp > 1) {
                    numDown = 0;
                    totalDrop = 0;
                    numUp = 1;
                }// if haven't found actual downward edge (because went up again in a
                 // not-insignificant manner)
                if (newRelLum < currentRelLum) {
                    numDown++;
                    totalDrop = totalDrop + (currentRelLum - newRelLum);
                    // if edgeMag > maxDownPix.relLum
                    if (newEdgeMag > eThresh && newEdgeMag >= currentEdgeMag
                            && newEdgeMag >= img.getEdgeMag(xToCheck + changeX, yToCheck + changeY)) {
                        maxDownPix = new Pixel(xToCheck, yToCheck, newEdgeMag);
                    }// if edge is a local maximum
                } else if (newRelLum > currentRelLum) {
                    totalDrop = totalDrop + (currentRelLum - newRelLum);
                    numUp++;
                }// else if went up instead of down TODO: what if is equal???
                xToCheck = xToCheck + changeX;
                yToCheck = yToCheck + changeY;
                currentRelLum = newRelLum;
                currentEdgeMag = newEdgeMag;
                newRelLum = img.getPixelIntensity(xToCheck, yToCheck);
                newEdgeMag = img.getEdgeMag(xToCheck, yToCheck);
                if (numDown > 2 && totalDrop > 30 && maxDownPix != null)
                    possEdgeFound = true;
            }// while

            // THEN begin trying to find flat section; if find new significant edge b4 flat section,
            // replace current significant edge
            int numFlat = 0;// want 10 of these before accept
            int numInterveningUnflat = 0;
            boolean foundFlatSec = false;
            while (!foundFlatSec) {
                if (numInterveningUnflat > 1)
                    numFlat = 0;
                if (newEdgeMag < eThresh) {
                    numFlat++;
                    numInterveningUnflat = 0;
                } else {
                    numInterveningUnflat++;
                    if (numUp > 1) {
                        numDown = 0;
                        totalDrop = 0;
                        numUp = 1;
                    }// if haven't found actual downward edge (because went up again in a
                     // not-insignificant manner)
                    if (newRelLum < currentRelLum) {
                        numDown++;
                        totalDrop = totalDrop + (currentRelLum - newRelLum);
                        if (newEdgeMag > eThresh
                                && newEdgeMag > (maxDownPix.relLum * 3 / 4)
                                && newEdgeMag >= currentEdgeMag
                                && newEdgeMag >= img.getEdgeMag(xToCheck + changeX, yToCheck
                                        + changeY)) {// edgeMag > maxDownPix.relLum
                            maxDownPix = new Pixel(xToCheck, yToCheck, newEdgeMag);
                        }// if edge is a local maximum
                    } else if (newRelLum > currentRelLum) {
                        totalDrop = totalDrop + (currentRelLum - newRelLum);
                        numUp++;
                    }// else if went up instead of down TODO: what if is equal???
                    xToCheck = xToCheck + changeX;
                    yToCheck = yToCheck + changeY;
                    currentRelLum = newRelLum;
                    currentEdgeMag = newEdgeMag;
                    newRelLum = img.getPixelIntensity(xToCheck, yToCheck);
                    newEdgeMag = img.getEdgeMag(xToCheck, yToCheck);
                }// else: a significant edgePix has been found
                if (numFlat > 9)
                    foundFlatSec = true;
            }// while
            return maxDownPix;
        } catch (ArrayIndexOutOfBoundsException oob) {
            Nepic.log(EventType.WARNING, EventLogger.LOG_ONLY, "NO CB FOUND: changeX =", changeX,
                    ", changeY =", changeY);
            return null;
        }// if CB hits the edge of the pic
    }// findThresh

    // returns maxXPoint
    private List<Point> extendEdges(List<Point> outerEdges, Roi<?> roi, int minPi) {
        List<Point> extendedBy = new LinkedList<Point>();
        DoubleLinkRing<Point> candEdges = new DoubleLinkRing<Point>();
        int roiId = roi.getId();
        for (Point edgePt : outerEdges) {
            Verify.argument(img.getId(edgePt.x, edgePt.y) == roiId,
                    "edgePt is not in ROI!  Expected RoiNum = " + roiId + ", actual RoiNum of "
                            + edgePt + " = " + img.getId(edgePt.x, edgePt.y));
            candEdges.add(edgePt);
        }
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
                    }// if add checkIfAdd to cand
                }// if pixel to left of pixel being examined is not already in cand
                if (ePixX < img.width - 1 && img.getId(ePixX + 1, ePixY) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX + 1, ePixY);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX + 1, ePixY, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                    }// if add checkIfAdd to cand
                }// if pixel to the right of pixel being examined is not already in cand
                if (ePixY > 0 && img.getId(ePixX, ePixY - 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY - 1);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX, ePixY - 1, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                    }// if add checkIfAdd to cand
                }// if pixel above pixel being examined not already in cand
                if (ePixY < img.height - 1 && img.getId(ePixX, ePixY + 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY + 1);
                    if (shouldAddPixel(ePixel, minPi)) {
                        Pixel pixToAdd = new Pixel(ePixX, ePixY + 1, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
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
            if (extendedBy.isEmpty()) {
                return outerEdges;
            }
            return Blob.traceOuterEdges(img, maxXPix, roiId);
        } catch (IllegalStateException e) {
            // If enlarge into another ROI
            for (Point pt : extendedBy) {
                img.noLongerCand(pt.x, pt.y);
            }
            throw new IllegalStateException(e.getMessage()
                    + ", caught by CellBodyFinder.extendEdges();"
                    + " unable to extend edges of cbCand");
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

    private int tryToAdd(Pixel toAdd, DoubleLinkRing<Point> candEdges, Roi<?> roi) {
        int numErrors = 0;

        if (img.getId(toAdd.x, toAdd.y) == roi.getId()) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "CB Cand already contains", toAdd);
            numErrors += 1;
        } else {
            img.setId(toAdd.x, toAdd.y, roi);
        }

        if (candEdges.contains(toAdd)) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "candEdges already contains", toAdd);
            numErrors += 1;
        } else {
            candEdges.addLast(toAdd);
        }

        return numErrors;
    }

    // *******************************************************************
    // SeedPolygon
    // *******************************************************************

    public static class SeedPolygon extends CellBodyConstraint<Polygon> {

        public SeedPolygon(Polygon val) {
            super(val);
        }

    }

    // *******************************************************************
    // EdgeThresh
    // *******************************************************************

    public static class EdgeThresh extends CellBodyConstraint<Integer> {

        public EdgeThresh(Integer val) {
            super(val);
        }

    }

    // *******************************************************************
    // DesiredSize
    // *******************************************************************

    public enum SizeEdgeCase {
        BIGGER,
        SMALLER,
        AS_CLOSE_AS_POSSIBLE;
    }

    public static class DesiredSize extends CellBodyConstraint<Pair<Integer, SizeEdgeCase>> {

        public DesiredSize(Pair<Integer, SizeEdgeCase> val) {
            super(val);
            int desiredSize = val.first;
            SizeEdgeCase edgeCase = val.second;
            Verify.argument(desiredSize > -1,
                    "Desired size of cell body must be a non-negative integer; instead got "
                            + desiredSize);
            Verify.notNull(edgeCase,
                    "The edge case for how to deal with desiredSize cannot be null.");
        }
    }
}
