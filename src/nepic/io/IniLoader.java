package nepic.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import nepic.Nepic;
import nepic.Preferences;
import nepic.logging.EventLogger;
import nepic.logging.EventType;

/**
 * Uninstantiable class that loads and saves NEPIC {@link Preferences} to an INI file.
 *
 * @author AJ Parmidge
 */
public class IniLoader {
    /**
     * The name of the ini file containing NEPIC's preferences information.
     */
    public static final String INI_FILE_NAME = "nepic.properties";

    // This class is cannot be instantiated.
    private IniLoader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads NEPIC's preferences from an INI file.
     *
     * @return NEPIC's preferences
     */
    public static Preferences load() {
        Preferences prefs = null;
        try{
            FileInputStream in = new FileInputStream(INI_FILE_NAME);
            Properties props = new Properties();
            try {
                props.load(in);
                prefs = Preferences.createFromProperties(props);
            } catch (IOException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to load", INI_FILE_NAME,
                        EventLogger.formatException(e));
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                            "Unable to close FileInputStream for loading from", INI_FILE_NAME,
                            EventLogger.formatException(e));
                }
            }
        } catch (FileNotFoundException e) {
            Nepic.log(EventType.WARNING, "Could not load program constants.  Using default values",
                    EventLogger.formatException(e));
        }

        return prefs == null ? new Preferences() : prefs; // NEVER return null!
    }

    /**
     * Saves the given {@link Preferences} to an INI file.
     *
     * @param prefs the prefences to save
     */
    public static void save(Preferences prefs) {
        Properties props = prefs.toProperties();

        // Make sure that the ini file exists, so we can write to it.
        File outputFile = new File(INI_FILE_NAME);
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to create non-existent",
                        INI_FILE_NAME, "file", EventLogger.formatException(e));
                return;
            }
        }

        // Save the preferences to the ini file.
        try {
            FileOutputStream out = new FileOutputStream(INI_FILE_NAME);
            try {
                props.store(out, "Constants for NEPIC");
            } catch (IOException e) {
                Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, EventLogger.formatException(e));
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                            "Unable to close FileOutputStream for saving", INI_FILE_NAME,
                            EventLogger.formatException(e));
                }
            }
        } catch (FileNotFoundException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, "Unable to save", INI_FILE_NAME,
                    EventLogger.formatException(e));
        }
    }
}
