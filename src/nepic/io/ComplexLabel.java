package nepic.io;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-8_NewLogger
 * @version AutoCBFinder_Alpha_v0-8_NewLogger
 */
public class ComplexLabel extends Label {
    public final Label[] sublabels;

    public ComplexLabel(String label, Label[] sublabels) {
        super(label);
        Verify.notNull(sublabels);
        this.sublabels = sublabels;
    }
}
