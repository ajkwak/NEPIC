package nepic.io;

import java.awt.image.*;
import java.io.*;

import nepic.Nepic;
import nepic.image.ImagePage;
import nepic.logging.EventLogger;
import nepic.logging.EventType;

/**
 * Allows WormAnal to successfully load Pixel information from Tagged-Image-File-Format (TIFF)
 * files. Adapted from ImageJ ij.io.Opener and ij.io.FileOpener classes (see individual methods for
 * specifics on origin).
 *
 * @author AJ Parmidge
 * @since ManualCBFinder_v1-6
 * @version AutoCBFinder_Alpha_v0-9120112
 */
public class TiffOpener {
    /**
     * Information about the TIFF file that this TiffOpener is going to try to read.
     */
    private FileInfo tiffInfo = null;

    // dummy constructor

    /**
     * Creates an instance of the TiffOpener class.
     */
    public TiffOpener() {
    }// no-parameter constructor of TiffOpener

    // public methods

    /**
     * Attempts to open the specified file as a TIFF. Adapted from: openTiff method and openTiff2
     * method of ImageJ ij.io.Opener class, open method of ImageJ ij.io.FileOpener class.
     *
     * @param directory The class path of the directory of the file to open.
     * @param name The name of the file to open.
     */
    public boolean loadTiffInfo(String classPath) {
        TiffDecoder td = new TiffDecoder(Files.getDir(classPath), Files.getName(classPath));
        FileInfo[] info = null;
        try {
            info = td.getTiffInfo();
            if (info != null) {
                if (info.length == 1) {
                    tiffInfo = info[0];// should never be null based on TiffDecoder code
                    return true;
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < info.length; i++) {
                        builder.append("\tinfo[").append(i).append("] is: ").append(info[i]);
                    }// for all images in info[]

                    Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                            "TiffOpener, openTiff: Unhandled Pic Info; info.length ==",
                            info.length, "(>1 FileInfo objects generated for", classPath, ")",
                            builder);
                }// else: TIFF is a stack of multiple-page TIFFs
            }// if TiffDecoder was able to find FileInfo
            return false;
        } catch (IOException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                    "TiffOpener, openTiff: Unable to getTiffInfo for", classPath,
                    "from TiffDecoder:", EventLogger.formatException(e));
            return false;
        }// catch IOException
    }// openTiff

    public ImagePage openTiffPage(int pageNum) {
        long skip = tiffInfo.getOffset();
        if (pageNum > 0)
            skip = skip + pageNum * tiffInfo.width * tiffInfo.height * tiffInfo.getBytesPerPixel()
                    + (pageNum - 1) * tiffInfo.gapBetweenImages;
        try {
            ImageReader reader = new ImageReader(tiffInfo);
            InputStream is = createInputStream();
            if (is == null)
                return null;
            Object pixels;
            pixels = reader.readPixels(is, skip);// skip = offset the 1st time, then = gap between
                                                 // images
            ImagePage pgToReturn = new ImagePage(getPageWidth(), getPageHeight());
            int fileType = tiffInfo.fileType;
            if (fileType == FileInfo.GRAY8 || fileType == FileInfo.COLOR8
                    || fileType == FileInfo.BITMAP) {
                convertFromByteArray(pixels, pgToReturn);// must convert pixels from byte to int
            } else if (fileType == FileInfo.GRAY16_SIGNED || fileType == FileInfo.GRAY16_UNSIGNED
                    || fileType == FileInfo.GRAY12_UNSIGNED) {
                convertFromShortArray(pixels, pgToReturn);
            } else if (fileType == FileInfo.RGB || fileType == FileInfo.BGR
                    || fileType == FileInfo.ARGB || fileType == FileInfo.ABGR
                    || fileType == FileInfo.BARG || fileType == FileInfo.RGB_PLANAR) {
                convertFromIntArray(pixels, pgToReturn);
            } else {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                        "unhandled file type (unable to parse).  fileType code =", fileType);
            }
            is.close();
            return pgToReturn;
        } catch (Exception e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, EventLogger.formatException(e));
            return null;
        }// catch and print all exceptions
    }// openStack

    /**
     * Gets the height of the last image opened by this TiffOpener (in Pixels)
     *
     * @return The height of the pages in the last TIFF file opened. If no TIFF file has been opened
     *         successfully, returns -1.
     */
    public int getPageHeight() {
        if (tiffInfo == null) {
            return -1;
        }// if haven't attempted to open a file yet
        return tiffInfo.height;
    }// getPageWidth

    /**
     * Gets the width of the last image opened by this TiffOpener (in Pixels)
     *
     * @return The width of the pages in the last TIFF file opened. If no TIFF file has been opened
     *         successfully, returns -1.
     */
    public int getPageWidth() {
        if (tiffInfo == null) {
            return -1;
        }// if haven't attempted to open a file yet
        return tiffInfo.width;
    }// getPageWidth

    /**
     * Creates an image (that can be displayed on screen) of the indicated page of the currently
     * loaded TIFF.
     *
     * @param pageNum The page of the TIFF of which to make a displayable image.
     * @return Pictorial representation of the indicated page of the loaded image. Returns null if
     *         no TIFF has been loaded.
     */
    public BufferedImage rgbToBufferedImage(int[] page) {// TODO: more efficient way to do this
        if (tiffInfo == null)
            return null;
        int imgWidth = tiffInfo.width;
        int imgHeight = tiffInfo.height;
        BufferedImage toReturn = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        int i = 0;
        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                toReturn.setRGB(x, y, page[i]);
                i++;
            }// for all y in image page
        }// for all x in image page
        return toReturn;
    }// rgbToBufferedImage

    public int getNumPagesInTiff() {
        return tiffInfo.nImages;
    }// getNumPagesInTiff

    // private supporting methods

    /**
     * Converts an array of bytes (representing pixel luminosity for images saved in 8-bit
     * grayscale) to an array of RGB (32-bit color) values
     *
     * @param pixels Represents the array of bytes describing the 8-bit grayscale coloring of each
     *        pixel in the page being processed
     * @return The RGB values of each pixel in the page being processed
     */
    private void convertFromByteArray(Object pixels, ImagePage page) {
        try {
            byte[] grayscaleArray = (byte[]) pixels;
            int i = 0;
            for (int y = 0; y < page.height; y++) {
                for (int x = 0; x < page.width; x++) {
                    page.setRGB(x, y, grayscaleArray[i]);
                    i++;
                }// for all y in image page
            }// for all x in image page
        } catch (Exception e) {
            // never called if tiffInfo == null, so should never throw exception due to below code
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "unable to cast 'pixels' to byte in",
                    tiffInfo.fileName, ":" + EventLogger.formatException(e));
        }// catch all exceptions
    }// byteToIntArray

    /**
     * Converts an array of bytes (representing pixel luminosity for images saved in 8-bit
     * grayscale) to an array of RGB (32-bit color) values
     *
     * @param pixels Represents the array of bytes describing the 8-bit grayscale coloring of each
     *        pixel in the page being processed
     * @return The RGB values of each pixel in the page being processed
     */
    private void convertFromShortArray(Object pixels, ImagePage page) {
        try {
            short[] grayscaleArray = (short[]) pixels;
            int min = 0xffff;
            for (int i = 0; i < grayscaleArray.length; i++) {
                int possNewMin = (0xfff & grayscaleArray[i]);
                if (possNewMin < min) {
                    min = possNewMin;
                }
            }// for: find min of grayscale array
            int i = 0;
            for (int y = 0; y < page.height; y++) {
                for (int x = 0; x < page.width; x++) {
                    // in these images, CB is relatively dim, so just take lowest 255
                    int lowVal = (grayscaleArray[i] & 0xffff) - min;
                    if (lowVal > 255) {
                        lowVal = 255;
                    }
                    page.setRGB(x, y, (byte) lowVal);
                    i++;
                }// for all y in image page
            }// for all x in image page
        } catch (Exception e) {
            // never called if tiffInfo == null, so should never throw exception due to below code
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                    "unable to cast 'pixels' to short[] in", tiffInfo.fileName, ":", EventLogger
                            .formatException(e));
        }// catch all exceptions
    }// byteToIntArray

    /**
     * Converts an array of bytes (representing pixel luminosity for images saved in 8-bit
     * grayscale) to an array of RGB (32-bit color) values
     *
     * @param pixels Represents the array of bytes describing the 8-bit grayscale coloring of each
     *        pixel in the page being processed
     * @return The RGB values of each pixel in the page being processed
     */
    private void convertFromIntArray(Object pixels, ImagePage page) {
        try {
            int[] grayscaleArray = (int[]) pixels;
            int i = 0;
            for (int y = 0; y < page.height; y++) {
                for (int x = 0; x < page.width; x++) {
                    page.setRGB(x, y, (byte) (255 & grayscaleArray[i]));
                    i++;
                }// for all y in image page
            }// for all x in image page
        } catch (Exception e) {
            // never called if tiffInfo == null, so should never throw exception due to below code
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                    "unable to cast 'pixels' to short[] in", tiffInfo.fileName, ":", EventLogger
                            .formatException(e));
        }// catch all exceptions
    }// byteToIntArray

    public int[] findMinAndMax(short[] set) {
        int min = 65535;
        int max = 0;
        for (int i = 0; i < set.length; i++) {
            int value = set[i] & 0xffff;
            if (value < min)
                min = value;
            if (value > max)
                max = value;
        }
        return new int[] { min, max };
    }

    /**
     * Creates an InputStream for loading the TIFF based on the FileInfo generated for the TIFF (the
     * tiffInfo value) From createInputStream method of ImageJ ij.io.FileOpener class.
     *
     * @return An InputStream for the image described by tiffInfo (assumes tiffInfo has already been
     *         initialized).
     * @throws IOException
     */
    private InputStream createInputStream() throws IOException {
        String sep = File.separator;
        if (tiffInfo.inputStream != null) {// does this ever happen?
            return tiffInfo.inputStream;
        }// if tiffInfo includes the input information
        if (tiffInfo.directory.length() > 0 && !tiffInfo.directory.endsWith(sep))
            tiffInfo.directory += sep;
        File picFile = new File(tiffInfo.directory + tiffInfo.fileName);
        if (picFile == null || !picFile.exists() || picFile.isDirectory()
                || !validateFileInfo(picFile)) {
            return null;
        }// if file is not valid for making input stream
        return new FileInputStream(picFile);
    }// createInputStream

    /**
     * Verifies that the FileInfo about the TIFF to be loaded is accurate (that the TIFF is possible
     * to load) Adapted from static validateFileInfo method of ImageJ from ij.io.FileOpener.
     *
     * @param toCheck The file that needs to be validated
     * @return true if the FileInfo about the TIFF to be loaded is valid; otherwise false
     */
    private boolean validateFileInfo(File toCheck) {
        long offset = tiffInfo.getOffset();
        long length = 0;
        if (tiffInfo.width <= 0 || tiffInfo.height <= 0) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                    "Dimensions of TIFF file illegal. width =", tiffInfo.width, "height =",
                    tiffInfo.height);
            return false;
        }// if
        if (offset >= 0 && offset < 1000L)
            return true;
        if (offset < 0L) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Offset cannot be negative! offset =",
                    offset);
            return false;
        }// if
        if (tiffInfo.fileType == FileInfo.BITMAP
                || tiffInfo.compression != FileInfo.COMPRESSION_NONE)
            return true;
        length = toCheck.length();
        long size = tiffInfo.width * tiffInfo.height * tiffInfo.getBytesPerPixel();
        size = tiffInfo.nImages > 1 ? size : size / 4;
        if (tiffInfo.height == 1)
            size = 0; // allows plugins to read info of unknown length at end of file
        if (offset + size > length) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "(offset =", offset,
                    ") + (image size =", size, ") > (file length =", length, ").");
            return false;
        }// if
        return true;
    }// validateFileInfo

}// TiffReader class