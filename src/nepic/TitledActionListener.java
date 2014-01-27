package nepic;

import java.awt.event.ActionListener;

import nepic.util.Verify;

/**
 * An {@link ActionListener} that contains a short description of what it does. This description is
 * meant to be displayed as the text of the button or other GUI component to which it listens.
 *
 * @author AJ Parmidge
 */
public abstract class TitledActionListener implements ActionListener {
    private final String text;

    /**
     * Creates a new {@link TitledActionListene}r with the given text.
     * 
     * @param text the short description of what this {@link ActionListener} does (to be displayed
     *        as the text on the GUI component to which this listener listens)
     */
    public TitledActionListener(String text) {
        Verify.nonEmpty(text, "text");
        this.text = text;
    }

    /**
     * Returns the
     *
     * @return
     */
    public String getText() {
        return text;
    }
}
