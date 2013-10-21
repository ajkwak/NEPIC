package nepic.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;

import nepic.ButtonHandler;
import nepic.IniConstants;
import nepic.Nepic;
import nepic.data.DataSet;
import nepic.io.TiffOpener;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.logging.LoggerObserver;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 * @since ManualCBFinder_v1-6
 * @version AutoCBFinder_Alpha_v0-9-2013-02-10
 */
public class Interface extends JFrame implements LoggerObserver {
    private static final long serialVersionUID = 1L; // Default serialVersionIUD

    // Main menu
    private JMenuItem chooseFileMI, saveDataMI;

    // Image Display
    private AnnotatableImage img;
    private final JLabel imgL;
    private final JScrollPane imgSP;

    // Button Panel
    private final ButtonPanel btnPanel;

    // Output Text
    private final JTextArea outputTA;
    private final JScrollPane outputSP;

    // **************************************************
    // Constructor
    // **************************************************

    public Interface(
            int maxNumRois,
            WindowListener exitHandler,
            ActionListener fileChooser,
            ActionListener saveDataHandler,
            ActionListener pgDecrementor,
            ActionListener pgIncrementor,
            MouseListener clickHandler,
            MouseMotionListener dragHandler,
            ButtonHandler... functionButtonHandlers) {

        // General
        super(Nepic.getFullAppName());
        setVisible(true);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(exitHandler);
        addComponentListener(new ResizeHandler());

        // Main Menu
        JMenuBar myMainMenu = makeMainMenu(fileChooser, saveDataHandler);
        setJMenuBar(myMainMenu);

        // Image Display
        img = new AnnotatableImage(maxNumRois);
        imgL = new JLabel();
        imgSP = new JScrollPane(imgL, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        initializeImageDisplay(clickHandler, dragHandler);

        // Button Panel
        btnPanel = new ButtonPanel(pgDecrementor, pgIncrementor, functionButtonHandlers);
        add(btnPanel);

        // Output Text
        outputTA = new JTextArea();
        outputSP = new JScrollPane(outputTA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        initializeOutputTA();

        // From INI constants
        int windowLocX = Nepic.INI_CONSTANTS.SCREEN_POS_X.getValue();
        int windowLocY = Nepic.INI_CONSTANTS.SCREEN_POS_Y.getValue();
        setLocation(windowLocX, windowLocY);

        int windowWidth = Nepic.INI_CONSTANTS.WINDOW_WIDTH.getValue();
        int windowHeight = Nepic.INI_CONSTANTS.WINDOW_HEIGHT.getValue();
        setMySize(windowWidth, windowHeight);

    }// Interface

    // **************************************************
    // General Methods
    // **************************************************

    public void close() {
        Nepic.endLog();
        saveGuiIniConstants();
        dispose();
        System.exit(0);
    }

    private void enableStartAnal(boolean allow) {// allows user to start analysis if true (by
        btnPanel.enableStartDiag(allow);
    }// enableStartAnal

    private void saveGuiIniConstants() {
        IniConstants iniConsts = Nepic.INI_CONSTANTS;
        iniConsts.SCREEN_POS_X.setValue(this.getX());
        iniConsts.SCREEN_POS_Y.setValue(this.getY());
        iniConsts.WINDOW_WIDTH.setValue(this.getWidth());
        iniConsts.WINDOW_HEIGHT.setValue(this.getHeight());
        iniConsts.saveConstants();
    }

    private void setMySize(int width, int height) {
        final int widthBuffer = 5; // Make up for borders
        final int heightBuffer = 50; // Make up for menu bar, borders
        final int outputTAHeight = 100;
        final int minImgDimension = 30;

        // Resize button panel
        int buttonPanelHeight = height - heightBuffer - outputTAHeight;
        if (buttonPanelHeight < minImgDimension) {
            buttonPanelHeight = minImgDimension;
        }
        buttonPanelHeight = btnPanel.setHeight(buttonPanelHeight);
        Verify.argument(buttonPanelHeight == btnPanel.getHeight());
        int buttonPanelWidth = btnPanel.getWidth();

        // Resize Image Display
        int imgWidth = width - buttonPanelWidth - widthBuffer;
        if (imgWidth < minImgDimension) {
            imgWidth = minImgDimension;
        }
        imgSP.setSize(imgWidth, buttonPanelHeight);

        // Re-locate button panel based upon image display size
        btnPanel.setLocation(imgWidth, 0);

        // Resize / Relocate outputTA based upon image display and button panel.
        int innerWidth = imgWidth + buttonPanelWidth;
        outputSP.setSize(innerWidth, outputTAHeight);
        outputSP.setLocation(0, buttonPanelHeight);
        int innerHeight = buttonPanelHeight + outputTAHeight;

        // Resize window
        setSize(innerWidth + widthBuffer, innerHeight + heightBuffer);

        // Repaint everyone
        imgSP.getViewport().revalidate(); // Needed to repaint the resized scrollpane
        outputSP.getViewport().revalidate(); // Needed to repaint the resized scrollpane
        repaint();
    }

    public void setPage(int thePgNum, int totNumPgs, BufferedImage theImage) {
        Verify.notNull(theImage, "Cannot set page to null image");

        // update and display page number
        btnPanel.setPgNum(thePgNum, totNumPgs);

        imgL.setIcon(new ImageIcon(theImage));
        img.setImage(theImage);
        repaint();
    }// setPage

    public boolean userAgrees(String questionTitle, String question) {
        Verify.argument(question != null && !question.isEmpty(),
                "Question must be a non-empty string.");
        if (questionTitle == null || questionTitle.isEmpty()) {
            questionTitle = "NEPIC: Question";
        }

        int response = JOptionPane.showConfirmDialog(this, question, Nepic.APP_NAME + ": "
                + questionTitle, JOptionPane.YES_NO_OPTION);

        return response == JOptionPane.YES_OPTION;
    }

    // **************************************************
    // Main Menu
    // **************************************************

    private JMenuBar makeMainMenu(ActionListener fileChooser, ActionListener saveDataHandler) {
        JMenuBar toReturn = new JMenuBar();

        JMenu menu = new JMenu("File");
        toReturn.add(menu);

        chooseFileMI = new JMenuItem("Choose File To Analyze");
        chooseFileMI.addActionListener(fileChooser);
        menu.add(chooseFileMI);

        saveDataMI = new JMenuItem("Save Data");
        saveDataMI.addActionListener(saveDataHandler);
        saveDataMI.setEnabled(false);
        menu.add(saveDataMI);

        // Build "Help" menu in the menu bar.
        menu = new JMenu("Help");
        toReturn.add(menu);

        JMenuItem otherMI = new JMenuItem("About " + Nepic.APP_NAME);
        otherMI.addActionListener(new WormAnalInfoHandler());
        menu.add(otherMI);

        otherMI = new JMenuItem("Open User Manual");
        otherMI.addActionListener(new OpenUserManualHandler());
        menu.add(otherMI);

        toReturn.setVisible(true);
        return toReturn;
    }// Toolbar

    public File selectTiffFile() {
        String whereToLoadImg = Nepic.INI_CONSTANTS.LOAD_IMG_LOC.getValue();
        JFileChooser chooser = new JFileChooser(whereToLoadImg);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new NepicFileFilter(NepicFileFilter.TIFS_ONLY));
        int folderSelected = chooser.showDialog(this, "Select Image");

        if (folderSelected == JFileChooser.APPROVE_OPTION) {
            enableStartAnal(true);
            File toReturn = chooser.getSelectedFile();
            String directory = TiffOpener.getDir(toReturn.getAbsolutePath());
            Nepic.INI_CONSTANTS.LOAD_IMG_LOC.setValue(directory);

            Nepic.log(EventType.INFO, EventLogger.LOG_ONLY, "File selected:", toReturn.getName());
            return toReturn;
        }// if user selects a file
        return null;
    }// selectFile

    public void enableSaveData(boolean enable) {
        saveDataMI.setEnabled(enable);
    }

    public File selectCsvSaveLocation() {
        String whereToSave = Nepic.INI_CONSTANTS.DATA_SAVE_LOC.getValue();
        JFileChooser chooser = new JFileChooser(whereToSave);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new NepicFileFilter(NepicFileFilter.CSV_ONLY));
        int folderSelected = chooser.showSaveDialog(this);

        if (folderSelected == JFileChooser.APPROVE_OPTION) {
            String classpath = chooser.getSelectedFile().getAbsolutePath();
            String directory = TiffOpener.getDir(classpath);
            Nepic.INI_CONSTANTS.DATA_SAVE_LOC.setValue(directory);
            String fileExt = EventLogger.getFileExtention(classpath);
            if (fileExt == null || !fileExt.equals("csv")) {
                classpath = classpath + ".csv";
            }
            File selectedFile = new File(classpath);
            return selectedFile;
        }// if user selects a file
        return null;
    }

    public void openJPopupMenu(Component invoker, int x, int y, ButtonHandler... handlers) {
        JPopupMenu popup = new JPopupMenu();
        for (ButtonHandler handler : handlers) {
            JMenuItem menuItem = new JMenuItem(handler.getButtonText());
            menuItem.addActionListener(handler);
            popup.add(menuItem);
        }
        popup.show(invoker, x, y);
    }

    // **************************************************
    // Image Display
    // **************************************************

    private void initializeImageDisplay(MouseListener clickHandler, MouseMotionListener dragHandler) {
        imgL.setOpaque(true);
        imgL.setBackground(Color.black);
        imgL.setVisible(true);
        imgL.setHorizontalAlignment(JLabel.LEFT);
        imgL.setVerticalAlignment(JLabel.TOP);
        imgL.addMouseListener(clickHandler);
        imgL.addMouseMotionListener(dragHandler);

        imgSP.setLocation(0, 0);
        imgSP.setVisible(true);
        add(imgSP);
    }

    public void redraw(int categoryId, DataSet... newVals) {
        img.redraw(categoryId, newVals);
        repaint();
    }// drawPixels

    public void recolor(int categoryId, int color) {
        img.recolor(categoryId, color);
        repaint();
    }// drawPixels

    public void erase(int categoryId) {
        img.erase(categoryId);
        repaint();
    }

    public void restoreImg() {
        img.eraseAll();
        repaint();
    }// restoreImg

    // **************************************************
    // Message Display Methods (output)
    // **************************************************

    private void initializeOutputTA() {
        outputTA.setVisible(true);
        outputTA.setEditable(false);
        outputSP.setVisible(true);
        add(outputSP);
    }

    public void displayCurrentAction(String theAction) {
        outputTA.append(theAction + "\n");
        outputTA.repaint();
    }// displayCurrentAction

    public void clearDisplayedActions() {
        outputTA.setText("");
    }

    // **************************************************
    // LoggerObserver Methods
    // **************************************************

    @Override
    public void verboseMessageLogged(String message) {
        if (message != null && (Nepic.INI_CONSTANTS.LOG_VERBOSE.getValue() || !Nepic.canLog())) {
            displayCurrentAction("DIAGNOSTIC: " + message);
        }
    }

    @Override
    public void infoMessageLogged(String message) {
        if (message != null) {
            displayCurrentAction(message);
        }
    }

    @Override
    public void warningMessageLogged(String message) {
        if (message != null) {
            displayCurrentAction("WARNING: " + message);
        }
    }

    /**
     * @version AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    @Override
    public void errorMessageLogged(String message) {
        if (message != null) {
            String toDisplay = (message.isEmpty() ? Nepic.APP_NAME
                    + " has encountered an error in its performance." : message);
            if (Nepic.canLog()) {
                toDisplay += "\r\n\r\nWhen you close " + Nepic.APP_NAME + ", please send \r\n'"
                        + Nepic.getLogName()
                        + "' (which is located in the same directory as this \r\n"
                        + "application's executable file) to " + Nepic.AUTHOR + " at "
                        + Nepic.AUTHOR_EMAIL;
            }
            JOptionPane.showMessageDialog(this, toDisplay, Nepic.APP_NAME + ": Error Detected",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void fatalErrorMessageLogged(String message) {
        // Display fatal error message
        String toDisplay = Nepic.APP_NAME + " has encountered a fatal error and needs to close";
        if (message != null) {
            toDisplay += ":\r\n" + message;
        }
        if (Nepic.canLog()) {
            toDisplay += "\r\n\r\nPlease send '" + Nepic.getLogName()
                    + "' (which is located in the same\r\n"
                    + "directory as this application's executable file) to " + Nepic.AUTHOR
                    + " at " + Nepic.AUTHOR_EMAIL;
        }

        JOptionPane.showMessageDialog(this, toDisplay, Nepic.APP_NAME + ": Fatal Error Detected",
                JOptionPane.ERROR_MESSAGE);

        // Quit application (data not stored)
        close();
    }

    // **************************************************
    // Interface-Specific Handlers
    // **************************************************

    /**
     *
     * @author AJ Parmidge
     * @version AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    private class WormAnalInfoHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            displayProgramInfo();
        }// actionPerformed
    }// ExitHandler

    /**
     *
     * @version AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    private void displayProgramInfo() {
        StringBuilder appInfoBuilder = new StringBuilder("App Name:  ")
                .append(Nepic.APP_NAME)
                .append("\r\n");

        if (Nepic.TEST_STAGE != Nepic.TestStage.Production) { // If not in production
            appInfoBuilder.append("Testing Stage:  ").append(Nepic.TEST_STAGE).append("\r\n");
        }

        appInfoBuilder
                .append("Version:  ")
                .append(Nepic.VERSION)
                .append("\r\n")
                .append("Author:  ")
                .append(Nepic.AUTHOR)
                .append("\r\n")
                .append("Contact Info:  ")
                .append(Nepic.AUTHOR_EMAIL);

        String releaseDate = Nepic.RELEASE_DATE;
        if (releaseDate != null) {
            appInfoBuilder.append("\r\n").append("Released:  ").append(releaseDate);
        }

        JOptionPane.showMessageDialog(this, appInfoBuilder.toString(), "About This Program",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     * @author AJ Parmidge
     * @version AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    private class OpenUserManualHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userManualLoc = Nepic.INI_CONSTANTS.USER_MANUAL_LOC.getValue();
            if (userManualLoc != null && !userManualLoc.isEmpty()) {
                File manualFile = new File(userManualLoc);
                if (manualFile.exists()) {
                    try {
                        Desktop.getDesktop().open(manualFile);
                    } catch (IOException er) {
                        Nepic.log(EventType.ERROR, "There was an error while trying to open "
                                + userManualLoc);
                    }
                } else {
                    Nepic.log(EventType.ERROR, "Unable to find " + manualFile.getAbsolutePath()
                            + ".  File does not exist.");
                }
            } else {
                Nepic.log(EventType.ERROR, "User manual location unknown.  Unable to open.",
                        "Nepic.INI_CONSTANTS.USER_MANUAL_LOC =", userManualLoc);
            }
        }// actionPerformed
    }// ExitHandler

    public class ResizeHandler extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            if (btnPanel != null) {
                Dimension newSize = e.getComponent().getBounds().getSize();
                setMySize(newSize.width, newSize.height);
                repaint();
            }
        }

    }

}// Interface class