package nepic.io;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger
 * @version AutoCBFinder_Alpha_v0-8_NewLogger
 */
public class Label {
    public final String label;

    public Label(String label) {
        Verify.notNull(label);
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
