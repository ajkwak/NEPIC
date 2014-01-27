package nepic.gui;

import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nepic.TitledActionListener;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_Alpha_v0-9-2013-01-16
 * @version AutoCBFinder_Alpha_v0-9-2013-01-16-newGui
 */
public class ButtonPanel extends JPanel {
    private static final long serialVersionUID = 1L; // default

    private JButton[] buttons;
    private JButton prevB, nextB;
    private JTextField pageTF;
    private int pgNum;

    private final int spacer = 5;
    private final int changePgComponentHeight = 25;

    private final int width;
    private final int minHeight;

    /**
     * Constructs such that the height of the panel is the minHeight of the panel
     * 
     * @param prevPgHandler
     * @param nxtPgHandler
     * @param functHandlers
     */
    protected ButtonPanel(
            ActionListener prevPgHandler,
            ActionListener nxtPgHandler,
            TitledActionListener[] functHandlers) {
        setLayout(null);
        setBorder(UtilityMethods.PANEL_BORDERS);
        setVisible(true);

        final int functButtonWidth = 170;
        final int functButtonHeight = 40;

        final int numFunctButtons = functHandlers.length;

        width = 2 * spacer + functButtonWidth;
        minHeight = 2 * spacer + numFunctButtons * (functButtonHeight + spacer)
                + changePgComponentHeight;

        buttons = new JButton[numFunctButtons];
        int yPos = spacer;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = UtilityMethods.makeButton(functHandlers[i].getText(),
                    functButtonWidth, functButtonHeight, spacer, yPos, false, functHandlers[i],
                    this);
            yPos += functButtonHeight + spacer;
        }// for all functional buttons: give associated handler

        final int changePgButtonWidth = 50;
        final int changePgTFWidth = 60;
        int xPos = spacer;

        prevB = UtilityMethods.makeButton("-", changePgButtonWidth, changePgComponentHeight, xPos,
                yPos, false, prevPgHandler, this);
        xPos += changePgButtonWidth + spacer;

        pageTF = UtilityMethods.makeTextField("N/A", changePgTFWidth, xPos, yPos, false, this);
        pageTF.setHorizontalAlignment(JTextField.RIGHT);
        pageTF.setEditable(false);
        xPos += changePgTFWidth + spacer;

        nextB = UtilityMethods.makeButton("+", changePgButtonWidth, changePgComponentHeight, xPos,
                yPos, false, nxtPgHandler, this);
        xPos += changePgButtonWidth + spacer;

        setSize(width, minHeight);
    }// DiagnosticPanel

    /**
     * 
     * @return the actual height (in other words, will be the minHeight if the height parameter is
     *         smaller than the minHeight
     */
    public int setHeight(int height) {
        if (height < minHeight) {
            height = minHeight;
        }

        final int yLoc = height - spacer - changePgComponentHeight;

        prevB.setLocation(prevB.getX(), yLoc);
        pageTF.setLocation(pageTF.getX(), yLoc);
        nextB.setLocation(nextB.getX(), yLoc);
        setSize(width, height);
        repaint();

        return height;
    }

    public void setPgNum(int pgNum, int totalNumPgs) {
        this.pgNum = pgNum;
        pageTF.setText((pgNum + 1) + "/" + totalNumPgs);// display page number starting at 1 instead
                                                        // of zero
    }

    public int getPgNum() {
        return pgNum;
    }

    protected void enableStartDiag(boolean toEnable) {
        for (int i = 0; i < buttons.length; i++)
            buttons[i].setEnabled(toEnable);
        prevB.setEnabled(toEnable);
        nextB.setEnabled(toEnable);
    }// enableStartDiag

}
