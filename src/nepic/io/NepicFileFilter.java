package nepic.io;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * Immutable collection of {@link FileFilter}s used by NEPIC.
 *
 * @author AJ Parmidge
 */
public class NepicFileFilter extends FileFilter {
    /**
     * A {@link FileFilter} that only accepts TIFF files.
     */
    public static final NepicFileFilter TIFF_ONLY = new NepicFileFilter(FilterType.TIF_ONLY);
    /**
     * A {@link FileFilter} that only accepts CSV files.
     */
    public static final NepicFileFilter CSV_ONLY = new NepicFileFilter(FilterType.CSV_ONLY);

    private enum FilterType {
        TIF_ONLY,
        CSV_ONLY
    }

    private FilterType filterType;

    private NepicFileFilter(FilterType filterType) {
        this.filterType = filterType;
    }

    @Override
    public boolean accept(File theFile) {
        String extension = Files.getFileExtension(theFile.getName());
        switch (filterType) {
            case CSV_ONLY:
            return theFile.isDirectory() || extension.equals("csv");
            case TIF_ONLY:
            return theFile.isDirectory() || extension.equals("tif") || extension.equals("tiff");
            default:
                return false;
        }
    }

    @Override
    public String getDescription() {
        switch (filterType) {
            case CSV_ONLY:
                return "Comma-Separated Value File Format (*.csv)";
            case TIF_ONLY:
                return "Tagged Image File Format (*.tif, *.tiff)";
            default:
                return null;
        }
    }
}