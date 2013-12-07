package nepic.roi;

// Need methods from CBIdentifier.java from AutoCBFinder_Alpha_v0-3

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.image.ConstraintMap;
import nepic.image.Roi;
import nepic.image.RoiFinder;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.roi.CellBody.GraphDataAngle;
import nepic.roi.model.Blob;
import nepic.data.Histogram;
import nepic.roi.model.Line;
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

        // System.out.print("pi/2");
        DataScanner scanner = new DataScanner(
                img, new Line(seedPixel, -Math.PI / 2));
        roi.setGraphData(GraphDataAngle.PI_OVER_TWO, scanner.getGraphData());
        // System.out.print("-pi/4");
        scanner = new DataScanner(img, new Line(seedPixel, -Math.PI / 4));
        roi.setGraphData(GraphDataAngle.NEGATIVE_PI_OVER_FOUR, scanner.getGraphData());
        // System.out.print("zero");
        scanner = new DataScanner(img, new Line(seedPixel, 0));
        roi.setGraphData(GraphDataAngle.ZERO, scanner.getGraphData());
        // System.out.print("pi/4");
        scanner = new DataScanner(img, new Line(seedPixel, Math.PI / 4));
        roi.setGraphData(GraphDataAngle.PI_OVER_FOUR, scanner.getGraphData());

        List<Point> edges = new LinkedList<Point>();
        edges.add(seedPixel);
        int minPi = determinePiThreshold(eThresh, seedPixel);

        // Extend edges to MinPi
        edges = extendEdges(edges, roi, minPi);
        roi.setMinPi(minPi);
        setRoiEdges(roi, new Blob(edges));

        // DesiredSize constraint
        Pair<Integer, SizeEdgeCase> desiredSize = constraints.getConstraint(DesiredSize.class);
        if (desiredSize != null) {
            adjustToDesiredSize(roi, desiredSize.first, desiredSize.second);
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
            try {
                img.setId(edgePt.x, edgePt.y, validRoi);
            } catch (IllegalAccessException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e, "Unable to restore edge point "
                        + edgePt);
            }
        }
        for (Point innardPt : roiArea.getInnards()) {
            try {
                img.setId(innardPt.x, innardPt.y, validRoi);
            } catch (IllegalAccessException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e,
                        "Unable to restore innard point " + innardPt);
            }
        }
    }

    private void adjustToSeedPolygon(CellBody roi, Polygon seedPolygon) {
        Pixel seedPixel = getMostIntensePixel(seedPolygon);

        // Set seed pixel
        try {
            img.setId(seedPixel.x, seedPixel.y, roi);
        } catch (IllegalAccessException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e, "Unable to set seed pixel "
                    + seedPixel);
        }
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
            try {
                img.setId(seedPix.x, seedPix.y, roi);
            } catch (IllegalAccessException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e, "Unable to set seed pixel "
                        + seedPix);
            }
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
            try {
                img.setId(seedPix.x, seedPix.y, roi);
            } catch (IllegalAccessException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e, "Unable to set seed pixel "
                        + seedPix);
            }

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
        Histogram.Builder piHistBuilder = new Histogram.Builder(0, 255);
        for (Point cbPt : newEdges.getInnards()) {
            // For all points in the cell body
            piHistBuilder.addValues(img.getPixelIntensity(cbPt.x, cbPt.y));
        }
        roi.setPiHist(piHistBuilder.build());
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

    // NOTE: THIS WILL BE REPLACED (CURRENTLY CONVERTED TO DUMMY METHOD).
    private int determinePiThreshold(int eThresh, Point seedPix) {
        return Math.min(100, img.getPixelIntensity(seedPix.x, seedPix.y) - 3);
    }// determineThreshold

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
                    if (checkIfAdd >= minPi) {
                        Pixel pixToAdd = new Pixel(ePixX - 1, ePixY, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                    }// if add checkIfAdd to cand
                }// if pixel to left of pixel being examined is not already in cand
                if (ePixX < img.width - 1 && img.getId(ePixX + 1, ePixY) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX + 1, ePixY);
                    if (checkIfAdd >= minPi) {
                        Pixel pixToAdd = new Pixel(ePixX + 1, ePixY, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                    }// if add checkIfAdd to cand
                }// if pixel to the right of pixel being examined is not already in cand
                if (ePixY > 0 && img.getId(ePixX, ePixY - 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY - 1);
                    if (checkIfAdd >= minPi) {
                        Pixel pixToAdd = new Pixel(ePixX, ePixY - 1, checkIfAdd);
                        numErrors += tryToAdd(pixToAdd, candEdges, roi);
                        extendedBy.add(pixToAdd);
                    }// if add checkIfAdd to cand
                }// if pixel above pixel being examined not already in cand
                if (ePixY < img.height - 1 && img.getId(ePixX, ePixY + 1) != roiId) {
                    int checkIfAdd = img.getPixelIntensity(ePixX, ePixY + 1);
                    if (checkIfAdd >= minPi) {
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

    private int tryToAdd(Pixel toAdd, DoubleLinkRing<Point> candEdges, Roi<?> roi) {
        int numErrors = 0;

        if (img.getId(toAdd.x, toAdd.y) == roi.getId()) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "CB Cand already contains", toAdd);
            numErrors += 1;
        } else {
            try {
                img.setId(toAdd.x, toAdd.y, roi);
            } catch (IllegalAccessException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, e, "Unable to add pixel "
                        + toAdd);
            }
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
