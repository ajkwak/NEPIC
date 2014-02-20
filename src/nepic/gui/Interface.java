package nepic.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import nepic.Nepic;
import nepic.Preferences;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.logging.LoggerObserver;
import nepic.util.Verify;

// Key Bindings: http://stackoverflow.com/questions/19066859/adding-keylistener-or-key-binding-to-jbuttons-that-use-actionlistener

// JSplitPane. http://docs.oracle.com/javase/tutorial/uiswing/components/splitpane.html
// Doing layout correctly: http://docs.oracle.com/javase/tutorial/uiswing/layout/index.html
public class Interface extends JFrame implements LoggerObserver {
    /**
     * The title displayed at the top of NEPIC's primary window.
     */
    public static final String TITLE = Nepic.getName() + ' ' + Nepic.getMainVersion();

    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = 6991127457546610699L;
    private static final SimpleAttributeSet BOLD_WARNING_TEXT_STYLE = newTextStyle(0xffaa00, true);
    private static final SimpleAttributeSet WARNING_TEXT_STYLE = newTextStyle(0xffaa00, false);
    private static final SimpleAttributeSet BOLD_ERROR_TEXT_STYLE = newTextStyle(0xcc0000, true);
    private static final SimpleAttributeSet ERROR_TEXT_STYLE = newTextStyle(0xcc0000, false);
    private static final SimpleAttributeSet INFO_TEXT_STYLE = newTextStyle(0x000088, false);

    private JMenuItem chooseFileMenuItem;
    private JMenuItem saveDataMenuItem;
    private JCheckBoxMenuItem equalizeHistogramMenuItem;
    private JMenuItem aboutNepicMenuItem;
    private AnnotatableImageLabel imgLabel;
    private JButton defineBackgroundButton;
    private JButton findCellBodyButton;
    private JButton enlargeCellBodyButton;
    private JButton shrinkCellBodyButton;
    private JButton acceptCandidatesButton;
    private JButton prevPgButton;
    private JButton nextPgButton;
    private JTextField pgNumTextField;
    private JTextPane outputTextPane;

    private static SimpleAttributeSet newTextStyle(int rgb, boolean isEmphasized) {
        SimpleAttributeSet textStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(textStyle, new Color(rgb));
        StyleConstants.setBold(textStyle, isEmphasized);
        StyleConstants.setItalic(textStyle, isEmphasized);
        return textStyle;
    }

    public Interface() {
        super(TITLE);
        setJMenuBar(constructMenuBar());

        // Add components.
        JPanel analysisPanel = constructAnalysisPanel();
        JScrollPane outputScrollPane = constructOutputScrollPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                analysisPanel, outputScrollPane);
        splitPane.setResizeWeight(1.0);
        add(splitPane);

        // General methods.
        setMinimumSize(new Dimension(500, 450));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Load desired size and location from prefs.
        Preferences prefs = Nepic.getPrefs();
        setLocation(prefs.getWindowX(), prefs.getWindowY());
        setSize(prefs.getWindowWidth(), prefs.getWindowHeight());
    }

    public void close() {
        saveGuiIniConstants();
        if (Nepic.getEventLogger().isErrorRecorded()) {
            respondToError("Errors have been detected and logged during this session.");
        }
        dispose();
        Nepic.exit();
    }

    public JMenuItem getAboutNepicMenuItem() {
        return aboutNepicMenuItem;
    }

    public JButton getAcceptCandidatesButton() {
        return acceptCandidatesButton;
    }

    public JMenuItem getChooseFileMenuItem() {
        return chooseFileMenuItem;
    }

    public JButton getDefineBackgroundButton() {
        return defineBackgroundButton;
    }

    public JButton getEnlargeCellBodyButton() {
        return enlargeCellBodyButton;
    }

    public JMenuItem getEqualizeHistogramMenuItem() {
        return equalizeHistogramMenuItem;
    }

    public JButton getFindCellBodyButton() {
        return findCellBodyButton;
    }

    public AnnotatableImageLabel getImageLabel() {
        return imgLabel;
    }

    public JButton getNextPgButton() {
        return nextPgButton;
    }

    public JTextField getPageNumberTextField() {
        return pgNumTextField;
    }

    public JButton getPrevPgButton() {
        return prevPgButton;
    }

    public JMenuItem getSaveDataMenuItem() {
        return saveDataMenuItem;
    }

    public JButton getShrinkCellBodyButton() {
        return shrinkCellBodyButton;
    }

    /**
     * Returns whether or not the displayed image should have its histogram equalized
     * ("Enhanced Contrast" mode).
     */
    public boolean isHistogramEqualizationDesired() {
        return equalizeHistogramMenuItem.getState();
    }

    public File selectFile(String title, String location, FileFilter filter) {
        JFileChooser chooser = new JFileChooser(location);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
        if (chooser.showDialog(this, title) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    public boolean userAgrees(String questionTitle, String question) {
        Verify.nonEmpty(question, "question");
        if (questionTitle == null || questionTitle.isEmpty()) {
            questionTitle = "NEPIC: Question";
        }

        int response = JOptionPane.showConfirmDialog(this, question, Nepic.getName() + ": "
                + questionTitle, JOptionPane.YES_NO_OPTION);

        return response == JOptionPane.YES_OPTION;
    }

    private JMenuBar constructMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setVisible(true);

        // "File" Menu (Mnemonic = 'F').
        JMenu menu = menuBar.add(new JMenu("File"));
        menu.setMnemonic(KeyEvent.VK_F);
        chooseFileMenuItem = menu.add(new JMenuItem("Choose Image to Analyze"));
        saveDataMenuItem = menu.add(new JMenuItem("Save Data"));
        saveDataMenuItem.setAccelerator(KeyStroke
                .getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        // "Image" Menu (Mnemonic = 'I').
        menu = menuBar.add(new JMenu("Image"));
        menu.setMnemonic(KeyEvent.VK_I);
        equalizeHistogramMenuItem = new JCheckBoxMenuItem("Enhance Contrast");
        equalizeHistogramMenuItem.setState(Nepic.getPrefs().isHistogramEqualizationDesired());
        menu.add(equalizeHistogramMenuItem);

        // "Help" Menu (Mnemonic = 'H').
        menu = menuBar.add(new JMenu("Help"));
        menu.setMnemonic(KeyEvent.VK_H);
        aboutNepicMenuItem = new JMenuItem("About " + Nepic.getName());
        menu.add(aboutNepicMenuItem);

        return menuBar;
    }

    /*
     * GroupLayout explanations/examples at:
     * http://docs.oracle.com/javase/tutorial/uiswing/layout/group.html
     * http://docs.oracle.com/javase/tutorial/uiswing/layout/groupExample.html
     *
     * GroupLayout javadoc at:
     * http://docs.oracle.com/javase/7/docs/api/javax/swing/GroupLayout.html
     */
    private JPanel constructAnalysisPanel() {
        JPanel analysisPanel = new JPanel();
        GroupLayout layout = new GroupLayout(analysisPanel);
        analysisPanel.setLayout(layout);

        imgLabel = new AnnotatableImageLabel(Color.black);
        JScrollPane imgScrollPane = makeImageScrollPane(imgLabel);
        imgScrollPane.setPreferredSize(new Dimension(100, 100));
        defineBackgroundButton = makeFunctionButton("Define Background");
        findCellBodyButton = makeFunctionButton("Find Cell Body");
        enlargeCellBodyButton = makeFunctionButton("Enlarge Cell Body");
        shrinkCellBodyButton = makeFunctionButton("Shrink Cell Body");
        acceptCandidatesButton = makeFunctionButton("Accept Candidates");
        prevPgButton = new JButton("<");
        pgNumTextField = makePageNumberTextField("N/A");
        nextPgButton = new JButton(">");

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(imgScrollPane)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(defineBackgroundButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(findCellBodyButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(enlargeCellBodyButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(shrinkCellBodyButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(acceptCandidatesButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(prevPgButton)
                                .addComponent(pgNumTextField)
                                .addComponent(nextPgButton))));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(imgScrollPane)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(defineBackgroundButton)
                                .addComponent(findCellBodyButton)
                                .addComponent(enlargeCellBodyButton)
                                .addComponent(shrinkCellBodyButton)
                                .addComponent(acceptCandidatesButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 50, Short.MAX_VALUE) // Create gap so next el at bottom.
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(prevPgButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pgNumTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(nextPgButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))));

        return analysisPanel;
    }

    private JScrollPane makeImageScrollPane(AnnotatableImageLabel content) {
        JScrollPane scrollpane = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setPreferredSize(new Dimension(100, 100));
        scrollpane.setMinimumSize(new Dimension(50, 50));
        return scrollpane;
    }

    private JButton makeFunctionButton(String message){
        JButton button = new JButton(message);
        Dimension preferredSize = button.getPreferredSize();
        preferredSize.height = Math.max(50, preferredSize.height);
        button.setMinimumSize(preferredSize);
        button.setPreferredSize(preferredSize);
        return button;
    }

    private JTextField makePageNumberTextField(String message) {
        JTextField textField = new JTextField(message);
        Dimension preferredSize = textField.getPreferredSize();
        textField.setPreferredSize(
                new Dimension(Math.max(70, preferredSize.width), preferredSize.height));
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setEditable(false);
        return textField;
    }

    private JScrollPane constructOutputScrollPane() {
        outputTextPane = new JTextPane();
        outputTextPane.setEditable(false);

        JScrollPane ouptutScrollPane = new JScrollPane(outputTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ouptutScrollPane.setPreferredSize(new Dimension(300, 100));
        ouptutScrollPane.setMinimumSize(new Dimension(300, 0));
        return ouptutScrollPane;
    }

    private void saveGuiIniConstants() {
        Preferences prefs = Nepic.getPrefs();
        prefs.setWindowX(getX());
        prefs.setWindowY(getY());
        prefs.setWindowWidth(getWidth());
        prefs.setWindowHeight(getHeight());
        prefs.setHistogramEqualizationDesired(isHistogramEqualizationDesired());
    }

    public void clearOutput() {
        outputTextPane.setText("");
    }

    @Override
    public void respondToInfo(String message) {
        appendOutput(message, INFO_TEXT_STYLE);
    }

    @Override
    public void respondToWarning(String message) {
        appendOutput("WARNING", BOLD_WARNING_TEXT_STYLE, message, WARNING_TEXT_STYLE);
    }

    @Override
    public void respondToError(String message) {
        appendOutput("ERROR", BOLD_ERROR_TEXT_STYLE, message, ERROR_TEXT_STYLE);
    }

    @Override
    public void respondToFatalError(String message) {
        displayErrorPopup(new StringBuilder(Nepic.getName())
                .append(" has encountered a fatal error and needs to close")
                .append(message.isEmpty() ? ":\r\n" : ".")
                .toString());
    }

    private void displayErrorPopup(String message) {
        Verify.nonEmpty(message);

        StringBuilder msgBuilder = new StringBuilder(message);
        if (Nepic.getEventLogger().canSaveLog()) {
            msgBuilder.append("\r\n\r\nPlease send '")
                    .append(Nepic.getEventLogger().getLogFileName())
                    .append("' (which is located in the same\r\n")
                    .append("directory as this application's executable file) to ")
                    .append(Nepic.getName())
                    .append("'s developers at ")
                    .append(Nepic.getDeveloperContactInfo());
        }

        JOptionPane.showMessageDialog(this, msgBuilder.toString(),
                TITLE + ": Error Detected", JOptionPane.ERROR_MESSAGE);
    }

    private boolean appendOutput(String message, SimpleAttributeSet messageTextStyle) {
        return appendOutput("", null, message, messageTextStyle);
    }

    private boolean appendOutput(String prefix, SimpleAttributeSet prefixTextStyle,
            String message, SimpleAttributeSet messageTextStyle) {
        StyledDocument doc = outputTextPane.getStyledDocument();
        int docLength = doc.getLength();
        try {
            if (!prefix.isEmpty()) {
                prefix = prefix + ": ";
                doc.insertString(docLength, prefix, prefixTextStyle);
            }
            doc.insertString(docLength + prefix.length(), message + "\r\n", messageTextStyle);
            return true;
        } catch (BadLocationException e) {
            Nepic.log(EventType.ERROR, EventLogger.LOG_ONLY,
                    "Unable to append text to outputTextPane", EventLogger.formatException(e));
            return false;
        }
    }

    public void displayProgramInfo() {
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
}
