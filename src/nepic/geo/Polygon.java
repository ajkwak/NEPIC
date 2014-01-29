package nepic.geo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since ???
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 * 
 */
public class Polygon implements BoundedRegion {
    private final Point[] vertices;
    private final BoundingBox boundaries;

    public Polygon(Point[] vertexPts) {
        Verify.notNull(vertexPts, "Polygon vertices cannot be null");
        int numVertices = vertexPts.length;
        Verify.argument(numVertices > 2, "Polygon must have at least 3 vertices");
        vertices = new Point[numVertices];
        boundaries = new BoundingBox(vertexPts[0], vertexPts[0]);
        for (int i = 0; i < numVertices; i++) {
            Point vertex = vertexPts[i];
            Verify.notNull(vertex, "Vertex " + i + " cannot be null");
            vertices[i] = vertex;
            boundaries.editBoundariesIfNeeded(vertex.x, vertex.y);
        }
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

    public boolean needToCropToBoundsOf(BoundedRegion region) {
        return !region.boundsContain(this);
    }

    public BoundingBox getBoundingBox() {
        return boundaries.deepCopy();
    }

    public Point[] getVertices() {
        int numVertices = vertices.length;
        Point[] verticesCopy = new Point[numVertices];
        for (int i = 0; i < numVertices; i++) {
            verticesCopy[i] = vertices[i];
        }
        return verticesCopy;
    }

    public List<Point> getEdges() {
        List<Point> edges = new LinkedList<Point>();
        Point lastPoint = vertices[vertices.length - 1];
        for (int i = 0; i < vertices.length; i++) {
            Point currentPoint = vertices[i];
            edges.addAll(new LineSegment(lastPoint, currentPoint).draw(
                    LineSegment.IncludeStart.YES, LineSegment.IncludeEnd.NO));
            lastPoint = currentPoint;
        }
        return edges;
    }

    public boolean intersects(Polygon other) {
        // If bounding boxes of polygons don't touch, can't intersect
        BoundingBox intersection = boundaries.getIntersectionWith(other.boundaries);
        if (intersection == null) { // Bounding boxes don't intersect
            return false;
        }

        int iMinX = intersection.getMinX(); // iMinX --> intersectionMinX
        int iMaxX = intersection.getMaxX();
        int iMinY = intersection.getMinY();
        int iMaxY = intersection.getMaxY();

        // Make hashmap to look for possible intersections
        int hashLength = iMaxX - iMinX + 1;
        ArrayList<LinkedList<Integer>> intersectionHash = new ArrayList<LinkedList<Integer>>(iMaxX
                - iMinX + 1);
        for (int i = 0; i < hashLength; i++) {
            intersectionHash.add(new LinkedList<Integer>());
        }

        // Put this polygon in hash
        for (Point edgePt : getEdges()) {
            if (edgePt.x >= iMinX && edgePt.x <= iMaxX && edgePt.y >= iMinY && edgePt.y <= iMaxY) {
                intersectionHash.get(edgePt.x - iMinX).add(edgePt.y);
            } // if edge point within range of bounding box
        }

        // Check for intersections
        for (Point edgePt : other.getEdges()) {
            if (edgePt.x >= iMinX && edgePt.x <= iMaxX && edgePt.y >= iMinY && edgePt.y <= iMaxY
                    && intersectionHash.get(edgePt.x - iMinX).contains(edgePt.y)) {
                return true; // intersection found
            } // if edge point within range of bounding box
        }
        return false; // No intersections found
    }

    public List<Point> getInnards() {
        ArrayList<LinkedList<Integer>> edges = makeEdgesForScanlineFill();
        LinkedList<Point> toReturn = new LinkedList<Point>();
        if (edges != null) {
            int rowNum = 0;
            int minY = boundaries.getMinY();
            while (rowNum < edges.size()) {
                LinkedList<Integer> currentRow = edges.get(rowNum);
                Collections.sort(currentRow);
                Iterator<Integer> currRowItr = currentRow.iterator();
                while (currRowItr.hasNext()) {
                    int start = currRowItr.next();
                    int end = currRowItr.next();
                    for (int x = start + 1; x < end; x++) {
                        toReturn.add(new Point(x, rowNum + minY + 1));
                    }
                }
                rowNum++;
            }
        }
        return toReturn;
    }

    private ArrayList<LinkedList<Integer>> makeEdgesForScanlineFill() {
        // Initialize edge lists
        int minY = boundaries.getMinY();
        int maxY = boundaries.getMaxY();
        int numRows = maxY - minY - 1; // Exclude top, bottom points of polygon
        if (numRows < 1) {
            return null;
        }
        ArrayList<LinkedList<Integer>> scanlineEdgePts = new ArrayList<LinkedList<Integer>>(numRows);
        ArrayList<LinkedList<HorizEdge>> horizEdges = new ArrayList<LinkedList<HorizEdge>>(numRows);
        for (int i = 0; i < numRows; i++) { // Initialize all linked lists
            scanlineEdgePts.add(new LinkedList<Integer>());
            horizEdges.add(new LinkedList<HorizEdge>());
        }

        // Fill edge lists (in place)
        int numVertices = vertices.length;
        for (int i = 0; i < numVertices; i++) {
            Point prevPt = vertices[(i + numVertices - 1) % numVertices];
            Point currPt = vertices[i];
            Point nextPt = vertices[(i + 1) % numVertices];
            int prevY = prevPt.y;
            int currY = currPt.y;
            int nextY = nextPt.y;

            // Focus on currPt
            if (currY == nextY) { // currPt precedes horizontal edge
                if (currY > minY && currY < maxY) {
                    horizEdges.get(currY - minY - 1).add(new HorizEdge(currPt.x, nextPt.x));
                }
            } else {
                if (prevY != currY && (prevY < currY ^ nextY < currY)) {
                    // If side vertex (i.e. not top or bottom): include currPt once
                    scanlineEdgePts.get(currY - minY - 1).add(currPt.x);
                }

                // Include all pts in edge until nextPt once
                List<Point> nextLine = new LineSegment(currPt, nextPt).drawByY(
                        LineSegment.IncludeStart.NO, LineSegment.IncludeEnd.NO); // Draws 1 value
                                                                                 // per y
                                                                                 // coordinate,
                                                                                 // excludes end
                                                                                 // points
                for (Point linePt : nextLine) {
                    scanlineEdgePts.get(linePt.y - minY - 1).add(linePt.x);
                    // System.out.println("ERROR: " + e.getMessage());
                    // System.out.println("minX = " + minX + ", minY = " + minY + ", maxX = "
                    // + maxX + ", maxY = " + maxY);
                    // System.out.println("currPt = " + currPt + ", nextPt = " + nextPt);
                    // System.out.println("nextLine = " + nextLine);
                    // System.out.println("linePt = " + linePt);
                    // System.out.println("linePt.y - minY - 1 = " + linePt.y + " - " + minY
                    // + " - " + "1 = " + (linePt.y - minY - 1));
                    // System.out.println("numRows = " + numRows + ", actual num rows = "
                    // + scanlineEdgePts.size());
                }
            }
        }

        // Put in horizEdge, if necessary
        for (int i = 0; i < horizEdges.size(); i++) { // Assume horizEdges don't overlap
            LinkedList<HorizEdge> hEdgesAtX = horizEdges.get(i);
            if (!hEdgesAtX.isEmpty()) { // if have horizontal edge at this x value
                Collections.sort(hEdgesAtX);
                LinkedList<Integer> ptsAtX = scanlineEdgePts.get(i);
                Collections.sort(ptsAtX);
                int numPts = ptsAtX.size();
                Iterator<Integer> ptsAtItr = ptsAtX.iterator();

                // Include hEdge.startPt if numPixs to the left of hEdge.startPt is odd
                // Include hEdge.endPt if numPixs to the right of hEdge.endPt is odd
                int numPtsPassed = 0;
                int numHEdgesRemaining = hEdgesAtX.size();
                List<Integer> ptsToAdd = new LinkedList<Integer>();
                for (HorizEdge hEdge : hEdgesAtX) {
                    numHEdgesRemaining--;
                    int toAdd = hEdge.startPt;
                    while (ptsAtItr.hasNext() && ptsAtItr.next() < toAdd) {
                        numPtsPassed++;
                    }
                    if (numPtsPassed % 2 == 1) {
                        ptsToAdd.add(hEdge.startPt);
                        numPtsPassed++;
                        numPts++;
                    }
                    toAdd = hEdge.endPt;
                    while (ptsAtItr.hasNext() && ptsAtItr.next() < toAdd) {
                        numPtsPassed++;
                    }
                    if ((numPts - numPtsPassed) % 2 == 1 || numHEdgesRemaining > 0) {
                        ptsToAdd.add(hEdge.endPt);
                        numPtsPassed++;
                        numPts++;
                    }
                }
                ptsAtX.addAll(ptsToAdd);
            }
        }
        return scanlineEdgePts;
    }

    public void translate(int x, int y) {
        // Translate vertices
        for (Point vertex : vertices) {
            vertex.x += x;
            vertex.y += y;
        }

        // Update the BoundingBox of the polygon
        int newMinX = boundaries.getMinX() + x;
        int newMaxX = boundaries.getMaxX() + x;
        int newMinY = boundaries.getMinY() + y;
        int newMaxY = boundaries.getMaxY() + y;
        boundaries.update(newMinX, newMaxX, newMinY, newMaxY);
    }

    // Gets the midpoint of the boundaries of the polygon
    public Point getMidpoint() {
        return boundaries.getMidPoint();
    }

    // NOT in place!
    // Enlarges / shrinks the polygon to the given factor (enlarges around the midpoint of the
    // polygon's boundaries)
    public Polygon changeSize(double factor) {
        Point midpoint = getMidpoint();
        int midX = midpoint.x;
        int midY = midpoint.y;

        int numVertices = vertices.length;
        Point[] resizedVertices = new Point[numVertices];
        for (int i = 0; i < numVertices; i++) {
            Point vertex = vertices[i];
            int x = (int) (factor * (vertex.x - midX) + midX + 0.5);
            int y = (int) (factor * (vertex.y - midY) + midY + 0.5);
            resizedVertices[i] = new Point(x, y);
        }
        return new Polygon(resizedVertices);
    }

    /**
     * Creates an identical polygon that has been rotated the given angle about the given origin.
     * 
     * @param phi angle through which to rotate the polygon (in radians)
     * @param origin
     */
    public Polygon rotate(double phi, Point origin) {
        int numVertices = vertices.length;
        Point[] rotatedVertices = new Point[numVertices];

        double r;
        double theta;
        for (int i = 0; i < numVertices; i++) {
            int x = vertices[i].x - origin.x;
            int y = vertices[i].y - origin.y;
            // System.out.println("vertex initially (" + x + ", " + y + ")");

            // Convert Cartesian to polar
            r = Math.sqrt(x * x + y * y);
            theta = Math.atan2(y, x);
            // System.out.println("r = " + r + ", theta = " + theta);

            // Rotate
            theta += phi;
            // System.out.println("After rotate, theta = " + theta);

            // Convert polar back to Cartesian
            x = (int) (r * Math.cos(theta) + 0.5) + origin.x;
            y = (int) (r * Math.sin(theta) + 0.5) + origin.y;
            rotatedVertices[i] = new Point(x, y);
        }
        return new Polygon(rotatedVertices);
    }

    public Polygon deepCopy() {
        // Copy vertices
        int numVertices = vertices.length;
        Point[] copiedVertices = new Point[numVertices];
        for (int i = 0; i < numVertices; i++) {
            int x = vertices[i].x;
            int y = vertices[i].y;
            copiedVertices[i] = new Point(x, y);
        }
        return new Polygon(copiedVertices);
    }

    private class HorizEdge implements Comparable<HorizEdge> {
        final int startPt;
        final int endPt;

        HorizEdge(int start, int end) {
            startPt = Math.min(start, end);
            endPt = Math.max(start, end);
        }

        @Override
        public int compareTo(HorizEdge other) {
            return startPt - other.startPt;
        }

        @Override
        public String toString() {
            return "hEdge(" + startPt + ", " + endPt + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Polygon {");
        for (int i = 0; i < vertices.length; i++) {
            sb.append("(").append(vertices[i].x).append(", ").append(vertices[i].y).append("), ");
        }
        sb.append("}");
        return sb.toString();
    }

    /*
     * ********************************FOR TESTING************************************************
     */
    // private void printDrawEdges() {
    // ArrayList<LinkedList<Integer>> edges = makeEdgesForScanlineFill();
    // System.out.println("\n\n");
    // int minX = vertices[0].x; // The minimum y value in the polygon
    // int maxX = minX; // The maximum y value in the polygon
    // for (int i = 1; i < vertices.length; i++) {
    // int x = vertices[i].x;
    // if (x < minX) {
    // minX = x;
    // } else if (x > maxX) {
    // maxX = x;
    // }
    // }
    // for (LinkedList<Integer> row : edges) {
    // Collections.sort(row);
    // Iterator<Integer> rowItr = row.iterator();
    // int nextEdge = rowItr.hasNext() ? rowItr.next() : minX - 1;
    // for (int x = minX; x <= maxX; x++) {
    // if (x == nextEdge) {
    // printChar(true);
    // if (rowItr.hasNext()) {
    // nextEdge = rowItr.next();
    // }
    // } else {
    // printChar(false);
    // }
    // }
    // System.out.print("   " + row + "\n");
    // }
    // }

    public String printDraw() {
        StringBuilder builder = new StringBuilder();
        ArrayList<LinkedList<Integer>> edges = makeEdgesForScanlineFill();
        int minX = vertices[0].x; // The minimum y value in the polygon
        int maxX = minX; // The maximum y value in the polygon
        for (int i = 1; i < vertices.length; i++) {
            int x = vertices[i].x;
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
        }
        for (LinkedList<Integer> row : edges) {
            Collections.sort(row);
            Iterator<Integer> rowItr = row.iterator();
            int nextEdge = rowItr.hasNext() ? rowItr.next() : minX - 1;
            boolean inside = false;
            for (int x = minX; x <= maxX; x++) {
                if (inside) {
                    while (x == nextEdge) {
                        inside = !inside;
                        if (rowItr.hasNext()) {
                            nextEdge = rowItr.next();
                        } else {
                            break;
                        }
                    }
                    builder.append("8");
                } else {
                    builder.append("-");
                    while (x == nextEdge) {
                        inside = !inside;
                        if (rowItr.hasNext()) {
                            nextEdge = rowItr.next();
                        } else {
                            break;
                        }
                    }
                }
            }
            builder.append("   ").append(row).append("\n");
        }
        return builder.toString();
    }

    // private void printDrawInnards() { // Terribly inefficient!
    // List<Point> innards = getInnards();
    // int minX = Integer.MAX_VALUE;
    // int minY = minX;
    // int maxX = Integer.MIN_VALUE;
    // int maxY = maxX;
    //
    // for (Point innardPt : innards) {
    // int innardX = innardPt.x;
    // int innardY = innardPt.y;
    // if (innardX < minX) {
    // minX = innardX;
    // }
    // if (innardX > maxX) {
    // maxX = innardX;
    // }
    // if (innardY < minY) {
    // minY = innardY;
    // }
    // if (innardY > maxY) {
    // maxY = innardY;
    // }
    // }
    //
    // for (int y = minY; y <= maxY; y++) {
    // for (int x = minX - 1; x <= maxX + 1; x++) {
    // if (innards.remove(new Point(x, y))) {
    // printChar(true);
    // } else {
    // printChar(false);
    // }
    // }
    // System.out.print("\n");
    // }
    //
    // }

    static Polygon makeStar() {
        return new Polygon(new Point[] {
                new Point(12, 0),
                new Point(15, 7),
                new Point(24, 7),
                new Point(17, 12),
                new Point(20, 21),
                new Point(12, 15),
                new Point(4, 21),
                new Point(7, 12),
                new Point(0, 7),
                new Point(9, 7) });
    }

    static Polygon makeSquare() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(8, 0),
                new Point(8, 8),
                new Point(0, 8), });
    }

    static Polygon makeUtah() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(8, 0),
                new Point(8, 8),
                new Point(20, 8),
                new Point(20, 20),
                new Point(0, 20), });
    }

    static Polygon makeTimeTurner() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(10, 0),
                new Point(0, 10),
                new Point(10, 10) });
    }

    static Polygon makeDiamond() {
        return new Polygon(new Point[] {
                new Point(4, 0),
                new Point(8, 4),
                new Point(4, 8),
                new Point(0, 4) });
    }

    static Polygon makeScaleneTriangle() {
        return new Polygon(new Point[] { new Point(20, 0), new Point(40, 8), new Point(60, 8), });
    }

    static Polygon makeBittenHouse() {
        return new Polygon(new Point[] {
                new Point(4, 0),
                new Point(8, 3),
                new Point(5, 6),
                new Point(8, 9),
                new Point(0, 9),
                new Point(0, 3) });
    }

    static Polygon makeStairs() {
        return new Polygon(new Point[] {
                new Point(0, 5),
                new Point(1, 5),
                new Point(1, 4),
                new Point(2, 4),
                new Point(2, 3),
                new Point(3, 3),
                new Point(3, 2),
                new Point(4, 2),
                new Point(4, 1),
                new Point(5, 1),
                new Point(5, 0),
                new Point(0, 0) });
    }

    static Polygon makeU() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 10),
                new Point(8, 10),
                new Point(8, 0),
                new Point(12, 0),
                new Point(12, 14),
                new Point(0, 14) });
    }

    static Polygon makeMiniU() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(2, 0),
                new Point(2, 2),
                new Point(4, 2),
                new Point(4, 0),
                new Point(6, 0),
                new Point(6, 4),
                new Point(0, 4) });
    }

    static Polygon makeMiniH() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(2, 0),
                new Point(2, 2),
                new Point(4, 2),
                new Point(4, 0),
                new Point(6, 0),
                new Point(6, 6),
                new Point(4, 6),
                new Point(4, 4),
                new Point(2, 4),
                new Point(2, 6),
                new Point(0, 6) });
    }

    static Polygon makeCross() {
        return new Polygon(new Point[] {
                new Point(2, 2),
                new Point(2, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(6, 2),
                new Point(6, 4),
                new Point(4, 4),
                new Point(4, 6),
                new Point(2, 6),
                new Point(2, 4),
                new Point(0, 4),
                new Point(0, 2) });
    }

    static Polygon makeIncrement() {
        return new Polygon(new Point[] {
                new Point(0, 2),
                new Point(2, 2),
                new Point(2, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(6, 2),
                new Point(6, 0),
                new Point(8, 0),
                new Point(8, 2),
                new Point(10, 2),
                new Point(10, 4),
                new Point(8, 4),
                new Point(8, 6),
                new Point(6, 6),
                new Point(6, 4),
                new Point(4, 4),
                new Point(4, 6),
                new Point(2, 6),
                new Point(2, 4),
                new Point(0, 4), });
    }

    static Polygon makeMiniI() { // TODO
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(6, 0),
                new Point(6, 2),
                new Point(4, 2),
                new Point(4, 3),
                new Point(6, 3),
                new Point(6, 5),
                new Point(0, 5),
                new Point(0, 3),
                new Point(2, 3),
                new Point(2, 2),
                new Point(0, 2) });
    }

    static Polygon makeMiniC() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(2, 2),
                new Point(2, 4),
                new Point(4, 4),
                new Point(4, 6),
                new Point(0, 6), });
    }

    static Polygon makeMinierC() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(4, 0),
                new Point(4, 2),
                new Point(2, 2),
                new Point(2, 3),
                new Point(4, 3),
                new Point(4, 5),
                new Point(0, 5), });
    }

    static Polygon makeCastle() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(5, 0),
                new Point(5, 10),
                new Point(10, 5),
                new Point(15, 10),
                new Point(20, 5),
                new Point(25, 10),
                new Point(25, 0),
                new Point(30, 0),
                new Point(30, 30),
                new Point(0, 30) });
    }

    static Polygon makePentagon() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(10, 6),
                new Point(6, 14),
                new Point(-6, 14),
                new Point(-10, 6) });
    }

    static Polygon makeCrossOverStar() {
        return new Polygon(new Point[] {
                new Point(0, 0),
                new Point(6, 14),
                new Point(-10, 6),
                new Point(10, 6),
                new Point(-6, 14) });
    }

    public static void main(String[] args) {
        Polygon a = makeSquare();
        // a.printDrawEdges();
        // System.out.println("\n");
        a.printDraw();
        System.out.println();
        // a.printDrawInnards();
    }
}
