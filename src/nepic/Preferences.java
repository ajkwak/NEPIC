package nepic;

import java.util.Properties;

/**
 * Class containing user-specified preferences that NEPIC should persist across initializations
 * (likely stored in some form of INI file).
 *
 * @author AJ Parmidge
 */
public class Preferences {
    private boolean verboseEventLogged = false;
    private String imageLoadLocation = null;
    private String dataSaveLocation = null;
    private int windowX = 0;
    private int windowY = 0;
    private int windowWidth = 650;
    private int windowHeight = 475;
    private boolean histogramEqualizationDesired = false;

    /**
     * Constructs a {@link Preferences} object with default values.
     */
    public Preferences() {
        // Default constructor.
    }

    /**
     * Constructs a {@link Preferences} object with values loaded from the given {@link Properties}
     * file.
     *
     * @param props the properties to load from
     * @return the preferences with values corresponding to those in the given properties file
     */
    public static Preferences createFromProperties(Properties props) {
        Preferences prefs = new Preferences();
        prefs.setVerboseEventLogged(
                asBoolean(props.getProperty("VERBOSE_EVENT_LOGGED"), prefs.isVerboseEventLogged()));
        prefs.setImageLoadLocation(
                asString(props.getProperty("IMAGE_LOAD_LOCATION"), prefs.getImageLoadLocation()));
        prefs.setDataSaveLocation(
                asString(props.getProperty("DATA_SAVE_LOCATION"), prefs.getDataSaveLocation()));
        prefs.setWindowX(
                asInteger(props.getProperty("WINDOW_X"), prefs.getWindowX()));
        prefs.setWindowY(
                asInteger(props.getProperty("WINDOW_Y"), prefs.getWindowY()));
        prefs.setWindowWidth(
                asInteger(props.getProperty("WINDOW_WIDTH"), prefs.getWindowWidth()));
        prefs.setWindowHeight(
                asInteger(props.getProperty("WINDOW_HEIGHT"), prefs.getWindowHeight()));
        prefs.setHistogramEqualizationDesired(
                asBoolean(props.getProperty("HISTOGRAM_EQUALIZATION_DESIRED"),
                        prefs.isHistogramEqualizationDesired()));
        return prefs;
    }

    /**
     * Converts these {@link Preferences} to a format that can be easily saved.
     *
     * @return the "properties" view of these preferences, which can be easily saved in a
     *         '*.properties' file
     */
    public Properties toProperties() {
        Properties props = new Properties();
        props.setProperty("VERBOSE_EVENT_LOGGED", String.valueOf(verboseEventLogged));
        props.setProperty("IMAGE_LOAD_LOCATION", String.valueOf(imageLoadLocation));
        props.setProperty("DATA_SAVE_LOCATION", String.valueOf(dataSaveLocation));
        props.setProperty("WINDOW_X", String.valueOf(windowX));
        props.setProperty("WINDOW_Y", String.valueOf(windowY));
        props.setProperty("WINDOW_WIDTH", String.valueOf(windowWidth));
        props.setProperty("WINDOW_HEIGHT", String.valueOf(windowHeight));
        props.setProperty("HISTOGRAM_EQUALIZATION_DESIRED",
                String.valueOf(histogramEqualizationDesired));
        return props;
    }

    /**
     * Returns whether or not {@link nepic.logging.EventType#VERBOSE} messages should be logged.
     */
    public boolean isVerboseEventLogged() {
        return verboseEventLogged;
    }

    /**
     * Sets whether or not {@link nepic.logging.EventType#VERBOSE} messages should be logged.
     */
    public Preferences setVerboseEventLogged(boolean verboseEventLogged) {
        this.verboseEventLogged = verboseEventLogged;
        return this;
    }

    /**
     * Returns the string representation of the most recent folder from which images were loaded, or
     * {@code null} if images have never been loaded by NEPIC.
     */
    public String getImageLoadLocation() {
        return imageLoadLocation;
    }

    /**
     * Sets the string representation of the most recent folder from which images were loaded.
     */
    public Preferences setImageLoadLocation(String imageLoadLocation) {
        this.imageLoadLocation = imageLoadLocation;
        return this;
    }

    /**
     * Returns the string representation of the most recent folder to which data was saved by NEPIC,
     * or {@code null} if data has never been saved by NEPIC.
     */
    public String getDataSaveLocation() {
        return dataSaveLocation;
    }

    /**
     * Sets the string representation of the most recent folder to which data was saved by NEPIC.
     */
    public Preferences setDataSaveLocation(String dataSaveLocation) {
        this.dataSaveLocation = dataSaveLocation;
        return this;
    }

    /**
     * Gets the {@code x} coordinate at which the top left corner of the NEPIC window should appear
     * upon initialization.
     */
    public int getWindowX() {
        return windowX;
    }

    /**
     * Sets the {@code x} coordinate at which the top left corner of the NEPIC window should appear
     * upon initialization.
     */
    public Preferences setWindowX(int windowX) {
        this.windowX = windowX;
        return this;
    }

    /**
     * Gets the {@code y} coordinate at which the top left corner of the NEPIC window should appear
     * upon initialization.
     */
    public int getWindowY() {
        return windowY;
    }

    /**
     * Sets the {@code y} coordinate at which the top left corner of the NEPIC window should appear
     * upon initialization.
     */
    public Preferences setWindowY(int windowY) {
        this.windowY = windowY;
        return this;
    }

    /**
     * Gets the width that NEPIC's window should have upon initialization.
     */
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Sets the width that NEPIC's window should have upon initialization.
     */
    public Preferences setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
        return this;
    }

    /**
     * Gets the height that NEPIC's window should have upon initialization.
     */
    public int getWindowHeight() {
        return windowHeight;
    }

    /**
     * Sets the height that NEPIC's window should have upon initialization.
     */
    public Preferences setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
        return this;
    }

    /**
     * Determines whether NEPIC (upon initialization) equalizes the histograms of image pages
     * displayed to the user.
     */
    public boolean isHistogramEqualizationDesired() {
        return histogramEqualizationDesired;
    }

    /**
     * Sets whether NEPIC (upon initialization) equalizes the histograms of image pages displayed to
     * the user.
     */
    public Preferences setHistogramEqualizationDesired(boolean histogramEqualizationDesired) {
        this.histogramEqualizationDesired = histogramEqualizationDesired;
        return this;
    }

    // For use by createFromProperties()
    private static boolean asBoolean(String string, boolean defaultVal) {
        if (string != null) {
            string = string.trim().toLowerCase();
            if (string.equals("true") || string.equals("1")) {
                return true;
            } else if (string.equals("false") || string.equals("0")) {
                return false;
            }
        }
        return defaultVal;
    }

    // For use by createFromProperties()
    private static int asInteger(String string, int defaultVal) {
        if (string != null) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException nfe) {
                return defaultVal;
            }
        }
        return defaultVal;
    }

    // For use by createFromProperties()
    // Note: empty string maps to null!
    private static String asString(String string, String defaultVal) {
        if (string != null) {
            string = string.trim();
            if (string.isEmpty() || string.toLowerCase().equals("null")) {
                return null;
            }
            return string;
        }
        return defaultVal;
    }
}
