package nepic;

import java.awt.event.ActionListener;

import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-7-093012
 * @version AutoCBFinder_Alpha_v0-7-093012
 */
public abstract class ButtonHandler implements ActionListener {
    private final String buttonText;

    public ButtonHandler(String buttonText) {
        Verify.notNull(buttonText);
        this.buttonText = buttonText;
    }

    public String getButtonText() {
        return buttonText;
    }
}
