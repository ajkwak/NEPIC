package nepic.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * 
 * @author AJ parmidge
 * @since ManualCBFinder_v1-6
 * @version AutoCBFinder_Alpha_v0-3
 */
public class UtilityMethods {
    public static final Border PANEL_BORDERS = BorderFactory
            .createEtchedBorder(EtchedBorder.LOWERED);// from:
                                                      // http://download.oracle.com/javase/tutorial/uiswing/components/border.html

    /**
     * constructs a generic button, adds the constructed button to parent object
     * 
     * @param theText text displayed on the constructed button
     * @param theWidth width of the constructed button
     * @param theHeight height of the constructed button
     * @param theX x-component of constructed button's location on the parent component
     * @param theY y-component of constructed button's location on the parent component
     * @param isEnabled whether the button is enabled when constructed
     * @param theHandler called to perform specified actions when button being constructed is
     *        clicked
     * @param theCP object (parent) to which constructed button is added
     * @return generic button constructed by method
     */
    public static JButton makeButton(String theText, int theWidth, int theHeight, int theX,
            int theY, boolean isEnabled, ActionListener theHandler, Container theCP) {
        JButton genericB = new JButton(theText);
        genericB.setSize(theWidth, theHeight);
        genericB.setLocation(theX, theY);
        genericB.setEnabled(isEnabled);
        genericB.setVisible(true);
        theCP.add(genericB);
        genericB.addActionListener(theHandler);
        return genericB;
    }// makeButton

    /**
     * constructs a generic label, adds constructed label to parent object
     * 
     * @param theText text displayed by label to be constructed
     * @param theWidth width of the label to be constructed
     * @param theX x-component of constructed label's location
     * @param theY x-component of constructed label's location
     * @param theCP object (parent) to which constructed label is added
     * @return generic label constructed by method
     */
    public static JLabel makeLabel(String theText, int theWidth, int theX, int theY,
            int textAlignment, Container theCP) {
        JLabel genericL = new JLabel(theText);
        genericL.setSize(theWidth, 20);
        genericL.setLocation(theX, theY);
        genericL.setHorizontalAlignment(textAlignment);
        genericL.setForeground(Color.BLACK);
        genericL.setVisible(true);
        theCP.add(genericL);
        return genericL;
    }// makeLabel

    /**
     * constructs generic text field, adds constructed text field to parent object
     * 
     * @param theW width of text field to be constructed
     * @param theX x-component of constructed scroll pane's location on parent
     * @param theY y-component of constructed scroll pane's location on parent
     * @param isEnabled whether the text field is enabled on construction
     * @param theCP object (parent) to which constructed text field is added
     * @return generic text field constructed by method
     */
    public static JTextField makeTextField(String theText, int theW, int theX, int theY,
            boolean isEditable, Container theCP) {
        JTextField theTF = new JTextField(theText);
        theTF.setSize(theW, 25);
        theTF.setLocation(theX, theY);
        theTF.setEditable(isEditable);
        theTF.setVisible(true);
        theCP.add(theTF);
        return theTF;
    }// makeTextField

    public static int[] findMaxMinPixels(Point startLoc, Point endLoc) {
        int[] pixels = new int[4];// order: startX, endX, startY, endY
        if (startLoc.x < endLoc.x) {
            pixels[0] = startLoc.x;
            pixels[1] = endLoc.x;
        } else {
            pixels[1] = startLoc.x;
            pixels[0] = endLoc.x;
        }// else
        if (startLoc.y < endLoc.y) {
            pixels[2] = startLoc.y;
            pixels[3] = endLoc.y;
        } else {
            pixels[3] = startLoc.y;
            pixels[2] = endLoc.y;
        }// else
        return pixels;
    }// findMaxMinPixels

}// UtilityMethods