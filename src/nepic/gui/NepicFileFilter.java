package nepic.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import nepic.io.Files;

/**
 * Immutable collection of {@link FileFilter}s used by NEPIC.
 *
 * @author AJ Parmidge
 */
public class NepicFileFilter extends FileFilter {
    /**
     * A {@link FileFilter} that only accepts TIFF files.
     */
    public static final NepicFileFilter TIF_ONLY = new NepicFileFilter(FilterType.TIF_ONLY);
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
            return extension.equals("csv");
            case TIF_ONLY:
                return extension.equals("tif") || extension.equals("tiff");
            default:
                return false;
        }
    }

    @Override
    public String getDescription() {
        switch (filterType) {
            case TIF_ONLY:
                return "Tagged Image File Format (*.tif, *.tiff)";
            case CSV_ONLY:
                return "Comma-Separated Value File Format (*.csv)";
            default:
                return null;
        }
    }
}