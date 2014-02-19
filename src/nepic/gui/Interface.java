package nepic.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;

import nepic.Preferences;
import nepic.TitledActionListener;
import nepic.Nepic;
import nepic.data.DataSet;
import nepic.io.Files;
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
    private JCheckBoxMenuItem equalizeHistogramMI;

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
            ActionListener imgContrastHandler,
            ActionListener pgDecrementor,
            ActionListener pgIncrementor,
            MouseListener clickHandler,
            MouseMotionListener dragHandler,
            TitledActionListener... functionButtonHandlers) {

        // General
        super(Nepic.getName() + ' ' + Nepic.getMainVersion());
        setVisible(true);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(exitHandler);
        addComponentListener(new ResizeHandler());

        // Main Menu
        JMenuBar myMainMenu = makeMainMenu(fileChooser, saveDataHandler, imgContrastHandler);
        setJMenuBar(myMainMenu);

        // Image Display
        // img = new AnnotatableImage();
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
        Preferences prefs = Nepic.getPrefs();
        setLocation(prefs.getWindowX(), prefs.getWindowY());
        setMySize(prefs.getWindowWidth(), prefs.getWindowHeight());
    }// Interface

    // **************************************************
    // General Methods
    // **************************************************

    public void close() {
        saveGuiIniConstants();
        if (Nepic.getEventLogger().isErrorRecorded()) {
            respondToError("Errors have been detected and logged during this session.");
        }
        dispose();
        Nepic.exit();
    }

    private void enableStartAnal(boolean allow) {// allows user to start analysis if true (by
        btnPanel.enableStartDiag(allow);
    }// enableStartAnal

    private void saveGuiIniConstants() {
        Preferences prefs = Nepic.getPrefs();
        prefs.setWindowX(getX());
        prefs.setWindowY(getY());
        prefs.setWindowWidth(getWidth());
        prefs.setWindowHeight(getHeight());
        prefs.setHistogramEqualizationDesired(shouldEqualizeHistogram());
    }

    // Look @ component resize() method with same args? override?
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
        img = new AnnotatableImage(theImage);
        repaint();
    }// setPage

    public boolean userAgrees(String questionTitle, String question) {
        Verify.argument(question != null && !question.isEmpty(),
                "Question must be a non-empty string.");
        if (questionTitle == null || questionTitle.isEmpty()) {
            questionTitle = "NEPIC: Question";
        }

        int response = JOptionPane.showConfirmDialog(this, question, Nepic.getName() + ": "
                + questionTitle, JOptionPane.YES_NO_OPTION);

        return response == JOptionPane.YES_OPTION;
    }

    // **************************************************
    // Main Menu
    // **************************************************

    private JMenuBar makeMainMenu(ActionListener fileChooser, ActionListener saveDataHandler,
            ActionListener imageContrastHandler) {
        JMenuBar toReturn = new JMenuBar();

        // "File" Menu.
        JMenu menu = new JMenu("File");
        toReturn.add(menu);

        chooseFileMI = new JMenuItem("Choose File To Analyze");
        chooseFileMI.addActionListener(fileChooser);
        menu.add(chooseFileMI);

        saveDataMI = new JMenuItem("Save Data");
        saveDataMI.addActionListener(saveDataHandler);
        saveDataMI.setEnabled(false);
        menu.add(saveDataMI);

        // "Image" menu.
        menu = new JMenu("Image");
        toReturn.add(menu);
        equalizeHistogramMI = new JCheckBoxMenuItem("Enhance Contrast");
        equalizeHistogramMI.setState(Nepic.getPrefs().isHistogramEqualizationDesired());
        equalizeHistogramMI.addActionListener(imageContrastHandler);
        menu.add(equalizeHistogramMI);
        // TODO

        // "Help" menu.
        menu = new JMenu("Help");
        toReturn.add(menu);

        JMenuItem otherMI = new JMenuItem("About " + Nepic.getName());
        otherMI.addActionListener(new WormAnalInfoHandler());
        menu.add(otherMI);

        otherMI = new JMenuItem("Open User Manual");
        otherMI.addActionListener(new OpenUserManualHandler());
        menu.add(otherMI);

        toReturn.setVisible(true);
        return toReturn;
    }// Toolbar

    public boolean shouldEqualizeHistogram() {
        return equalizeHistogramMI.getState();
    }

    public File selectTiffFile() {
        Preferences prefs = Nepic.getPrefs();
        String whereToLoadImg = prefs.getImageLoadLocation();
        JFileChooser chooser = new JFileChooser(whereToLoadImg);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(NepicFileFilter.TIF_ONLY);
        int folderSelected = chooser.showDialog(this, "Select Image");

        if (folderSelected == JFileChooser.APPROVE_OPTION) {
            enableStartAnal(true);
            File toReturn = chooser.getSelectedFile();
            String directory = Files.getDir(toReturn.getAbsolutePath());
            prefs.setImageLoadLocation(directory);

            Nepic.log(EventType.INFO, EventLogger.LOG_ONLY, "File selected:", toReturn.getName());
            return toReturn;
        }// if user selects a file
        return null;
    }// selectFile

    public void enableSaveData(boolean enable) {
        saveDataMI.setEnabled(enable);
    }

    public File selectCsvSaveLocation() {
        Preferences prefs = Nepic.getPrefs();
        String whereToSave = prefs.getDataSaveLocation();
        JFileChooser chooser = new JFileChooser(whereToSave);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(NepicFileFilter.CSV_ONLY);
        int folderSelected = chooser.showSaveDialog(this);

        if (folderSelected == JFileChooser.APPROVE_OPTION) {
            String classpath = chooser.getSelectedFile().getAbsolutePath();
            String directory = Files.getDir(classpath);
            prefs.setDataSaveLocation(directory);
            String fileExt = Files.getFileExtension(classpath);
            if (fileExt == null || !fileExt.equals("csv")) {
                classpath = classpath + ".csv";
            }
            File selectedFile = new File(classpath);
            return selectedFile;
        }// if user selects a file
        return null;
    }

    public void openJPopupMenu(Component invoker, int x, int y, TitledActionListener... handlers) {
        JPopupMenu popup = new JPopupMenu();
        for (TitledActionListener handler : handlers) {
            JMenuItem menuItem = new JMenuItem(handler.getText());
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

    public void draw(int categoryId, DataSet first, DataSet... rest) {
        img.annotate(categoryId, first, rest);
        repaint();
    }// drawPixels

    public void recolor(int categoryId, int color) {
        img.recolorAnnotation(categoryId, color);
        repaint();
    }// drawPixels

    public void erase(int categoryId) {
        img.eraseAnnotation(categoryId);
        repaint();
    }

    public void restoreImg() {
        img.clear();
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
    public void respondToInfo(String message) {
        if (message != null) {
            displayCurrentAction(message);
        }
    }

    @Override
    public void respondToWarning(String message) {
        if (message != null) {
            displayCurrentAction("WARNING: " + message);
        }
    }

    /**
     * @version AutoCBFinder_Alpha_v0-9-2013-02-10
     */
    @Override
    public void respondToError(String message) {
        if (message != null) {
            String appName = Nepic.getName();
            String toDisplay = (message.isEmpty() ? appName
                    + " has encountered an error in its performance." : message);
            if (Nepic.getEventLogger().canSaveLog()) {
                toDisplay += "\r\n\r\nWhen you close " + appName + ", please send \r\n'"
                        + Nepic.getEventLogger().getLogFileName()
                        + "' (which is located in the same directory as this \r\n"
                        + "application's executable file) to " + appName + " developers at "
                        + Nepic.getDeveloperContactInfo();
            }
            JOptionPane.showMessageDialog(this, toDisplay, appName + ": Error Detected",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void respondToFatalError(String message) {
        // Display fatal error message
        String appName = Nepic.getName();
        String toDisplay = appName + " has encountered a fatal error and needs to close";
        if (message != null) {
            toDisplay += ":\r\n" + message;
        }
        if (Nepic.getEventLogger().canSaveLog()) {
            toDisplay += "\r\n\r\nPlease send '" + Nepic.getEventLogger().getLogFileName()
                    + "' (which is located in the same\r\n"
                    + "directory as this application's executable file) to " + appName
                    + " developers at " + Nepic.getDeveloperContactInfo();
        }

        JOptionPane.showMessageDialog(this, toDisplay, appName + ": Fatal Error Detected",
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
                .append(Nepic.getName())
                .append("\r\n")
                .append("Version:  ")
                .append(Nepic.getFullVersion())
                .append("\r\n")
                .append("Developer Contact Info:  ")
                .append(Nepic.getDeveloperContactInfo());

        String releaseDate = Nepic.getReleaseDate();
        if (releaseDate != null) {
            appInfoBuilder.append("\r\n").append("Released:  ").append(releaseDate);
        } else {
            appInfoBuilder.append("\r\n").append("Status: IN DEVELOPMENT (Not Released)");
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
            String userManualLoc = "NepicUserManual.pdf"; // TODO: don't hard code
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