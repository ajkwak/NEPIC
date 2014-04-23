package nepic.geo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import nepic.image.IdTaggedImage;
import nepic.util.HorizontalEdge;
import nepic.util.Pixels;
import nepic.util.Verify;

/**
 * Class representing a single, arbitrarily-shaped clump of pixels with no internal holes.
 *
 * @author AJ Parmidge
 */
public class Blob implements BoundedRegion {
    private final ArrayList<LinkedList<HorizontalEdge>> horizEdges;
    private final BoundingBox boundaries;
    private int size = -1; // Loaded lazily.

    /**
     * Create a {@link Blob} from the given collection of points, which represents a single clump of
     * mutually-touching pixels. Results are not guaranteed if the given {@code pixelClump}
     * represents more than one clump of pixels.
     * <p>
     * NOTE: It is <b> strongly </b> recommended that you avoid using this method if you already
     * have an {@link IdTaggedImage} that already contains a clump of pixels with a unique ID that
     * you want to convert into a {@link Blob}. In that case, it would be much less expensive to
     * create your {@link Blob} this way:
     *
     * <pre>
     * Blob myBlob = Blob.newBlobFromTracedEdges(
     *         traceOuterEdges(myIdTaggedImage, maxXPointInPixelClump, uniqueIdOfPixelClump));
     * </pre>
     *
     * @param pixelClump represents a <i> single </i> clump of mutually touching pixels
     * @return a {@link Blob} representing the given pixel clump
     */
    public static Blob newBlobFromPixelClump(Collection<? extends Point> pixelClump) {
        Verify.nonEmpty(pixelClump, "pixelClump");

        // Determine the Blob boundaries.
        BoundingBox blobBounds = null;
        Point maxXPt = null;
        for (Point pt : pixelClump) {
            int x = pt.x;
            int y = pt.y;
            if (blobBounds == null) { // If on the first loop.
                blobBounds = new BoundingBox(x, x, y, y);
                maxXPt = pt;
            } else {
                if (x > blobBounds.getMaxX()) {
                    maxXPt = pt;
                }
                blobBounds.update(x, y);
            }
        }

        // Make an IdTagged image for tracing the edges of the pixel clump.
        int id = 1909; // Arbitrary non-zero number.
        IdTaggedImage idImg = new RoiEdgeTracer.ImgMatrix(blobBounds, pixelClump, id);
        List<Point> blobEdgePointList = RoiEdgeTracer.traceOuterEdges(idImg, id, maxXPt);
        Point[] blobEdgePointArray = blobEdgePointList.toArray(new Point[blobEdgePointList.size()]);

        // Create the Blob.
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = findHorizEdges(
                blobBounds, blobEdgePointArray);
        return new Blob(blobBounds, horizEdges);
    }

    /**
     * Create a {@link Blob} from the given edges of a single clump of pixels. The given collection
     * of end points must satisfy 3 criteria:
     * <ol>
     * <li>They must all be edge points. No inner (non-edge) points from the clump of pixels may be
     * included, or the integrity of the resulting {@link Blob} (if any) is not guaranteed
     * <li>All adjacent points in the given collection of edges must touch (imagine tracing your
     * finger clockwise or counter-clockwise around the edges of the pixel clump).
     * <li>The first and last pixels in the given collection must touch
     * </ol>
     * If you cannot guarantee that all three of these conditions are met, <i> DO <b>NOT</b> CREATE
     * YOUR {@link Blob BLOB} USING THIS METHOD!!! </i>
     *
     * @param edges the edges of the pixel clump for which to make a {@link Blob}
     * @return the {@link Blob} representing the pixel clump with the given edges
     */
    public static Blob newBlobFromTracedEdges(Collection<? extends Point> edges) {
        int numEdgePts = edges.size();
        assert numEdgePts > 0;
        Point[] edgePtArray = new Point[numEdgePts];

        // Verify that adjacent pixels touch.
        int idx = 0;
        Point firstPt = null;
        Point prevPt = null;
        BoundingBox blobBounds = null;
        for (Point pt : edges) {
            if (idx == 0) {
                firstPt = pt;
                blobBounds = new BoundingBox(firstPt.x, firstPt.x, firstPt.y, firstPt.y);
            } else {
                assert Pixels.areTouching(pt, prevPt);
                blobBounds.update(pt.x, pt.y);
                prevPt = pt;
            }
            edgePtArray[idx++] = pt;
        }

        // Make the new Blob.
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = findHorizEdges(blobBounds, edgePtArray);
        return new Blob(blobBounds, horizEdges);
    }

    // Creates the horizontal edges from which the parity checking on the Blob is done (when
    // determining whether a pixel is inside the Blob, e.g.)
    private static ArrayList<LinkedList<HorizontalEdge>> findHorizEdges(BoundingBox bounds,
            Point[] edgePts) {
        // Special case: single point Blob
        if (edgePts.length == 1) {
            return makeSinglePointBlob(edgePts[0]);
        }

        // Initialize all linked lists in horizEdges
        int minY = bounds.getMinY();
        int numRows = bounds.getMaxY() - minY + 1;
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

    private static ArrayList<LinkedList<HorizontalEdge>> initializeHorizEdgeLists(int numRows) {
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = Lists.newArrayListWithCapacity(numRows);
        for (int i = 0; i < numRows; i++) {
            horizEdges.add(new LinkedList<HorizontalEdge>());
        }
        return horizEdges;
    }

    // TODO: do I really need to break this out into a special case??
    private static ArrayList<LinkedList<HorizontalEdge>> makeSinglePointBlob(Point pt) {
        ArrayList<LinkedList<HorizontalEdge>> horizEdges = initializeHorizEdgeLists(1);
        LinkedList<HorizontalEdge> row = horizEdges.get(0);
        HorizontalEdge edge = new HorizontalEdge(pt.x, pt.x);
        row.add(edge);
        row.add(edge);
        return horizEdges;
    }

    private static void sortHorizEdgeLists(ArrayList<LinkedList<HorizontalEdge>> horizEdges) {
        for (LinkedList<HorizontalEdge> row : horizEdges) {
            Collections.sort(row);
        }
    }

    private Blob(BoundingBox boundaries, ArrayList<LinkedList<HorizontalEdge>> horizEdges) {
        this.horizEdges = horizEdges;
        this.boundaries = boundaries;
    }

    private Blob(BoundingBox boundaries, ArrayList<LinkedList<HorizontalEdge>> horizEdges, int size) {
        this.horizEdges = horizEdges;
        this.boundaries = boundaries;
        this.size = size;
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

    public synchronized int getSize() {
        if (size < 0) {
            size = getInnards().size() + getEdges().size(); // TODO more efficient
        }
        return size;
    }

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

    // TODO: test that this returns only points that touch (are flush with?) a point not in the Blob
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

    public List<Point> getAllPoints() {
        LinkedList<Point> toReturn = new LinkedList<Point>();
        final int minY = boundaries.getMinY();
        for (int rowNum = 0; rowNum < horizEdges.size(); rowNum++) {
            LinkedList<HorizontalEdge> currentRow = horizEdges.get(rowNum);
            Iterator<HorizontalEdge> currRowItr = currentRow.iterator();
            int prevEnd = Integer.MIN_VALUE;
            while (currRowItr.hasNext()) {
                int start = currRowItr.next().first;
                int end = currRowItr.next().last;
                if (start <= prevEnd && end > prevEnd) {
                    start = prevEnd + 1;
                }
                if (start > prevEnd) {
                    for (int x = start; x <= end; x++) {
                        toReturn.add(new Point(x, rowNum + minY));
                    }
                    prevEnd = Math.max(start - 1, end); // Since sometimes, start > end
                }
            }
        }

        return toReturn;
    }

    @Override
    public String toString(){
        List<Point> edgePts = getEdges();
        StringBuilder builder = new StringBuilder();
        builder.append("Blob [");
        for(Point edgePt : edgePts){
            builder.append('(').append(edgePt.x).append(',').append(edgePt.y).append("),");
        }
        builder.append(']');
        return builder.toString();
    }

    public Blob erode() {
        List<Point> innards = getInnards();
        if (innards.size() > 0) {
            return newBlobFromPixelClump(innards);
        }
        return null;
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