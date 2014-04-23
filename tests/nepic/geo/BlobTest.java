package nepic.geo;

import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import nepic.geo.test.ExemplarBlobs;
import nepic.geo.test.TestIdTaggedImage;
import nepic.testing.util.Assertions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * JUnit tests for {@link Blob}.
 *
 * @author AJ Parmidge
 */
@RunWith(Parameterized.class)
public class BlobTest {
    private final ExemplarBlobs.Builder blobBldr; // The object that builds the blobs for each test.
    private Blob blob; // The blob to test (remade for each test).

    public BlobTest(ExemplarBlobs.Builder blobBldr) {
        this.blobBldr = blobBldr;
    }

    @Before
    public void setUp() {
        blob = blobBldr.buildBlob();
    }

    @Test
    public void testInnardsAndEdgesDistinct() {
        Assertions.assertDistinct(blob.getEdges(), blob.getInnards());
    }

    @Test
    public void getEdges_containsSameElementsAsOriginalTracedEdges() {
        Assertions.assertContainSameElements(blobBldr.getTracedEdges(), blob.getEdges());
    }

    @Test
    public void getInnards_noPointsTouchingNonBlobPoint() {
        // Set up the image to test.
        TestIdTaggedImage img = new TestIdTaggedImage(
                blob.getMinX(), blob.getMaxX(), blob.getMinY(), blob.getMaxY());
        int blobId = 1;
        img.createId(blobId, '*');
        for (Point point : blob.getAllPoints()) {
            img.setIdOrThrow(point.x, point.y, blobId);
        }

        // Test that no innard points are touching points that are not in the Blob.
        for (Point point : blob.getInnards()) {
            int x = point.x;
            int y = point.y;
            // To be a true innard point, this point must be FULLY surrounded by other points in the
            // Blob (which are all contained by img).
            if (!img.contains(x - 1, y) || img.getId(x - 1, y) != blobId
                    || !img.contains(x + 1, y) || img.getId(x + 1, y) != blobId
                    || !img.contains(x, y - 1) || img.getId(x, y - 1) != blobId
                    || !img.contains(x, y + 1) || img.getId(x, y + 1) != blobId) {
                fail("(" + x + "," + y + ") is not a true innard point!");
            }
        }
    }

    @Test
    public void getAllPoints_equivalentToUnionOfEdgesAndInnards() {
        // Set up the union of the Blob edges and innard points.
        List<Point> edges = blob.getEdges();
        List<Point> innards = blob.getInnards();
        List<Point> union = Lists.newArrayListWithCapacity(edges.size() + innards.size());
        union.addAll(edges);
        union.addAll(innards);

        // Test that the result of getAllPoints() contains the same points as the union.
        Assertions.assertEqualsAnyOrder(union, blob.getAllPoints());
    }

    @Parameters//(name= "{index}: {0}") // This parameter added in JUnit 4.11 (NEED UPDATE VERSION)
    public static Collection<Object[]> getBlobs() {
        Collection<Object[]> blobBuilderList = new LinkedList<Object[]>();

        for (ExemplarBlobs.Builder blobBldr : ExemplarBlobs.getExemplarBlobBuilders()) {
            blobBuilderList.add(new Object[] { blobBldr });
        }

        return blobBuilderList;
    }
}
