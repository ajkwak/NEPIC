package nepic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import nepic.logging.EventLogger;
import nepic.logging.EventType;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212 (Called Constants until AutoCBFinder_Alpha_v0-9-2013-01-27)
 * @version AutoCBFinder_Alpha_v0-9-2013-01-16_newGui
 */
// constants read from .ini file
public class IniConstants { // TODO: change when constants become constant!
    public final String fileName;

    public final Property<Boolean> LOG_VERBOSE = new Property<Boolean>("LOG_VERBOSE", true);
    public final Property<String> LOAD_IMG_LOC = new Property<String>("LOAD_IMG_LOC", null);
    public final Property<String> DATA_SAVE_LOC = new Property<String>("DATA_SAVE_LOC", null);
    public final Property<String> LOG_SAVE_LOC = new Property<String>("LOG_SAVE_LOC", null);
    public final Property<String> USER_MANUAL_LOC = new Property<String>("USER_MANUAL_LOC", null);
    public final Property<Integer> SCREEN_POS_X = new Property<Integer>("SCREEN_POS_X", 0);
    public final Property<Integer> SCREEN_POS_Y = new Property<Integer>("SCREEN_POS_Y", 0);
    public final Property<Integer> WINDOW_WIDTH = new Property<Integer>("WINDOW_WIDTH", 0);
    public final Property<Integer> WINDOW_HEIGHT = new Property<Integer>("WINDOW_HEIGHT", 0);

    private void propertiesToConstants(Properties props) {
        updateProperty(LOG_VERBOSE, asBoolean(getProperty(props, LOG_VERBOSE)));
        updateProperty(LOAD_IMG_LOC, getProperty(props, LOAD_IMG_LOC));
        updateProperty(DATA_SAVE_LOC, getProperty(props, DATA_SAVE_LOC));
        updateProperty(LOG_SAVE_LOC, getProperty(props, LOG_SAVE_LOC));
        updateProperty(USER_MANUAL_LOC, getProperty(props, USER_MANUAL_LOC));
        updateProperty(SCREEN_POS_X, asInteger(getProperty(props, SCREEN_POS_X)));
        updateProperty(SCREEN_POS_Y, asInteger(getProperty(props, SCREEN_POS_Y)));
        updateProperty(WINDOW_WIDTH, asInteger(getProperty(props, WINDOW_WIDTH)));
        updateProperty(WINDOW_HEIGHT, asInteger(getProperty(props, WINDOW_HEIGHT)));
    }

    private Properties constantsToProperties() {
        Properties props = new Properties();

        setProperty(props, LOG_VERBOSE);
        setProperty(props, LOAD_IMG_LOC);
        setProperty(props, DATA_SAVE_LOC);
        setProperty(props, LOG_SAVE_LOC);
        setProperty(props, USER_MANUAL_LOC);
        setProperty(props, SCREEN_POS_X);
        setProperty(props, SCREEN_POS_Y);
        setProperty(props, WINDOW_WIDTH);
        setProperty(props, WINDOW_HEIGHT);

        return props;
    }

    public IniConstants(String fileName) {
        this.fileName = fileName;
    }

    public boolean initialize() {
        return loadConstants();
    }

    private <E> void updateProperty(Property<E> prop, E newVal) {
        if (newVal != null) {
            prop.setValue(newVal);
        }
    }

    private String getProperty(Properties props, Property<?> prop) {
        String property = props.getProperty(prop.name);
        if (property.equals("null")) {
            return null;
        }
        return property;
    }

    private void setProperty(Properties props, Property<?> prop) {
        Object propVal = prop.getValue();
        props.setProperty(prop.name, (propVal == null ? "null" : propVal.toString()));
    }

    public boolean loadConstants() {
        Properties props = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(fileName);
            props.load(in);
            propertiesToConstants(props);
            in.close();
            return true;
        } catch (FileNotFoundException e) {
            Nepic.log(EventType.WARNING, "Could not load program constants.  Using default values",
                    EventLogger.formatException(e));
        } catch (IOException e) {
            Nepic.log(EventType.WARNING, EventLogger.LOG_ONLY, "Unable to load", fileName,
                    EventLogger.formatException(e));
        }
        return false;
    }

    public void saveConstants() {
        Properties constantProps = constantsToProperties();

        FileOutputStream out;
        try {
            out = new FileOutputStream("nepic.properties");
            constantProps.store(out, "Constants for NEPIC");
            out.close();
        } catch (FileNotFoundException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, EventLogger.formatException(e));
        } catch (IOException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY, EventLogger.formatException(e));
        }
    }

    public Integer asInteger(String val) {
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public Boolean asBoolean(String val) {
        if (val == null) {
            return null;
        }
        if (val.isEmpty() || val.equals("false")) {
            return false;
        }
        return true;
    }

}
