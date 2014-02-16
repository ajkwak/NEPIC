package nepic.io;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * JUnit tests for {@link Files}.
 *
 * @author AJ Parmidge
 */
public class FilesTest {
    private static final String SEP = File.separator;

    @Test
    public void getFileExtension() {
        assertEquals("txt", Files.getFileExtension("answers.txt"));
        assertEquals("sh", Files.getFileExtension("file123.sh"));
        assertEquals("txt", Files.getFileExtension("path" + SEP + "to" + SEP + "answers.txt"));
        assertEquals("", Files.getFileExtension("path" + SEP + "to" + SEP + "answers"));
        assertEquals("", Files.getFileExtension("path" + SEP + "to" + SEP + "answers."));
        assertEquals("", Files.getFileExtension("answers"));
        assertEquals("", Files.getFileExtension("answers."));
        assertEquals("", Files.getFileExtension("."));
        assertEquals("", Files.getFileExtension(""));
    }

    @Test
    public void getDir() {
        String directoryName = "path" + SEP + "to" + SEP + "MyDirectory";
        assertEquals("MyDirectory", Files.getDir("MyDirectory" + SEP));
        assertEquals(directoryName, Files.getDir(directoryName + SEP + "answers.txt"));
        assertEquals(directoryName, Files.getDir(directoryName + SEP + "answers."));
        assertEquals(directoryName, Files.getDir(directoryName + SEP + "answers"));
        assertEquals(directoryName, Files.getDir(directoryName + SEP + "a"));
        assertEquals(directoryName, Files.getDir(directoryName + SEP + "."));
    }

    @Test
    public void getName() {
        assertEquals("", Files.getName("MyDirectory/"));
        assertEquals("MyDirectory", Files.getName("MyDirectory"));
        assertEquals("answers.txt", Files.getName("path" + SEP + "to" + SEP
                + "answers.txt"));
        assertEquals("answers", Files.getName("path" + SEP + "to" + SEP + "answers"));
        assertEquals("answers.", Files.getName("path" + SEP + "to" + SEP + "answers."));
        assertEquals("a", Files.getName("path" + SEP + "to" + SEP + "a"));
    }

}
