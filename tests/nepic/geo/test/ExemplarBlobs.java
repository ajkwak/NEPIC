package nepic.geo.test;

import java.awt.Point;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nepic.geo.Blob;

public class ExemplarBlobs {
    private static final List<Builder> EXEMPLAR_BLOBS = makeExemplarBlobs();

    private static final List<ExemplarBlobs.Builder> makeExemplarBlobs() {
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

    public static ImmutableList<ExemplarBlobs.Builder> getExemplarBlobBuilders() {
        return ImmutableList.copyOf(EXEMPLAR_BLOBS);
    }

    private ExemplarBlobs() {
        throw new UnsupportedOperationException(); // This class is not instantiable.
    }

    public static class Builder {
        private final String blobName;
        private final List<Point> blobCtorParams;

        private Builder(String polygonName, List<Point> blobCtorParams) {
            this.blobName = polygonName;
            this.blobCtorParams = blobCtorParams;
        }

        public String getBlobName() {
            return blobName;
        }

        public Blob buildBlob() {
            return Blob.newBlobFromTracedEdges(blobCtorParams);
        }
    }
}
