package nepic;

import nepic.image.PageInfo;
import nepic.io.DataWriter;
import nepic.io.IniLoader;
import nepic.logging.EventLogger;
import nepic.logging.EventType;

/**
 * Uninstantiable class containing critical constants and variables needed by the entire NEPIC
 * framework. The {@code main} method is also located here.
 *
 * @author AJ Parmidge
 */
public class Nepic {
    private static final String NAME = "NEPIC";
    private static final String VERSION = "v1.1";
    private static final String SUB_VERSION = "20141402";
    private static final String RELEASE_DATE = "14 February 2014";
    private static final String DEVELOPER_CONTACT_INFO = "aparmidge@mills.edu";
    private static Preferences prefs;
    private static EventLogger eventLogger;
    private static DataWriter dataWriter;

    // This class should never be instantiated.
    private Nepic() {
        throw new UnsupportedOperationException();
    }

    public static String getName() {
        return NAME;
    }

    public static String getMainVersion() {
        return VERSION;
    }

    public static String getFullVersion() {
        return new StringBuilder(VERSION.length() + SUB_VERSION.length() + 1)
                .append(VERSION).append('.').append(SUB_VERSION).toString();
    }

    public static String getReleaseDate() {
        return RELEASE_DATE;
    }

    public static String getDeveloperContactInfo() {
        return DEVELOPER_CONTACT_INFO;
    }

    public static Preferences getPrefs() {
        return prefs;
    }

    public static EventLogger getEventLogger() {
        return eventLogger;
    }

    public static DataWriter getDataWriter() {
        return dataWriter;
    }

    // TODO: what if eLogger not yet initialized
    public static void log(EventType eventType, String messageForUser, Object... furtherInfo) {
        eventLogger.logEvent(eventType, messageForUser, furtherInfo);
    }

    public static void exit() {
        IniLoader.save(prefs);
        eventLogger.endLog();
        System.exit(0);
    }

    public static void main(String args[]) {
        eventLogger = new EventLogger("nepicEvents");
        prefs = IniLoader.load();
        dataWriter = new DataWriter(PageInfo.getCsvLabels());
        new Tracker();
    }

    // TODO: REMOVE ALL BELOW!
    public static final int MOUSE_ACTION_ID = 0;

    // COLORS FOR ROIs, ETC
    public static final int MOUSE_ACTION_COLOR = 0x9999ff; // lavender
    public static final int BACKGROUND_CAND_COLOR = 0x99ff99; // light green
    public static final int BACKGROUND_COLOR = 0x00ff00; // bright green
    public static final int CELL_BODY_COLOR = 0xff0000; // red
    public static final int CELL_BODY_CAND_COLOR = 0xff9999; // salmon
    public static final int SELECTED_ROI_COLOR = 0xffff00; // yellow
}
