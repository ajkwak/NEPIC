package nepic.geo;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import nepic.geo.test.ExemplarBlobs;
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
