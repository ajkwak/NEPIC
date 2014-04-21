package nepic.geo;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.image.IdTaggedImage;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.util.Verify;

public class RoiEdgeTracer {
    // Used by the goLeft() method when finding the outer edges of a clump of pixels.
    private static final Point[] DIRECTIONS = new Point[] {
            new Point(-1, 0), // W
            new Point(-1, -1), // NW
            new Point(0, -1), // N
            new Point(1, -1), // NE
            new Point(1, 0), // E
            new Point(1, 1), // SE
            new Point(0, 1), // S
            new Point(-1, 1) }; // SW

    /**
     * Finds the outer edges of the singular clump of pixels with the given ID on the given image.
     * TODO
     *
     * @param img
     * @param id
     * @param maxXPoint
     * @return
     */
    public static List<Point> traceOuterEdges(IdTaggedImage img, int id, Point maxXPoint) {
        Verify.notNull(img, "Cannot find outer edges of region in null IdTaggedImage");

        // If maxXPoint is not the maxX point with the given ID in the IdTaggedImage, then find the
        // actual maxXPoint with the given ID in the IdTaggedImage.
        if (maxXPoint == null || img.getId(maxXPoint.x, maxXPoint.y) != id
                || (maxXPoint.x < img.getMaxX() && img.getId(maxXPoint.x + 1, maxXPoint.y) == id)) {
            maxXPoint = findMaxXInRegion(img, id);
            Verify.argument(maxXPoint != null,
                    "IdTaggedImage does NOT contain any pixels with id = " + id);
        }

        List<Point> outerEdges = new LinkedList<Point>();
        int dirToGo = 0; // West
        Point toAdd = maxXPoint;// add minY; assume came from left
        do {
            // Add all outer edge points (some may be duplicated if there is an intersection)
            outerEdges.add(toAdd);
            // .out.println("Add " + toAdd);

            // Find next direction to go
            dirToGo = goLeft(img, id, toAdd, (dirToGo + 4) % 8);
            if (dirToGo == -1) {
                if (outerEdges.size() == 1) {
                    // If blob contains only a single pixel
                    return outerEdges;
                } else {
                    // Should never happen.
                    Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                            "goLeft method failed.  currentPixel = " + toAdd
                                    + ", outerEdges.size() = " + outerEdges.size());
                    return null;
                }
            }// if unable to find which direction to go to find the rest of the outer edges
            int xToGo = toAdd.x + DIRECTIONS[dirToGo].x;
            int yToGo = toAdd.y + DIRECTIONS[dirToGo].y;
            toAdd = new Point(xToGo, yToGo);
        } while (!toAdd.equals(maxXPoint));

        return outerEdges;
    }

    private static int goLeft(IdTaggedImage img, int id, Point from, int dirFrom) {
        int dirToCheck = dirFrom;
        for (int i = 0; i < DIRECTIONS.length; i++) {
            dirToCheck = (dirToCheck + 1) % 8;// 8 == dirs.length
            int xToCheck = from.x + DIRECTIONS[dirToCheck].x;
            int yToCheck = from.y + DIRECTIONS[dirToCheck].y;
            if (img.contains(xToCheck, yToCheck) && img.getId(xToCheck, yToCheck) == id) {
                return dirToCheck;
            }
        }
        return -1; // This should only happen if the pixel clump being traced is a single pixel.
    }

    private static Point findMaxXInRegion(IdTaggedImage img, int id) {
        for (int y = img.getMinY(); y <= img.getMaxY(); y++) {
            for (int x = img.getMaxX(); x >= img.getMinX(); x--) {
                if (img.getId(x, y) == id) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    // This class should not be instantiated.
    private RoiEdgeTracer() {
        throw new UnsupportedOperationException();
    }

    public static class ImgMatrix implements IdTaggedImage {
        private BoundingBox boundaries;
        private int[][] imgMatrix;

        public ImgMatrix(BoundingBox boundaries, Collection<? extends Point> edgePts, int id) {
            this.boundaries = boundaries;
            int minX = boundaries.getMinX();
            int maxX = boundaries.getMaxX();
            int minY = boundaries.getMinY();
            int maxY = boundaries.getMaxY();
            imgMatrix = new int[maxX - minX + 1][maxY - minY + 1];

            // Initialize edgePts on the ImgMatrix
            for (Point pt : edgePts) {
                imgMatrix[pt.x - minX][pt.y - minY] = id;
            }
        }

        @Override
        public boolean contains(int x, int y) {
            return x >= getMinX() && x <= getMaxX() && y >= getMinY() && y <= getMaxY();
        }

        @Override
        public int getId(int x, int y) {
            return imgMatrix[x - getMinX()][y - getMinY()];
        }

        @Override
        public int getMinX() {
            return boundaries.getMinX();
        }

        @Override
        public int getMaxX() {
            return boundaries.getMaxX();
        }

        @Override
        public int getMinY() {
            return boundaries.getMinY();
        }

        @Override
        public int getMaxY() {
            return boundaries.getMaxY();
        }

        @Override
        public boolean boundsContain(BoundedRegion region) {
            return boundaries.boundsContain(region);
        }

        @Override
        public boolean boundsContain(int x, int y) {
            return boundaries.boundsContain(x, y);
        }
    }
}
