package nepic.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/*
 * Code Adapted from:
 * http://download.oracle.com/javase/tutorial/uiswing/components/filechooser.html
 * http://download.oracle.com/javase/1.4.2/docs/api/javax/swing/filechooser/FileFilter.html
 */

/**
 * File filter used by WormAnal so that only TIFF files are shown in dialog box when user wants to
 * select files to analyze
 * 
 * @author AJ Parmidge
 * @since ManualCBFinder_v1-6 (Called WormAnalFileFilter before AutoCBFinder_Alpha_v0-9-2013-01-29)
 * @version AutoCBFinder_Alpha_v0-9-2013-01-27
 */
public class NepicFileFilter extends FileFilter {
    /**
     * Kind of File for the Filter to Accept: Allows the user to select a single TIFF file
     */
    protected static final int TIFS_ONLY = 1;

    protected static final int CSV_ONLY = 2;
    /**
     * The kind of file that the specific instance of the file filter accepts (must be either
     * WormAnalFileFilter.FOLDERS_AND_TIFFS or WormAnalFileFilter.TIFFS_ONLY)
     */
    private int toAccept;

    /**
     * Makes an instance of the File Filter based on whether to select single file, or entire
     * directory of files.
     * 
     * @param filesToAccept Type of File(s) that FileFilter will allow user to see when choosing
     *        images to analyze.
     */
    protected NepicFileFilter(int filesToAccept) {
        toAccept = filesToAccept;
    }// WormAnalFileFilter

    @Override
    /**
     * Determines whether a specific file will be allowed by the File Filter
     * @param theFile The file to check if it is allowed by the FileFilter
     * @return true if the file is allowed by the filter, false if the file is in an unacceptable format (and so the user should not be able to see it)
     */
    public boolean accept(File theFile) {
        if (theFile.isDirectory() || hasCorrectFileExtention(theFile)) {
            return true;
        }// if theFile is a TIFF file
        return false;
    }// accept

    private static String getFileExtention(File theFile) {
        String ext = null;
        String fileName = theFile.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }// if found extention
        return ext;
    }// getFileExtension

    private boolean hasCorrectFileExtention(File theFile) {
        String extention = getFileExtention(theFile);
        if (extention != null)
            return toAccept == CSV_ONLY ? (extention.equals("csv"))
                    : (extention.equals("tif") || extention.equals("tiff"));
        return false;
    }// hasCorrectFileExtention

    @Override
    /**
     * Obtains a description of the files that the user can select within the parameters of the given file filter
     * @return Description of files allowed by file filter
     */
    public String getDescription() {
        if (toAccept == TIFS_ONLY)
            return "Tagged Image File Format (*.tif, *.tiff)";
        else if (toAccept == CSV_ONLY)
            return "Comma-Separated Value File Format (*.csv)";
        return null;
    }// getDescription

}// WormAnalFileFilter