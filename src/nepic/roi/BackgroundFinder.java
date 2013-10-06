package nepic.roi;

import java.awt.Point;
import java.util.List;

import nepic.Nepic;
import nepic.image.ConstraintMap;
import nepic.image.RoiFinder;
import nepic.logging.EventType;
import nepic.data.Histogram;
import nepic.roi.model.Polygon;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 */
public class BackgroundFinder extends RoiFinder<BackgroundConstraint<?>, Background> {
    // Need to initialize these values in order to track the background
    Polygon origBkArea = null;
    Point origOrigin = null; // Use center of CellBody as the origin (around which bkArea is
                             // rotated)
    double origTheta = 0; // original theta

    public boolean initialized() {
        return origBkArea != null && origOrigin != null;
    }

    @Override
    public Background createFeature(ConstraintMap<BackgroundConstraint<?>> constraints) {
        Background toReturn = new Background(img);

        // Set up constraints
        Polygon bkArea = constraints.getConstraint(BackgroundArea.class);
        Point origin = constraints.getConstraint(Origin.class);
        Double currTheta = constraints.getConstraint(CurrTheta.class);
        if (bkArea != null) {
            // Background constraint
            initializeBkArea(toReturn, bkArea);

            // Origin constraint
            if (origin != null) {
                initializeOrigin(toReturn, origin);
            }

            // CurrTheta constraint
            if (currTheta != null) {
                initializeTheta(toReturn, currTheta);
            }

        } else {
            Verify.state(origOrigin != null, "Original y-axis for background not set.");

            // CurrTheta constraint
            if (currTheta != null && currTheta != origTheta) { // Need to rotate
                final double pi = Math.PI;
                Double prevTheta = constraints.getConstraint(PrevTheta.class);
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
            setRoiBackground(toReturn, bkArea);
        }

        return toReturn;
    }

    @Override
    public Background editFeature(Background roi, ConstraintMap<BackgroundConstraint<?>> constraints) {
        roi.setModified(true);

        // BackgroundArea constraint
        Polygon bkArea = constraints.getConstraint(BackgroundArea.class);
        if (bkArea != null) {
            Polygon roiBkArea = roi.getArea();
            if (roiBkArea != null) {
                removeFeatureFromImage(roi);
            }
            initializeBkArea(roi, bkArea);
        }

        // Origin constraint
        Point origin = constraints.getConstraint(Origin.class);
        if (origin != null) {
            initializeOrigin(roi, origin);
        }

        // CurrTheta constraint
        Double currTheta = constraints.getConstraint(CurrTheta.class);
        if (currTheta != null) {
            initializeTheta(roi, currTheta);
        }

        return roi;
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
    public void restoreFeature(Background validRoi) {
        Verify.notNull(validRoi, "ROI to restore cannot be null.");
        Verify.argument(validRoi.isValid(), "ROI to restore must be valid");
        validRoi.revalidate(img); // Give valid ROI an Id handle for this image
        for (Point innardPt : validRoi.getArea().getInnards()) {
            img.setId(innardPt.x, innardPt.y, validRoi);
        }
    }

    // Does NOT clear or invalidate the roi
    private void removeFeatureFromImage(Background roi) {
        for (Point innardPix : roi.getArea().getInnards()) {
            if (img.contains(innardPix)) {
                img.noLongerCand(innardPix.x, innardPix.y);
            }
        }

        // Check : TODO remove this code!
        int numUnclearedPixs = getAllPixelsInRoi(roi.getId()).size();
        Verify.argument(numUnclearedPixs == 0, "Not all pixels in ROI (ID = " + roi.getId()
                + ") removed! " + numUnclearedPixs
                + " uncleared pixels remain.  Polygon.innards.size() = "
                + roi.getArea().getInnards().size());// + "\n\n" + img.printDraw());
    }

    private void setRoiBackground(Background roi, Polygon bkArea) {
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
                edgeHistBuilder.addValues(
                        rl - img.getPixelIntensity(x + 1, y), rl - img.getPixelIntensity(x, y + 1));

                img.setId(x, y, roi);
            } catch (ArrayIndexOutOfBoundsException e) {
                removeFeatureFromImage(roi);
                roi.setArea(null);
                Nepic.log(EventType.WARNING, "Background extends beyond image boundaries.  "
                        + "Please indicate a new background.");
                break;
            } catch (IllegalStateException e) { // if overlap with another ROI
                removeFeatureFromImage(roi);
                roi.setArea(null);
                Nepic.log(EventType.WARNING, "Background conflicts with another ROI.  "
                        + "Please indicate a new background.");
                break;
            }
        }

        // Set histograms to BK
        roi.setPiHist(piHistBuilder.build());
        roi.setEdgeHist(edgeHistBuilder.build());
    }

    private void initializeBkArea(Background roi, Polygon bkArea) {
        origBkArea = bkArea;
        setRoiBackground(roi, bkArea);
    }

    private void initializeTheta(Background toReturn, Double currTheta) {
        origTheta = currTheta;
        toReturn.setTheta(currTheta);
    }

    private void initializeOrigin(Background toReturn, Point origin) {
        origOrigin = origin;
        toReturn.setOrigin(origin);
    }

    // *******************************************************************
    // BackgroundArea
    // *******************************************************************

    public static class BackgroundArea extends BackgroundConstraint<Polygon> {

        public BackgroundArea(Polygon val) {
            super(val);
        }

    }

    // *******************************************************************
    // Origin
    // *******************************************************************

    public static class Origin extends BackgroundConstraint<Point> {

        public Origin(Point val) {
            super(val);
        }

    }

    // *******************************************************************
    // CurrTheta
    // *******************************************************************

    public static class CurrTheta extends BackgroundConstraint<Double> {

        public CurrTheta(Double val) {
            super(val);
        }

    }

    // *******************************************************************
    // PrevTheta
    // *******************************************************************

    public static class PrevTheta extends BackgroundConstraint<Double> {

        public PrevTheta(Double val) {
            super(val);
        }

    }

}
