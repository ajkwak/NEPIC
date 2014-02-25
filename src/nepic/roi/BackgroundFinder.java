package nepic.roi;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.Lists;

import nepic.Nepic;
import nepic.geo.Polygon;
import nepic.image.ConstraintMap;
import nepic.image.RoiFinder;
import nepic.logging.EventType;
import nepic.data.Histogram;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 */
public class BackgroundFinder extends RoiFinder<Background> {
    // Constraint names.
    public static final String ORIGIN = "Origin";
    public static final String CURR_THETA = "Current Theta";
    public static final String PREV_THETA = "Previous Theta";
    public static final String AREA = "Area";

    // Need to initialize these values in order to track the background
    Polygon origBkArea = null;
    Point origOrigin = null; // Use center of CellBody as the origin (around which bkArea is
                             // rotated)
    double origTheta = 0; // original theta

    public boolean initialized() {
        return origBkArea != null && origOrigin != null;
    }

    @Override
    public Background createFeature(ConstraintMap constraints) {
        Background toReturn = new Background(img);

        // Set up constraints
        Polygon bkArea = (Polygon) constraints.getConstraint(AREA);
        Point origin = (Point) constraints.getConstraint(ORIGIN);
        Double currTheta = (Double) constraints.getConstraint(CURR_THETA);
        if (bkArea != null) {
            // Background constraint
            if (!initializeBkArea(toReturn, bkArea)) {
                return null;
            }

            // Origin constraint
            if (origin != null) {
                initializeOrigin(toReturn, origin);
            }

            // CurrTheta constraint
            if (currTheta != null) {
                initializeTheta(toReturn, currTheta);
            }

            return toReturn;

        } else {
            Verify.state(origOrigin != null, "Original y-axis for background not set.");

            // CurrTheta constraint
            if (currTheta != null && currTheta != origTheta) { // Need to rotate
                final double pi = Math.PI;
                Double prevTheta = (Double) constraints.getConstraint(PREV_THETA);
                if (prevTheta == null) {
                    prevTheta = origTheta;
                }

                // System.out.println("currTheta = " + Math.toDegrees(currTheta));
                // System.out.println("prevTheta = " + Math.toDegrees(prevTheta));
                // System.out.println("origTheta = " + Math.toDegrees(origTheta));

                // Determine actual value of theta
                double diffTheta = currTheta - prevTheta; // new - old
                // System.out.println("diffTheta = " + Math.toDegrees(diffTheta));
                if (diffTheta > pi / 2) {
                    diffTheta = pi - diffTheta;
                    // System.out.println("Now, diffTheta = " + Math.toDegrees(diffTheta));
                } else if (diffTheta < -pi / 2) {
                    diffTheta = -pi - diffTheta;
                    // System.out.println("Now, diffTheta = " + Math.toDegrees(diffTheta));
                }
                currTheta = prevTheta + diffTheta;
                // System.out.println("Most likely currTheta = " + Math.toDegrees(prevTheta) + " + "
                // + Math.toDegrees(diffTheta) + " = " + Math.toDegrees(currTheta));
                currTheta = currTheta % (2 * pi);
                // System.out.println("Thus, currTheta finally = " + Math.toDegrees(currTheta));

                // Make transposed background
                diffTheta = currTheta - origTheta;
                // System.out.println("phi (diffTheta) = " + Math.toDegrees(diffTheta));
                bkArea = origBkArea.rotate(diffTheta, (origin == null ? origOrigin : origin));
                toReturn.setTheta(currTheta);

                // System.out.println("END ROTATE!\n\n");
            } else {
                bkArea = origBkArea.deepCopy();
                toReturn.setTheta(origTheta);
            }

            // Origin constraint
            if (origin != null) {
                bkArea.translate(origin.x - origOrigin.x, origin.y - origOrigin.y); // new - old
                toReturn.setOrigin(new Point(origin.x, origin.y));
            } else {
                toReturn.setOrigin(new Point(origOrigin.x, origOrigin.y));
            }

            // Background
            if (setRoiBackground(toReturn, bkArea)) {
                return toReturn;
            }
        }

        return null;
    }

    @Override
    public boolean editFeature(Background roi, ConstraintMap constraints) {
        roi.setModified(true);

        // BackgroundArea constraint
        Polygon bkArea = (Polygon) constraints.getConstraint(AREA);
        if (bkArea != null) {
            Polygon roiBkArea = roi.getArea();
            if (roiBkArea != null) {
                removeFeatureFromImage(roi);
            }
            if (!initializeBkArea(roi, bkArea)) {
                return false;
            }
        }

        // Origin constraint
        Point origin = (Point) constraints.getConstraint(ORIGIN);
        if (origin != null) {
            initializeOrigin(roi, origin);
        }

        // CurrTheta constraint
        Double currTheta = (Double) constraints.getConstraint(CURR_THETA);
        if (currTheta != null) {
            initializeTheta(roi, currTheta);
        }

        return true;
    }

    @Override
    public void acceptFeature(Background roi) {
        roi.setModified(false);
        removeFeatureFromImage(roi);
    }

    /**
     *
     */
    @Override
    public void removeFeature(Background roi) {
        removeFeatureFromImage(roi);
        // roi.clear();
    }

    @Override
    public boolean restoreFeature(Background validRoi) {
        Verify.notNull(validRoi, "ROI to restore cannot be null.");
        Verify.argument(validRoi.getArea() != null, "ROI to restore must have a non-null area");
        validRoi.revalidate(img); // Give valid ROI an Id handle for this image
        List<Point> bkInnards = validRoi.getInnards();
        List<Point> restoredBkPts = Lists.newArrayListWithCapacity(bkInnards.size());
        try {
            for (Point innardPt : bkInnards) {
                img.setId(innardPt.x, innardPt.y, validRoi);
                restoredBkPts.add(innardPt);
            }
            return true;
        } catch (ConflictingRoisException e) {
            for (Point invalidRestoredPt : restoredBkPts) {
                img.dissociatePixelWithRoi(invalidRestoredPt.x, invalidRestoredPt.y, validRoi);
            }
            return false;
        }
    }

    // Does NOT clear or invalidate the roi
    private void removeFeatureFromImage(Background roi) {
        for (Point innardPix : roi.getArea().getInnards()) {
            if (img.contains(innardPix.x, innardPix.y)) {
                img.dissociatePixelWithRoi(innardPix.x, innardPix.y, roi);
            }
        }

        // Check : TODO remove this code!
        List<Point> unclearedPixs = getAllPixelsInRoi(roi.getId());
        if (!unclearedPixs.isEmpty()) {
            List<Point> currentInnards = roi.getArea().getInnards();
            int numContainedInInnards = 0;
            for (Point unclearedPix : unclearedPixs) {
                if (currentInnards.contains(unclearedPix)) {
                    numContainedInInnards++;
                }
            }

            Nepic.log(EventType.ERROR, "Clearing current background region failed.",
                    "Not all pixels in Background (ID =", roi.getId(),
                    ") removed!", unclearedPixs.size(),
                    "uncleared pixels remain.  Current innards contain", numContainedInInnards,
                    " of these uncleared pixels:\r\n",
                            img.printDraw());
        }
    }

    private boolean setRoiBackground(Background roi, Polygon bkArea) {
        Polygon prevBkArea = roi.getArea();
        roi.setArea(bkArea);

        // Make Histograms for bkArea
        Histogram.Builder piHistBuilder = new Histogram.Builder(0, 255);
        Histogram.Builder edgeHistBuilder = new Histogram.Builder(-255, 255);

        // Fill histograms
        List<Point> bkPts = bkArea.getInnards();
        for (Point bkPt : bkPts) {
            int x = bkPt.x;
            int y = bkPt.y;
            try {
                int rl = img.getPixelIntensity(x, y);
                piHistBuilder.addValues(rl);

                // edgeHist: add only 2 differences so don't double-count edges
                edgeHistBuilder.addValues(rl - img.getPixelIntensity(x + 1, y), rl
                        - img.getPixelIntensity(x, y + 1));

                img.setId(x, y, roi);
            } catch (ArrayIndexOutOfBoundsException e) {
                removeFeatureFromImage(roi);
                roi.setArea(null);
                Nepic.log(EventType.WARNING, "Background extends beyond image boundaries.  "
                        + "Please indicate a new background.");
                return false;
            } catch (ConflictingRoisException e) { // if overlap with another ROI
                removeFeatureFromImage(roi); // TODO: THIS SCREWS UP OTHER ROI CONFLICTED WITH!!!!
                roi.setArea(prevBkArea);
                if (prevBkArea != null) {
                    restoreFeature(roi);
                }
                Nepic.log(EventType.WARNING, "Background conflicts with another ROI.  "
                        + "Please indicate a new background.");
                return false;
            }
        }

        if(bkPts.isEmpty()){
            Nepic.log(EventType.WARNING,
                    "Cannot accept current background, as it contains no pixels.");
            return false;
        }


        // Set histograms to BK
        roi.setPiHist(piHistBuilder.build());
        roi.setEdgeHist(edgeHistBuilder.build());
        return true;
    }

    private boolean initializeBkArea(Background roi, Polygon bkArea) {
        if (setRoiBackground(roi, bkArea)) {
            origBkArea = bkArea;
            return true;
        }
        return false;
    }

    private void initializeTheta(Background toReturn, Double currTheta) {
        origTheta = currTheta;
        toReturn.setTheta(currTheta);
    }

    private void initializeOrigin(Background toReturn, Point origin) {
        origOrigin = origin;
        toReturn.setOrigin(origin);
    }
}
