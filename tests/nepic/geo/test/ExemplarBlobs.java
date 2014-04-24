package nepic.geo.test;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nepic.geo.Blob;

/**
 * A class in which all of the exemplar {@link Blob}s used for testing the {@link Blob} class are
 * defined. The builders of the exemplar blobs can be retrieved using the
 * {@link #getExemplarBlobBuilders()} method.
 * <p>
 * A {@link ExemplarBlobs.Builder} is returned for each blob rather than returning the blobs
 * directly so that new instances of each exemplar blob can be created at will (making sure that
 * different tests that test the same exemplar blob use different {@link Blob} instances, for
 * example).
 *
 * @author AJ Parmidge
 */
public class ExemplarBlobs {
    private static final List<Builder> EXEMPLAR_BLOB_BUILDERS = makeExemplarBlobBuilders();

    // Add new exemplar blobs here.
    private static final List<ExemplarBlobs.Builder> makeExemplarBlobBuilders() {
        List<ExemplarBlobs.Builder> exemplarBlobs = Lists.newArrayList(
                new Builder("SHALLOW_HEART", Lists.newArrayList(
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
                        new Point(0, 1))),
                new Builder("NARROW_HEART", Lists.newArrayList(
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
                        new Point(0, 1))),
                new Builder("ORNAMENT", Lists.newArrayList(
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
                        new Point(2, 6))),
                new Builder("SIDEWAYS_ORNAMENT", Lists.newArrayList(
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
                        new Point(2, 3))),
                new Builder("SIDEWAYS_ORNAMENT_2", Lists.newArrayList(
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
                        new Point(3, 2))),
                new Builder("HORIZONTAL_TWO_POINT_LINE", Lists.newArrayList(
                        new Point(2, 1),
                        new Point(1, 1))),
                new Builder("VERTICAL_TWO_POINT_LINE", Lists.newArrayList(
                        new Point(1, 1),
                        new Point(1, 2))),
                new Builder("SINGLE_POINT", Lists.newArrayList(new Point(1, 1))));

        for (ExemplarPolygons.Builder polyBldr : ExemplarPolygons.getExemplarPolygonBuilders()) {
            exemplarBlobs.add(new Builder(polyBldr.getPolygonName(),
                    polyBldr.buildPolygon().getEdges()));
        }

        return exemplarBlobs;
    }

    /**
     * Retrieves the {@link ExemplarBlobs.Builder} for every exemplar blob defined in this class.
     *
     * @return the list of the exemplar blob builders
     */
    public static ImmutableList<ExemplarBlobs.Builder> getExemplarBlobBuilders() {
        return ImmutableList.copyOf(EXEMPLAR_BLOB_BUILDERS);
    }

    // This class is uninstantiable.
    private ExemplarBlobs() {
        throw new UnsupportedOperationException();
    }

    /**
     * A class designed to build a single exemplar blob. This allows a new {@link Blob} to be built
     * at will.
     * 
     * @author AJ Parmidge
     */
    public static class Builder {
        private final String blobName;
        private final List<Point> tracedEdges;

        // Used in the makeExemplarBlobs() method.
        private Builder(String polygonName, List<Point> tracedEdges) {
            this.blobName = polygonName;
            this.tracedEdges = tracedEdges;
        }

        /**
         * Gets the name of the exemplar blob that this {@link Builder} was designed to build.
         *
         * @return the name of the exemplar blob
         */
        public String getBlobName() {
            return blobName;
        }

        /**
         * Gets the traced edges of the exemplar blob that this {@link Builder} was designed to
         * build, as they would be passed to the
         * {@link Blob#newBlobFromTracedEdges(java.util.Collection)} method.
         *
         * @return the traced edges used to build the exemplar blob
         */
        public ImmutableList<Point> getTracedEdges() {
            return ImmutableList.copyOf(tracedEdges);
        }

        /**
         * Builds a new instance of the exemplar blob.
         *
         * @return the new {@link Blob} instance
         */
        public Blob buildBlob() {
            return Blob.newBlobFromTracedEdges(tracedEdges);
        }
    }
}
