package nepic;

import nepic.image.PageInfo;
import nepic.io.DataWriter;
import nepic.logging.EventLogger;
import nepic.logging.EventType;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger (Called SharedVariables until
 *        AutoCBFinder_Alpha_v0-9-2013-01-29)
 * @version AutoCBFinder_Alpha_v0-9-2013-026-18
 */
// Contains information about NEPIC and variables that are shared across all classes.
public class Nepic {
    public static final String APP_NAME = "NEPIC";

    public static final String VERSION = "v1.1";

    public static final String SUB_VERSION = "20141402";

    public static final String RELEASE_DATE = "14 February 2014";

    public static final String AUTHOR = "AJ Parmidge";

    public static final String AUTHOR_EMAIL = "aparmidge@mills.edu";

    public static final IniConstants INI_CONSTANTS = new IniConstants("nepic.properties");

    public static final DataWriter dWriter = new DataWriter(PageInfo.getCsvLabels());

    static final EventLogger eLogger = new EventLogger("nepicEvents");

    public static String getFullAppName() {
        return new StringBuilder(APP_NAME).append(" ").append(VERSION).toString();
    }

    public static final int MOUSE_ACTION_ID = 0; // TODO: remove, put somewhere else!!!

    // COLORS FOR ROIs, ETC
    public static final int MOUSE_ACTION_COLOR = 0x9999ff; // lavender
    public static final int BACKGROUND_CAND_COLOR = 0x99ff99; // light green
    public static final int BACKGROUND_COLOR = 0x00ff00; // bright green
    public static final int CELL_BODY_COLOR = 0xff0000; // red
    public static final int CELL_BODY_CAND_COLOR = 0xff9999; // salmon
    public static final int SELECTED_ROI_COLOR = 0xffff00; // yellow

    // STATIC UTILITY METHODS FOR LOGGING

    // TODO: what if eLogger not yet initialized
    public static void log(EventType eventType, String messageForUser, Object... furtherInfo) {
        eLogger.logEvent(eventType, messageForUser, furtherInfo);
    }

    public static void endLog() {
        eLogger.endLog();
        if (eLogger.errorsRecorded()) {
            log(EventType.ERROR, "Errors have been detected and logged during this session.");
        }
    }

    public static boolean canLog() {
        return eLogger.haveLoggingFile();
    }

    public static String getLogName() {
        return eLogger.getLogFileName();
    }

}
