package nepic.io;

import java.io.File;

/**
 * Collection of static convenience methods for manipulating and getting information about a system
 * files.
 *
 * @author AJ Parmidge
 */
public class Files { // TODO: JUnit!
    /**
     * Gets the file extension of the given file name.
     *
     * @param fileName the name or the classpath of the file
     * @return the extension of the given file name
     */
    public static String getFileExtension(String fileName) {
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Gets the directory of a file from the absolute path of that file.
     * <p>
     * Adapted from ImageJ's {@code ij.io.Opener#getDir(String)} method.
     *
     * @param classpath the absolute path of the file
     * @return the absolute path of the directory in which the file is located
     */
    public static String getDir(String classpath) {
        int i = classpath.lastIndexOf(File.separator);
        if (i > 0) {
            return classpath.substring(0, i);
        }
        return "";
    }

    /**
     * Gets the name of a file from the file's absolute path.
     * <p>
     * Adapted from: ImageJ's {@code ij.io.Opener#getName(String)} method.
     *
     * @param classpath the absolute path of the file
     * @return the name of the file
     */
    public static String getName(String classpath) {
        int i = classpath.lastIndexOf(File.separator);
        if (i < 0) {
            return classpath; // The name of the file is the entire string.
        } else if (i < classpath.length() - 1) {
            return classpath.substring(i + 1);
        } else { // There is no file name to return. In general, this shouldn't happen.
            return "";
        }
    }

}
