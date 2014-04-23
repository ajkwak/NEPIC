package nepic.geo;

import java.awt.Point;
import nepic.geo.test.ExemplarBlobs;
import nepic.geo.test.TestIdTaggedImage;

public class BlobTestsManual {

    private static TestIdTaggedImage displayEdges(Blob blob) {
        TestIdTaggedImage edgeImg = new TestIdTaggedImage(
                blob.getMinX(), blob.getMaxX(), blob.getMinY(), blob.getMaxY());
        int edgeId = 1;
        edgeImg.createId(edgeId, '*');
        for (Point point : blob.getEdges()) {
            edgeImg.setIdOrThrow(point.x, point.y, edgeId); // Edges should not exhibit repeats.
        }
        return edgeImg;
    }

    private static TestIdTaggedImage displayAllPoints(Blob blob) {
        TestIdTaggedImage edgeImg = new TestIdTaggedImage(
                blob.getMinX(), blob.getMaxX(), blob.getMinY(), blob.getMaxY());
        int id = 1;
        edgeImg.createId(id, '8');
        for (Point point : blob.getAllPoints()) {
            edgeImg.setIdOrThrow(point.x, point.y, id); // Should not exhibit repeats.
        }
        return edgeImg;
    }

    private static TestIdTaggedImage displayBlobDetail(Blob blob) {
        TestIdTaggedImage edgeImg = new TestIdTaggedImage(
                blob.getMinX(), blob.getMaxX(), blob.getMinY(), blob.getMaxY());
        int edgeId = 1;
        edgeImg.createIdOrThrow(edgeId, '*');
        for (Point point : blob.getEdges()) {
            edgeImg.setIdOrThrow(point.x, point.y, edgeId); // Edges should not exhibit repeats.
        }
        int innardId = 2;
        edgeImg.createIdOrThrow(innardId, '0');
        for (Point point : blob.getInnards()) {
            edgeImg.setIdOrThrow(point.x, point.y, innardId); // Innards should not exhibit repeats.
        }
        return edgeImg;
    }

    public static void main(String[] args) throws IllegalAccessException {
        // Test all Blob exemplars.
        for (ExemplarBlobs.Builder blobBldr : ExemplarBlobs.getExemplarBlobBuilders()) {
            System.out.println("\n\n" + blobBldr.getBlobName());
            Blob blob = blobBldr.buildBlob();
            System.out.println(displayBlobDetail(blob));
            System.out.println(displayAllPoints(blob));
        }
    }
}
