package nepic.geo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import nepic.util.Verify;

/**
 * Class that represents a two-dimensional polygon in Cartesian space.
 *
 * @author AJ Parmidge
 */
public class Polygon implements BoundedRegion {
    private final Point[] vertices;
    private final BoundingBox boundaries;

    /**
     * Creates a polygon with the given vertices.
     * <p>
     * Note that the order of the vertices is important. An edge of the {@link Polygon} is assumed
     * to exist between two adjacent vertices, and between the first and last vertices.
     * <p>
     * For example, assume you want to make a polygon with the vertices (0, 0), (5, 0), (0, 4), and
     * (5, 4). If you constructed the {@link Polygon} as:
     *
     * <pre>
     * Polygon polygon = new Polygon(
     *         new Point(0, 0),
     *         new Point(5, 0),
     *         new Point(5, 4),
     *         new Point(0, 4));
     * </pre>
     *
     * Then you would get the following polygon:
     *
     * <pre>
     * ******
     * *----*
     * *----*
     * *----*
     * ******
     * </pre>
     *
     * However, if you constructed your {@link Polygon} as:
     *
     * <pre>
     * Polygon polygon = new Polygon(
     *         new Point(0, 0),
     *         new Point(5, 4),
     *         new Point(5, 0),
     *         new Point(0, 4));
     * </pre>
     *
     * Then you would get the following polygon:
     *
     * <pre>
     * *----*
     * **--**
     * *-**-*
     * **--**
     * *----*
     * </pre>
     *
     * Note that these two {@link Polygon} objects were created using precisely the same vertices,
     * but in a different order.
     *
     * @param vertices the vertices of the polygon to create
     */
    public Polygon(Point... vertices) {
        int numVertices = vertices.length;
        Verify.argument(numVertices > 2, "Polygon must have at least 3 vertices");
        this.vertices = new Point[numVertices];
        boundaries = new BoundingBox(vertices[0], vertices[0]);
        for (int i = 0; i < numVertices; i++) {
            Point vertex = vertices[i];
            this.vertices[i] = vertex;
            boundaries.update(vertex.x, vertex.y);
        }
    }

    /**
     * Creates a polygon with the given collection of vertices. Note that the order of the given
     * vertices is important. These should be passed to this constructor such that there is an edge
     * between all adjacent vertices in the given collection of vertices. See
     * {@link #Polygon(Point...)} for details.
     *
     * @param vertices the vertices of the polygon to create
     */
    public Polygon(Collection<Point> vertices) {
        int numVertices = vertices.size();
        Verify.argument(numVertices > 2, "Polygon must have at least 3 vertices");
        this.vertices = new Point[numVertices];
        boundaries = new BoundingBox(0, 0, 0, 0); // These boundaries will never be used.
        int i = 0;
        for (Point vertex : vertices) {
            if (i == 0) {
                boundaries.resetBounds(vertex.x, vertex.x, vertex.y, vertex.y);
            } else {
                boundaries.update(vertex.x, vertex.y);
            }
            this.vertices[i] = vertex;
            i++;
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

    /**
     * Gets the bounds of the polygon. The maximum x-value of the returned {@link BoundingBox} is
     * the same as the maximum x-value of this {@link Polygon}. Ditto with the minimum x-value,
     * minimum y-value, and maximum y-value of this {@link Polygon}.
     *
     * @return the bounds of the polygon
     */
    public BoundingBox getBoundingBox() {
        return boundaries.deepCopy();
    }

    /**
     * Gets a copy of the vertices in this {@link Polygon}, given in the order in which the vertices
     * were specified in the {@link Polygon#Polygon(Point[]) Polygon constructor}.
     *
     * @return the vertices of this polygon
     */
    public ArrayList<Point> getVertices() {
        ArrayList<Point> verticesCopy = Lists.newArrayListWithCapacity(vertices.length);
        for (Point vertex : vertices) {
            // Need to create a new point because Point objects are mutable, and we don't want the
            // user to mutate the Polygon's vertices by mutating the result of this method.
            verticesCopy.add(new Point(vertex.x, vertex.y));
        }
        return verticesCopy;
    }

    /**
     * Gets all of the points (integer-precision) that are in the edges of this polygon.
     *
     * @return a list of the points in this polygon's edges
     */
    public LinkedList<Point> getEdges() {
        LinkedList<Point> edges = new LinkedList<Point>();
        Point lastPoint = vertices[vertices.length - 1];
        for (int i = 0; i < vertices.length; i++) {
            Point currentPoint = vertices[i];
            edges.addAll(new LineSegment(lastPoint, currentPoint).draw(
                    LineSegment.IncludeStart.YES, LineSegment.IncludeEnd.NO));
            lastPoint = currentPoint;
        }
        return edges;
    }

    /**
     * Translates (moves) this polygon the given amount in the x and y directions. Note that this
     * function is done to the polygon <i> in place </i>.
     *
     * @param x the amount to move this polygon in the x-direction (negative values will move the
     *        polygon to the left, while positive values will move the polygon to the right)
     * @param y the amount to move this polygon in the y-direction (negative values will move the
     *        polygon up, while positive values will move the polygon down)
     */
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
        boundaries.resetBounds(newMinX, newMaxX, newMinY, newMaxY);
    }

    /**
     * Creates a new polygon with the same basic shape as this polygon, but which has been resized
     * by the given factor.
     *
     * @param factor the factor by which to change the polygon's size. A factor < 1 will shrink the
     *        polygon, while a factor > 1 will enlarge the polygon
     * @return the resized polygon
     */
    public Polygon resize(double factor) {
        // Enlarge the polygon around the midpoint of the current polygon's boundaries.
        Point midpoint = boundaries.getMidPoint();
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
     * Creates a new polygon with the same basic shape as this polygon, but which has been rotated
     * by the given amount around the given origin.
     *
     * @param phi the angle (in radians) through which to rotate the polygon
     * @param origin the point around which to rotate the polygon
     * @return the rotated polygon
     */
    public Polygon rotate(double phi, Point origin) {
        int numVertices = vertices.length;
        Point[] rotatedVertices = new Point[numVertices];

        double r;
        double theta;
        for (int i = 0; i < numVertices; i++) {
            int x = vertices[i].x - origin.x;
            int y = vertices[i].y - origin.y;

            // Convert Cartesian point to polar coordinates.
            r = Math.sqrt(x * x + y * y);
            theta = Math.atan2(y, x);

            // Rotate
            theta += phi;

            // Convert polar coordinates back to Cartesian coordinates.
            x = (int) (r * Math.cos(theta) + 0.5) + origin.x;
            y = (int) (r * Math.sin(theta) + 0.5) + origin.y;
            rotatedVertices[i] = new Point(x, y);
        }
        return new Polygon(rotatedVertices);
    }

    /**
     * Creates an exact copy of the current polygon.
     *
     * @return the copy of this polygon
     */
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

    /**
     * Creates a {@link Blob} representation of this {@link Polygon}.
     *
     * @return the {@link Blob} representation of this {@link Polygon}
     */
    public Blob asBlob() {
        return Blob.newBlobFromTracedEdges(getEdges());
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

    /**
     * Draws this {@link Polygon} using ASCII characters. Each point (integer precision) in this
     * polygon is represented by a single ASCII character. An edge point in the Polygon is
     * represented by the '{@code *}' character, while points that are not in the polygon's edges
     * are represented by the '{@code -}' character.
     * <p>
     * For example, the result of
     *
     * <pre>
     * new Polygon(new Point(-3, 0), new Point(3, 0), new Point(0, 3)).drawAsAsciiArt();
     * </pre>
     *
     * would be:
     *
     * <pre>
     * *******
     * -*---*-
     * --*-*--
     * ---*---
     * </pre>
     *
     * @return the ASCII-art representation of this polygon
     */
    public String drawAsAsciiArt() {
        // Create a map of the rows in the polygon.
        int minY = getMinY();
        int numRows = getMaxY() - minY + 1;
        int minX = getMinX();
        int numColumns = getMaxX() - minX + 1;
        ArrayList<TreeSet<Integer>> rowEdgeMap = Lists.newArrayListWithCapacity(numRows);

        // Initialize the rows in the map.
        for (int i = 0; i < numRows; i++) {
            rowEdgeMap.add(new TreeSet<Integer>()); // TreeSets are naturally sorted.
        }

        // Add the edge points of the polygon to the map.
        for(Point edgePt : getEdges()){
            int row = edgePt.y - minY;
            int column = edgePt.x - minX;
            rowEdgeMap.get(row).add(column);
        }

        // Draw the polygon from the map.
        StringBuilder builder = new StringBuilder(
                numRows * (numColumns + 1 /* include carriage returns */));
        for(TreeSet<Integer> rowEdges : rowEdgeMap){
            int x = 0;
            for(int edgeColumn : rowEdges){
                while(x < edgeColumn){
                    builder.append('-');
                    x++;
                }
                builder.append('*');
                x++;
            }
            while (x < numColumns) {
                builder.append('-');
                x++;
            }
            builder.append('\n');
        }

        return builder.toString();
    }
}
