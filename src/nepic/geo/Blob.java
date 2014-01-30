package nepic.geo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.Nepic;
import nepic.image.IdTaggedImage;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.util.HorizontalEdge;
import nepic.util.Pixel;
import nepic.util.Pixels;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since ??? (called Blob2 until AutoCBFinder_Alpha_v0-9_122212)
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 *
 */
public class Blob implements BoundedRegion {
    private final ArrayList<LinkedList<HorizontalEdge>> horizEdges;
    private final BoundingBox boundaries;
    private int size = -1;

    // If need to findOuterEdges, it is ONLY guaranteed that the sum of the edge and innard points
    // will add up to the desired blob. Not all of the points in 'edges' under these circumstances
    // will actually be included as edge points.
    public Blob(List<? extends Point> edges) {
        Verify.notNull(edges, "Blob edges cannot be null");
        int numEdgePts = edges.size();
        Verify.argument(numEdgePts > 0, "Blob must contain at least 1 pixel.");

        // Check that first pixel is not null
        Iterator<? extends Point> itr = edges.iterator();
        Point firstPix = itr.next(); // Preconditions: edges must have at least 1 pixel.
        boundaries = new BoundingBox(firstPix.x, firstPix.x, firstPix.y, firstPix.y);

        // Check that all pixels in edges are non-null and check if adjacent pixels are touching
        Point prevPix = firstPix;
        Point currPix = null;
        boolean needToTraceEdges = false;
        while (itr.hasNext()) {
            currPix = itr.next();
            Verify.notNull(currPix, "None of the points in the Blob edges can be null.");
            boundaries.editBoundariesIfNeeded(currPix.x, currPix.y);
            if (!Pixels.areTouching(prevPix, currPix)) { // Check that adjacent pixels are touching
                needToTraceEdges = true;
            }
            prevPix = currPix;
        }

        // Check if the first and last pixels touch
        if (!needToTraceEdges && currPix != null && !Pixels.areTouching(firstPix, currPix)) {
            needToTraceEdges = true;
        }

        // Make edge trace if necessary
        if (needToTraceEdges) {
            // Make IdTaggedImage for tracing
            // System.out.println("NEED TO TRACE EDGES");
            int id = 1909; // Arbitrary non-zero number.
            IdTaggedImage idImg = new ImgMatrix(boundaries, edges, id);
            Point maxXPix = findMinXInRegion(idImg, id);
            edges = traceOuterEdges(idImg, maxXPix, id);
            // for (Point pt : edges) {
            // System.out.println("(" + pt.x + ", " + pt.y + ")");
            // }
            numEdgePts = edges.size();
        }
        horizEdges = findHorizEdges(edges.toArray(new Point[numEdgePts]));
    }

    // For making deep copy
    private Blob(BoundingBox boundaries, ArrayList<LinkedList<HorizontalEdge>> horizEdges, int size) {
        this.horizEdges = horizEdges;
        this.boundaries = boundaries;
        this.size = size;
    }

    // corresponds to {w, nw, n, ne, e, se, s, sw}
    private static final Point[] dirs = new Point[] { new Point(-1, 0), // W
            new Point(-1, -1), // NW
            new Point(0, -1), // N
            new Point(1, -1), // NE
            new Point(1, 0), // E
            new Point(1, 1), // SE
            new Point(0, 1), // S
            new Point(-1, 1) }; // SW

    /**
     * @param maxXPoint the point in the ROI that has the largest x-coordinate
     * @param id the ID number of the ROI for which to find the outer edges
     * @return the outer edges of the ROI with the specified ID number
     */
    public static List<Point> traceAllOuterEdgePoints(IdTaggedImage img, Point maxXPoint, int id) {
        Verify.notNull(img, "Cannot find outer edges of region in null IdTaggedImage");
        // System.out.println("traceAllOuterEdgePoints");

        // If maxXPoint is not the maxX point with the given ID in the IdTaggedImage, then find the
        // actualmaxXPoint with the given ID in the IdTaggedImage.
        // if (maxXPoint == null || img.getId(maxXPoint.x, maxXPoint.y) != id
        // || (maxXPoint.x < img.getMaxX() && img.getId(maxXPoint.x + 1, maxXPoint.y) == id)) {
        // maxXPoint = findMaxXInRegion(img, id);
        // Verify.argument(maxXPoint != null,
        // "IdTaggedImage does NOT contain any pixels with id = " + id);
        // }

        List<Point> outerEdges = new LinkedList<Point>();
        int dirToGo = 0; // West
        Point toAdd = maxXPoint;
        do {
            // Add all outer edge points (some may be duplicated if there is an intersection)
            outerEdges.add(toAdd);
            // System.out.println("Add " + toAdd);

            // Find next direction to go
            List<Integer> dirsToGo = goLeft_flush(img, id, toAdd, (dirToGo + 4) % 8);
            // System.out.println("dirstToGo = " + dirsToGo);
            if (dirsToGo.isEmpty()) {
                if (outerEdges.size() == 1) {
                    // If blob contains only a single pixel
                    return outerEdges;
                }
                // Should never happen.
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                        "goLeft method failed.  currentPixel =", toAdd, ", outerEdges.size() =",
                        outerEdges.size());
                return null;
            }// if unable to find which direction to go to find the rest of the outer edges
            Iterator<Integer> dirItr = dirsToGo.iterator();
            dirToGo = dirItr.next(); // Must have at least 1 value
            Point possToAdd = new Point(toAdd.x + dirs[dirToGo].x, toAdd.y + dirs[dirToGo].y);

            int altDirToGo = dirToGo;
            Point altPossToAdd = possToAdd;
            while (dirItr.hasNext() && outerEdges.contains(altPossToAdd)) {
                // Then try to find another dir to go
                altDirToGo = dirItr.next();
                altPossToAdd = new Point(toAdd.x + dirs[altDirToGo].x, toAdd.y + dirs[altDirToGo].y);
            }
            if (dirItr.hasNext()) {
                // then edges does not yet contain altPossToAdd
                toAdd = altPossToAdd;
                dirToGo = altDirToGo;
            } else {
                toAdd = possToAdd;
            }
        } while (!toAdd.equals(maxXPoint));

        return outerEdges;
    }// findOuterEdges

    /**
     * Finds the outer edges of an ROI in the current image.
     *
     * @param maxXPoint the point in the ROI that has the largest x-coordinate
     * @param id the ID number of the ROI for which to find the outer edges
     * @return the outer edges of the ROI with the specified ID number
     */
    public static List<Point> traceOuterEdges(IdTaggedImage img, Point maxXPoint, int id) {
        Verify.notNull(img, "Cannot find outer edges of region in null IdTaggedImage");

        // If maxXPoint is not the maxX point with the given ID in the IdTaggedImage, then find the
        // actualmaxXPoint with the given ID in the IdTaggedImage.
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
            int xToGo = toAdd.x + dirs[dirToGo].x;
            int yToGo = toAdd.y + dirs[dirToGo].y;
            toAdd = new Point(xToGo, yToGo);
        } while (!toAdd.equals(maxXPoint));

        // System.out.println("outer edges = " + outerEdges);
        return outerEdges;
    }// findOuterEdges

    /**
     * @param from: pixel already determined to be in outer edges
     * @param dirFrom = dirTo + 180 degrees
     * @return what direction to go to (which pixel is next pixel in outer edges when go clockwise)
     */
    // Fixed AutoCBFinder_Alpha_v0-9-2013-01-29
    private static int goLeft(IdTaggedImage img, int id, Point from, int dirFrom) {
        int dirToCheck = dirFrom;
        for (int i = 0; i < dirs.length; i++) {
            dirToCheck = (dirToCheck + 1) % 8;// 8 == dirs.length
            int xToCheck = from.x + dirs[dirToCheck].x;
            int yToCheck = from.y + dirs[dirToCheck].y;
            if (img.contains(xToCheck, yToCheck) && img.getId(xToCheck, yToCheck) == id) {
                return dirToCheck;
            }
        }// for
        return -1;// should never happen
    }// goLeft

    // TODO: make GoLeft method where prioritize flush pixels over merely touching pixels?? --> for
    // TraceAllEdgePts method
    private static List<Integer> goLeft_flush(IdTaggedImage img, int id, Point from, int dirFrom) {
        // Verify.argument(dirFrom % 2 == 0); // must be one of four primary compass directions

        // Make list of possibilities to return
        List<Integer> potentialDirs = new ArrayList<Integer>(dirs.length);

        // for all primary compass directions
        int primaryDir = dirFrom % 2 == 0 ? dirFrom : (dirFrom - 1 + 8) % 8;
        for (int i = 2; i < dirs.length; i += 2) {
            int dirToCheck = (primaryDir + i) % 8;// 8 == dirs.length
            int xToCheck = from.x + dirs[dirToCheck].x;
            int yToCheck = from.y + dirs[dirToCheck].y;
            if (img.contains(xToCheck, yToCheck) && img.getId(xToCheck, yToCheck) == id) {
                potentialDirs.add(dirToCheck);
            }
        }

        // For all secondary compass directions
        primaryDir = dirFrom % 2 == 0 ? dirFrom : (dirFrom + 1) % 8;
        for (int i = 1; i < dirs.length; i += 2) { // for all primary compass directions
            int dirToCheck = (primaryDir + i) % 8;// 8 == dirs.length
            int xToCheck = from.x + dirs[dirToCheck].x;
            int yToCheck = from.y + dirs[dirToCheck].y;
            if (img.contains(xToCheck, yToCheck) && img.getId(xToCheck, yToCheck) == id) {
                potentialDirs.add(dirToCheck);
            }
        }

        return potentialDirs;
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

    private static Point findMinXInRegion(IdTaggedImage img, int id) {
        for (int y = img.getMinY(); y <= img.getMaxY(); y++) {
            for (int x = img.getMinX(); x <= img.getMaxX(); x++) {
                if (img.getId(x, y) == id) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    public Blob deepCopy() {
        // Copy boundingBox
        BoundingBox boundariesCopy = new BoundingBox(boundaries.getMinX(), boundaries.getMaxX(),
                boundaries.getMinY(), boundaries.getMaxY());

        // Copy horizEdges
        int numRows = horizEdges.size();
        ArrayList<LinkedList<HorizontalEdge>> horizEdgeCopy = new ArrayList<LinkedList<HorizontalEdge>>(
                numRows);
        for (int i = 0; i < numRows; i++) {
            LinkedList<HorizontalEdge> rowCopy = new LinkedList<HorizontalEdge>();
            for (HorizontalEdge horizEdge : horizEdges.get(i)) {
                rowCopy.add(new HorizontalEdge(horizEdge.first, horizEdge.last));
            }
            horizEdgeCopy.add(rowCopy);
        }

        // Make deep copy
        return new Blob(boundariesCopy, horizEdgeCopy, size);
    }

    public LineSegment getMaxDiameter() { // Equivalent to get length
        // TODO: increase efficiency (same way as before)
        Point startPoint = null;
        Point endPoint = null;
        List<Point> edgePts = getEdges();
        int maxDiamSquared = -1;
        for (Point edgePt1 : edgePts) {
            for (Point edgePt2 : edgePts) {
                int changeX = edgePt1.x - edgePt2.x;
                int changeY = edgePt1.y - edgePt2.y;
                int possMaxDiamSquared = changeX * changeX + changeY * changeY;
                if (possMaxDiamSquared > maxDiamSquared) {
                    maxDiamSquared = possMaxDiamSquared;
                    startPoint = edgePt1;
                    endPoint = edgePt2;
                }
            }
        }
        if (startPoint == null) {
            return null;
        }
        return new LineSegment(startPoint, endPoint);
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

    public BoundingBox getBoundingBox() {
        return boundaries.deepCopy();
    }

    /**
     *
     * @return the size of the innards of the {@link Blob}
     */
    public int getSize() {
        if (size < 0) {
            size = getInnards().size() + getEdges().size(); // TODO more efficient
        }
        return size;
    }

    /**
     *
     * @param passThrough
     * @param angle
     * @return the diameter of the blob at the given angle and passing through the given point
     */
    public Line getDiameter(Point passThrough, double angle) {
        // Create line going from boundaries (either minX -> maxX or minY -> maxY)
        // double yForMinX = (passThrough.x - minX) * Math.tan(angle) + minY;
        // int yForMaxX;
        //
        // int xForMinY;
        // int xForMaxY;

        // create line going from either minX -> maxX or minY -> maxY that goes through passThrough
        // (whether or not passThrough is withing bounds)

        // Take union of line, edges

        throw new UnsupportedOperationException("Not yet implemented."); // TODO
    }

    // fixed: version AutoCBFinder_Alpha_v0-9_NewLogger
    public List<Point> getInnards() {
        LinkedList<Point> toReturn = new LinkedList<Point>();
        final int minY = boundaries.getMinY();
        for (int rowNum = 0; rowNum < horizEdges.size(); rowNum++) {
            LinkedList<HorizontalEdge> currentRow = horizEdges.get(rowNum);
            Iterator<HorizontalEdge> currRowItr = currentRow.iterator();
            int prevEnd = Integer.MIN_VALUE;
            while (currRowItr.hasNext()) {
                int start = currRowItr.next().last + 1;
                int end = currRowItr.next().first;
                if (start <= prevEnd && end > prevEnd) {
                    start = prevEnd + 1;
                }
                if (start > prevEnd) {
                    for (int x = start; x < end; x++) {
                        toReturn.add(new Point(x, rowNum + minY));
                    }
                    prevEnd = Math.max(start - 1, end); // Since sometimes, start > end
                }
            }
        }

        return toReturn;
    }

    // Order is NOT guaranteed in the returned list (i.e. a new Blob cannot be made from this)
    public List<Point> getEdges() { // Gets the union of the edges
        List<Point> edgePts = new LinkedList<Point>();
        final int minY = boundaries.getMinY();

        for (int rowNum = 0; rowNum < horizEdges.size(); rowNum++) {
            LinkedList<HorizontalEdge> currentRow = horizEdges.get(rowNum);
            int y = minY + rowNum;
            int prevEnd = Integer.MIN_VALUE;
            for (HorizontalEdge edge : currentRow) {
                int newEnd = edge.last;
                if (newEnd > prevEnd) {
                    for (int x = Math.max(edge.first, prevEnd + 1); x <= newEnd; x++) {
                        edgePts.add(new Point(x, y));
                    }
                    prevEnd = newEnd;
                }
            }
        }
        return edgePts;
    }

    private ArrayList<LinkedList<HorizontalEdge>> initializeHorizEdgeLists(int numRows) {
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = new ArrayList<LinkedList<HorizontalEdge>>(
                numRows);
        for (int i = 0; i < numRows; i++) {
            horizEdges.add(new LinkedList<HorizontalEdge>());
        }
        return horizEdges;
    }

    private ArrayList<LinkedList<HorizontalEdge>> makeSinglePointBlob(Point pt) {
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = initializeHorizEdgeLists(1);
        LinkedList<HorizontalEdge> row = horizEdges.get(0);
        HorizontalEdge edge = new HorizontalEdge(pt.x, pt.x);
        row.add(edge);
        row.add(edge);
        return horizEdges;
    }

    private void sortHorizEdgeLists(ArrayList<LinkedList<HorizontalEdge>> horizEdges) {
        for (LinkedList<HorizontalEdge> row : horizEdges) {
            Collections.sort(row);
        }
    }

    private ArrayList<LinkedList<HorizontalEdge>> findHorizEdges(Point[] edgePts) {
        // Special case: single point Blob
        if (edgePts.length == 1) {
            return makeSinglePointBlob(edgePts[0]);
        }

        // Initialize all linked lists in horizEdges
        final int minY = boundaries.getMinY();
        final int numRows = boundaries.getMaxY() - minY + 1;
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = initializeHorizEdgeLists(numRows);

        // Find start of first horizontal edge
        int currY = edgePts[0].y; // The y-value of the current horizontal edge
        int horizEdgeMin = edgePts[0].x; // min x in the current horizEdge
        int horizEdgeMax = horizEdgeMin; // max x in the current horizEdge
        int endIdx = edgePts.length - 1; // last pos in edgePts NOT in first horizontal edge
        while (endIdx > 0 && edgePts[endIdx].y == currY) {
            int edgePtX = edgePts[endIdx].x;
            if (edgePtX > horizEdgeMax) { // Need to check in case edge doubles back on itself
                horizEdgeMax = edgePtX;
            } else if (edgePtX < horizEdgeMin) {
                horizEdgeMin = edgePtX;
            }
            endIdx--;
        }
        int firstPos = (endIdx + 1) % edgePts.length; // pos of first el in horiz edge
        int prevDiffY = currY - edgePts[endIdx].y;

        // Find end of horizontal edge
        int lastPos = 1; // (pos of last el in horiz edge) + 1
        while (lastPos < edgePts.length - 1 && edgePts[lastPos].y == currY) {
            int edgePtX = edgePts[lastPos].x;
            if (edgePtX > horizEdgeMax) { // Need to check in case edge doubles back on itself
                horizEdgeMax = edgePtX;
            } else if (edgePtX < horizEdgeMin) {
                horizEdgeMin = edgePtX;
            }
            lastPos++;
        }
        int nextDiffY = currY - edgePts[lastPos].y;

        // Make horizontal edge with given first and last positions
        HorizontalEdge edge = new HorizontalEdge(horizEdgeMin, horizEdgeMax);
        LinkedList<HorizontalEdge> row = horizEdges.get(currY - minY);
        row.add(edge);
        if (prevDiffY * nextDiffY >= 0) { // if diffs have the same sign
            // If is local min or local max, add edge a second time
            row.add(edge);
        }

        while (lastPos <= endIdx) {
            // Reset for next horizontal edge
            firstPos = lastPos;
            currY = edgePts[firstPos].y;
            horizEdgeMin = edgePts[firstPos].x;
            horizEdgeMax = horizEdgeMin;
            prevDiffY = -nextDiffY;

            // Find end of current horizontal edge
            lastPos = firstPos + 1;
            while (lastPos <= endIdx && edgePts[lastPos].y == currY) {
                int edgePtX = edgePts[lastPos].x;
                if (edgePtX > horizEdgeMax) { // Need to check in case edge doubles back on itself
                    horizEdgeMax = edgePtX;
                } else if (edgePtX < horizEdgeMin) {
                    horizEdgeMin = edgePtX;
                }
                lastPos++;
            }
            if (lastPos < edgePts.length) {
                nextDiffY = currY - edgePts[lastPos].y;
            } else {
                nextDiffY = currY - edgePts[(endIdx + 1) % edgePts.length].y;
            }

            // Make horizontal edge with given first and last positions
            edge = new HorizontalEdge(horizEdgeMin, horizEdgeMax);
            row = horizEdges.get(currY - minY);
            row.add(edge);
            if (prevDiffY * nextDiffY >= 0) { // if diffs have the same sign
                // If is local min or local max, add edge a second time
                row.add(edge);
            }
        }
        sortHorizEdgeLists(horizEdges);
        return horizEdges;
    }

    private static class ImgMatrix implements IdTaggedImage {
        private BoundingBox boundaries;
        private int[][] imgMatrix;

        private ImgMatrix(BoundingBox boundaries, List<? extends Point> edgePts, int id) {
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
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int y = getMinY(); y <= getMaxY(); y++) {
                for (int x = getMinX(); x <= getMaxX(); x++) {
                    if (getId(x, y) == 0) {
                        builder.append(OUTSIDE_POINT);
                    } else {
                        builder.append(EDGE_POINT);
                    }
                }
                builder.append("\n");
            }
            return builder.toString();
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

    @Override
    public String toString() { // TODO: make more efficient
        ArrayList<LinkedList<Integer>> edges = makeEdgeList();
        ArrayList<LinkedList<Integer>> innards = makeInnardsList();
        final int minX = boundaries.getMinX();
        final int maxX = boundaries.getMaxX();
        final int minY = boundaries.getMinY();

        StringBuilder builder = new StringBuilder("minX = ").append(boundaries.getMinX()).append(
                ", maxX = ").append(boundaries.getMaxX()).append("\n");
        for (int i = 0; i < edges.size(); i++) {
            builder.append((minY + i) + "\t");
            LinkedList<Integer> edgeRow = edges.get(i);
            LinkedList<Integer> innardRow = innards.get(i);
            Collections.sort(edgeRow);
            Collections.sort(innardRow);
            for (int x = minX; x <= maxX; x++) {
                if (edgeRow.contains(x)) {
                    builder.append(EDGE_POINT);
                } else if (innardRow.contains(x)) {
                    builder.append(INNARD_POINT);
                } else {
                    builder.append(OUTSIDE_POINT);
                }
            }
            builder.append("  ").append(horizEdges.get(i)).append("\n");
        }
        return builder.toString();
    }

    public Blob erode() {
        List<Point> innards = getInnards();
        if (innards.size() > 0) {
            return new Blob(innards);
        }
        return null;
    }

    public List<Pixel> getSurroundingEdges() {
        // Get the list of pixels that are just outside of the Blob (touching the edges of the Blob,
        // but not included in the Blob)
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    public Blob dilate() {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    /*
     * ********************************FOR TESTING************************************************
     */
    private static final char INNARD_POINT = 'O';
    private static final char EDGE_POINT = '*';
    private static final char OUTSIDE_POINT = '-';
    private static final char UNCAUGHT_POINT = '&';

    private ArrayList<LinkedList<Integer>> makeEdgeList() {
        final int minY = boundaries.getMinY();
        final int numRows = boundaries.getMaxY() - minY + 1;
        ArrayList<LinkedList<Integer>> edgeList = new ArrayList<LinkedList<Integer>>(numRows);

        // Initialize all linked lists
        for (int i = 0; i < numRows; i++) {
            edgeList.add(new LinkedList<Integer>());
        }

        for (Point edgePt : getEdges()) {
            edgeList.get(edgePt.y - minY).add(edgePt.x);
        }
        return edgeList;
    }

    private ArrayList<LinkedList<Integer>> makeInnardsList() {
        final int minY = boundaries.getMinY();
        final int numRows = boundaries.getMaxY() - minY + 1;
        ArrayList<LinkedList<Integer>> edgeList = new ArrayList<LinkedList<Integer>>(numRows);

        // Initialize all linked lists
        for (int i = 0; i < numRows; i++) {
            edgeList.add(new LinkedList<Integer>());
        }

        for (Point edgePt : getInnards()) {
            edgeList.get(edgePt.y - minY).add(edgePt.x);
        }
        return edgeList;
    }

    // private void printDrawEdges() {
    // ArrayList<LinkedList<Integer>> edges = makeEdgeList();
    // System.out.println("\n\n");
    // final int minX = boundaries.getMinX();
    // final int maxX = boundaries.getMaxX();
    // for (int i = 0; i < edges.size(); i++) {
    // LinkedList<Integer> row = edges.get(i);
    // System.out.print(i + "\t");
    // for (int x = minX; x <= maxX; x++) {
    // if (row.remove((Object) x)) {
    // System.out.print(EDGE_POINT);
    // } else {
    // System.out.print(OUTSIDE_POINT);
    // }
    // }
    // System.out.println();
    // }
    // }

    public void printDraw() {
        System.out.println("\n\n" + this);
    }

    public void printDraw(List<Point> uncleared) {
        ArrayList<LinkedList<Integer>> edges = makeEdgeList();
        ArrayList<LinkedList<Integer>> innards = makeInnardsList();
        System.out.println("\n\n");
        final int minX = boundaries.getMinX();
        final int maxX = boundaries.getMaxX();
        final int minY = boundaries.getMinY();

        System.out.println("minX = " + boundaries.getMinX() + ", maxX = " + boundaries.getMaxX());
        for (int i = 0; i < edges.size(); i++) {
            System.out.print((minY + i) + "\t");
            LinkedList<Integer> edgeRow = edges.get(i);
            LinkedList<Integer> innardRow = innards.get(i);
            Collections.sort(edgeRow);
            Collections.sort(innardRow);
            for (int x = minX; x <= maxX; x++) {
                if (edgeRow.contains(x)) {
                    System.out.print(EDGE_POINT);
                } else if (innardRow.contains(x)) {
                    System.out.print(INNARD_POINT);
                } else {
                    if (uncleared != null && uncleared.contains(new Point(x, (minY + i)))) {
                        System.out.print(UNCAUGHT_POINT);
                    } else {
                        System.out.print(OUTSIDE_POINT);
                    }
                }
            }
            System.out.print("  " + horizEdges.get(i));
            System.out.print("\n");
        }
    }

    // private void printHorizEdgeLists() {
    // System.out.println("\n\n");
    // for (int i = 0; i < horizEdges.size(); i++) {
    // System.out.println(i + "\t" + horizEdges.get(i));
    // }
    // }

    public void printDrawInnards() { // Terribly inefficient!
        System.out.println("\n\n");
        ArrayList<LinkedList<Integer>> innardsList = makeInnardsList();

        for (int i = 0; i < innardsList.size(); i++) {
            LinkedList<Integer> row = innardsList.get(i);
            System.out.print(i + "\t");
            for (int x = boundaries.getMinX(); x <= boundaries.getMaxX(); x++) {
                if (row.remove((Object) x)) {
                    System.out.print(INNARD_POINT);
                } else {
                    System.out.print(OUTSIDE_POINT);
                }
            }
            System.out.println();
        }

    }

    static List<Point> convertToList(Point[] pts) {
        List<Point> edges = new ArrayList<Point>(pts.length);
        for (int i = 0; i < pts.length; i++) {
            edges.add(pts[i]);
        }
        return edges;
    }

    static Blob makeShallowHeart() {
        Point[] edgePts = new Point[] {
                new Point(1, 0),
                new Point(2, 0),
                new Point(3, 1),
                new Point(4, 1),
                new Point(5, 0),
                new Point(6, 0),
                new Point(7, 1),
                new Point(7, 2),
                new Point(6, 3),
                new Point(5, 3),
                new Point(4, 4),
                new Point(3, 4),
                new Point(2, 3),
                new Point(1, 3),
                new Point(0, 2),
                new Point(0, 1) };
        return new Blob(convertToList(edgePts));
    }

    static Blob makeNarrowHeart() {
        Point[] edgePts = (new Point[] {
                new Point(1, 0),
                new Point(2, 1),
                new Point(3, 0),
                new Point(4, 1),
                new Point(4, 2),
                new Point(3, 3),
                new Point(3, 4),
                new Point(2, 5),
                new Point(1, 4),
                new Point(1, 3),
                new Point(0, 2),
                new Point(0, 1) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makeOrnament() {
        Point[] edgePts = (new Point[] {
                new Point(2, 5),
                new Point(2, 4),
                new Point(3, 3),
                new Point(4, 2),
                new Point(4, 1),
                new Point(4, 2),
                new Point(5, 3),
                new Point(6, 4),
                new Point(6, 5),
                new Point(6, 6),
                new Point(5, 7),
                new Point(4, 7),
                new Point(3, 7),
                new Point(2, 6) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makeSidewaysOrnament() {
        Point[] edgePts = (new Point[] {
                new Point(1, 3),
                new Point(2, 3),
                new Point(3, 2),
                new Point(4, 1),
                new Point(5, 1),
                new Point(6, 1),
                new Point(7, 2),
                new Point(7, 3),
                new Point(7, 4),
                new Point(6, 5),
                new Point(5, 5),
                new Point(4, 5),
                new Point(3, 4),
                new Point(2, 3) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makeSidewaysOrnament2() {
        Point[] edgePts = (new Point[] {
                new Point(4, 1),
                new Point(5, 1),
                new Point(6, 1),
                new Point(7, 2),
                new Point(7, 3),
                new Point(7, 4),
                new Point(6, 5),
                new Point(5, 5),
                new Point(4, 5),
                new Point(3, 4),
                new Point(2, 3),
                new Point(1, 3),
                new Point(2, 3),
                new Point(3, 2) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makeHorizTwoPtLine() {
        Point[] edgePts = (new Point[] { new Point(2, 1), new Point(1, 1) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makeVertTwoPtLine() {
        Point[] edgePts = (new Point[] { new Point(1, 1), new Point(1, 2) });
        return new Blob(convertToList(edgePts));
    }

    static Blob makePt() {
        Point[] edgePts = (new Point[] { new Point(1, 1) });
        return new Blob(convertToList(edgePts));
    }

    public static void main(String[] args) {
        Blob a = new Blob(Polygon.makeCrossOverStar().getEdges());
        // Blob a = makeNarrowHeart();
        // a.printHorizEdgeLists();
        // a.printDrawEdges();
        // a.printDrawInnards();
        a.printDraw();

        a = a.erode();
        a.printDraw();

        a = a.erode();
        a.printDraw();

        a = a.erode();
        a.printDraw();

        a = a.erode();
        a.printDraw();

        a = a.erode();
        a.printDraw();
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