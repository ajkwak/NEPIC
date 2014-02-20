package nepic.io;

import static org.junit.Assert.*;

import java.io.File;

import nepic.testing.util.Assertions;

import org.junit.Test;

public class NepicFileFilterTest {

    @Test
    public void accept_TIFF_ONLY() {
        // Returns 'false' for non-TIFF files.
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.txt")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.bmp")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.jpg")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.doc")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.sh")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.csv")));
        assertFalse(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld")));

        // Returns 'true' for TIFF file.
        assertTrue(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.tif")));
        assertTrue(NepicFileFilter.TIFF_ONLY.accept(new File("HelloWorld.tiff")));
        assertTrue(NepicFileFilter.TIFF_ONLY.accept(new File("hello/world.tif")));
        assertTrue(NepicFileFilter.TIFF_ONLY.accept(new File("/Hello/World/the.tiff")));
    }

    @Test
    public void getDescription_TIFF_ONLY() {
        Assertions.assertContains(NepicFileFilter.TIFF_ONLY.getDescription(), "tif");
    }

    @Test
    public void accept_CSV_ONLY() {
        // Returns 'false' for non-CSV files.
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.txt")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.bmp")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.jpg")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.tif")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.sh")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.tiff")));
        assertFalse(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld")));

        // Returns 'true' for CSV file.
        assertTrue(NepicFileFilter.CSV_ONLY.accept(new File("HelloWorld.csv")));
        assertTrue(NepicFileFilter.CSV_ONLY.accept(new File("hello/world.csv")));
        assertTrue(NepicFileFilter.CSV_ONLY.accept(new File("/Hello/World/the.csv")));
    }

    @Test
    public void getDescription_CSV_ONLY() {
        Assertions.assertContains(NepicFileFilter.CSV_ONLY.getDescription(), "csv");
    }

}
