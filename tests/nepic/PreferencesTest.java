package nepic;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import nepic.testing.util.StringsUtil;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * JUnit tests for {@link Preferences}.
 *
 * @author AJ Parmidge
 */
public class PreferencesTest {
    private Preferences prefs;

    @Before
    public void setUp() {
        prefs = new Preferences();
    }

    // PLEASE NOTE: If this test fails, it likely means that both the toProperties() and the
    // createFromProperties() methods must be modified for some new 'Preferences' variable.
    @Test
    public void toProperties() throws IllegalAccessException {
        Properties props = prefs.toProperties();
        Field[] preferencesFields = Preferences.class.getDeclaredFields();
        String[] expectedPropertyKeys = new String[preferencesFields.length];

        for (int i = 0; i < preferencesFields.length; i++) {
            expectedPropertyKeys[i] = StringsUtil.toUpperSnakeCase(preferencesFields[i].getName());
        }

        List<Object> unexpectedPropertyKeys = Lists.newArrayList(props.keySet());
        for (int i = 0; i < expectedPropertyKeys.length; i++) {
            String propertyKey = expectedPropertyKeys[i];
            String propertyVal = props.getProperty(propertyKey);
            assertNotNull("Properties does not contain expected field " + propertyKey, propertyVal);
            unexpectedPropertyKeys.remove(propertyKey);
        }
        assertTrue("Properties contains unexpected fields: " + unexpectedPropertyKeys,
                unexpectedPropertyKeys.isEmpty());
    }

    // NOTE: for this test to work, toProperties() MUST be functioning correctly!
    // This test must be updated for every new preferences variable.
    @Test
    public void createFromProperties() {
        // Set all prefrences variables to non-default values.
        prefs.setVerboseEventLogged(true)
                .setImageLoadLocation("LoadLocation")
                .setDataSaveLocation("DataSaveLocation")
                .setWindowX(5840)
                .setWindowY(89)
                .setWindowHeight(40)
                .setWindowWidth(484)
                .setHistogramEqualizationDesired(true);
        Preferences createdPrefs = Preferences.createFromProperties(prefs.toProperties());
        assertEquals(prefs.isVerboseEventLogged(), createdPrefs.isVerboseEventLogged());
        assertEquals(prefs.getImageLoadLocation(), createdPrefs.getImageLoadLocation());
        assertEquals(prefs.getDataSaveLocation(), createdPrefs.getDataSaveLocation());
        assertEquals(prefs.getWindowX(), createdPrefs.getWindowX());
        assertEquals(prefs.getWindowY(), createdPrefs.getWindowY());
        assertEquals(prefs.getWindowWidth(), createdPrefs.getWindowWidth());
        assertEquals(prefs.getWindowHeight(), createdPrefs.getWindowHeight());
        assertEquals(prefs.isHistogramEqualizationDesired(),
                createdPrefs.isHistogramEqualizationDesired());
    }
}
