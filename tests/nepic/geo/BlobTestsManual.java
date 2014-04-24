package nepic.geo;

import java.awt.Point;
import nepic.geo.test.ExemplarBlobs;
import nepic.geo.test.TestIdTaggedImage;

/**
 * Manual tests for {@link Blob}. The contents of this class are not expected to remain constant.
 * The purpose of this class is to do initial testing on new {@link Blob} methods, before automated
 * testing is added to the {@link BlobTest} class, and to help debug problems when these automated
 * tests fail.
 * <p>
 * <b><i> IF YOU PUT TESTS HERE, DO NOT EXPECT THEM TO REMAIN HERE. </b></i> After debugging your
 * code, add all tests to {@link BlobTest}.
 *
 * @author AJ Parmidge
 */
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

    /**
     * Runs the manual tests of the {@link Blob} class. Results of these tests will be printed using
     * {@link System#out}.
     * 
     * @param args these are not used
     */
    public static void main(String[] args) {
        // Test all Blob exemplars.
        for (ExemplarBlobs.Builder blobBldr : ExemplarBlobs.getExemplarBlobBuilders()) {
            System.out.println("\n\n" + blobBldr.getBlobName());
            Blob blob = blobBldr.buildBlob();
            System.out.println(displayBlobDetail(blob));
            System.out.println(displayAllPoints(blob));
        }
    }
}
