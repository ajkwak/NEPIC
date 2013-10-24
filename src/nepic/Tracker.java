package nepic;

//TODO: Record if user accepts a CB where no edges were initially found (so can track for dimmer images)
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import nepic.gui.Graph;
import nepic.gui.HistogramViewPanel;
import nepic.gui.Interface;
import nepic.gui.ScannerGroupSizeVarierPanel;
import nepic.image.ImagePage;
import nepic.image.MultiPageImageInfo;
import nepic.image.PageInfo;
import nepic.io.DataWriter;
import nepic.io.TiffOpener;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.data.DataSet;
import nepic.data.GraphData;
import nepic.data.Histogram;
import nepic.data.UnorderedDataSet;
import nepic.roi.OneDimensionalScanner;
import nepic.roi.model.Line;
import nepic.roi.model.Polygon;
import nepic.util.Verify;

/**
 *
 * @author AJ Parmidge
 */
public class Tracker {
    private TiffOpener myOpener;
    private String analFileClassPath = null;

    private Interface myGUI;
    private Point clickLoc = null;
    private Point dragLoc = null;

    // For tracking
    private boolean unsavedDataOnCurrentImg = false;
    private MultiPageImageInfo pages;

    // Current page.img
    private ImagePage currPg = null;
    private PageInfo currPgInfo = null;
    private int currPgNum = -1; // Start with invalid number!

    public Tracker() {
        Nepic.INI_CONSTANTS.initialize();
        myGUI = new Interface(ImagePage.MAX_CAND_ID, new ExitHandler(), new ChooseFileHandler(),
                new SaveDataHandler(), new IncrementPageHandler(-1), new IncrementPageHandler(1),
                new ClickHandler(), new DragHandler(), new BkCharacterizer());
        Nepic.eLogger.registerObserver(myGUI);
        myOpener = new TiffOpener();
        Nepic.log(EventType.INFO, Nepic.APP_NAME + " successfully initialized.");
        myGUI.displayCurrentAction("Please select image to analyze.");
    }

    // *********************************************************************************************
    // File Selection
    // *********************************************************************************************

    private boolean selectFileToAnalyze() {
        File fileToAnal = myGUI.selectTiffFile();
        if (fileToAnal == null) {
            analFileClassPath = null;
            return false;
        }// if no file selected
        analFileClassPath = fileToAnal.getAbsolutePath();
        return true;
    }// selectFileToAnalyze

    // *********************************************************************************************
    // Load Image File
    // *********************************************************************************************

    private class ChooseFileHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectFileToAnalyze()) {
                loadAndDisplayTiff(analFileClassPath);
            }// if user selected file
        }// actionPerformed
    }// ChooseFileHandler

    private boolean loadAndDisplayTiff(String theClasspath) {
        if (myOpener.loadTiffInfo(theClasspath)) {
            int totNumPgs = myOpener.getNumPagesInTiff();
            updateImageBeingAnalyzed(totNumPgs);
            myGUI.setTitle(Nepic.getFullAppName() + " (" + TiffOpener.getName(analFileClassPath)
                    + ")");
            updateDisplayedPage(0); // open the first page of the image
            return true;
        }// if able to load info about TIFF

        Nepic.log(EventType.ERROR, "Unable to load image from the given classpath: "
                        + theClasspath);
        return false;
    }// loadPic

    public void updateImageBeingAnalyzed(int numPages) {
        if (unsavedDataOnCurrentImg) {
            logImageData();
        }
        unsavedDataOnCurrentImg = false;
        pages = new MultiPageImageInfo(numPages);
        currPgNum = 0;
    }

    // *********************************************************************************************
    // Display Image Page
    // *********************************************************************************************

    private void updateDisplayedPage(int pgNum) {
        ImagePage imageBeingAnalyzed = myOpener.openTiffPage(pgNum);
        updatePage(TiffOpener.getName(analFileClassPath), pgNum, imageBeingAnalyzed);
        myGUI.setPage(pgNum, pages.getNumPages(), currPg.displayImg());
        myGUI.clearDisplayedActions();
        Nepic.log(EventType.INFO, "Page " + (pgNum + 1) + " displayed.");
    }

    public void updatePage(String imgName, int pgNum, ImagePage page) {
        Verify.notNull(imgName, "Image name cannot be null.");
        Verify.argument(pgNum >= 0 && pgNum < pages.getNumPages());
        Verify.notNull(page, "ImagePage to update cannot be null.");

        // Make PageInfo for current page
        currPgNum = pgNum;
        currPg = page;
        currPgInfo = new PageInfo(imgName, pgNum, page);
    }

    // *********************************************************************************************
    // Find Background
    // *********************************************************************************************

    public class BkCharacterizer extends ButtonHandler {
        public BkCharacterizer() {
            super("Re-Define BK");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (clickLoc != null && dragLoc != null) {
                Polygon bkArea = new Polygon(new Point[] {
                        clickLoc,
                        new Point(dragLoc.x, clickLoc.y),
                        dragLoc,
                        new Point(clickLoc.x, dragLoc.y) });
                clickLoc = null;
                dragLoc = null;
                myGUI.erase(Nepic.MOUSE_ACTION_ID);
                currPgInfo.setCalibrationBkHist(currPg.makeHistogram(bkArea));
                myGUI.displayCurrentAction("Background information recorded.");
            } else {
                myGUI.displayCurrentAction("Background not chosen. Unable to accept.");
            }// else
        }// actionPerformed
    }// BkCharacterizer

    public void logImageData() {
        if (!unsavedDataOnCurrentImg) {
            return;
        }
        DataWriter dataWriter = Nepic.dWriter;
        for (PageInfo page : pages) {
            if (page != null && page.isValid()) {
                dataWriter.addDataRow(page.getCsvData());
            }
        }
        unsavedDataOnCurrentImg = false;
    }

    public class ViewHistHandler extends ButtonHandler {
        private Histogram hist;

        public ViewHistHandler(String name, Histogram hist) {
            super(name);
            this.hist = hist;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            JOptionPane.showMessageDialog(myGUI, new HistogramViewPanel(hist, 350, 2));
        }
    }

    public class ViewGraphHandler extends ButtonHandler {
        private GraphData data;

        public ViewGraphHandler(String name, GraphData data) {
            super(name);
            this.data = data;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Graph graph = new Graph(600, 400, 0x000000).setData(data);
            graph.redraw(true /* connectTheDots */, true /* inScaleX */, false /* inScaleY */);
            JOptionPane.showMessageDialog(myGUI, graph);
        }
    }

    // *********************************************************************************************
    // Change Current Page
    // *********************************************************************************************

    // Note: NextPageHandler == IncrementPageHandler(1), PrevPageHandler == IncrementPageHandler(-1)
    public class IncrementPageHandler implements ActionListener {
        int incrementFactor;

        public IncrementPageHandler(int amntIncrementBy) {
            Verify.argument(amntIncrementBy != 0);
            incrementFactor = amntIncrementBy;
        }// IncrementPageHandler

        @Override
        public void actionPerformed(ActionEvent e) {
            incrementPage(incrementFactor);
        }// actionPerformed
    }// IncrementPageHandler

    private boolean incrementPage(int numToIncrement) {
        int newPgNum = currPgNum + numToIncrement;
        boolean continueToNextPage = true;
        int totNumPgs = pages.getNumPages();
        if (newPgNum < 0) {
            if (myGUI.userAgrees("Reached First Page",
                    "You have reached the first page in the image.\n"
                            + "Would you like to go to the last page?")) {
                newPgNum = totNumPgs - 1;
            } else {
                newPgNum -= numToIncrement;
                continueToNextPage = false;
            }
        } else if (newPgNum >= totNumPgs) {
            if (myGUI.userAgrees("Reached Last Page",
                    "You have reached the last page in the image.\n"
                            + "Would you like to return to the first page?")) {
                newPgNum = 0;
            } else {
                newPgNum -= numToIncrement;
                continueToNextPage = false;
            }
        }

        if (continueToNextPage) {
            clickLoc = null;
            dragLoc = null;
            updateDisplayedPage(newPgNum);
        }

        return continueToNextPage;
    }

    // *********************************************************************************************
    // User Interactions with GUI
    // *********************************************************************************************

    public class ClickHandler implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            clickLoc = null;
            dragLoc = null;
            myGUI.erase(Nepic.MOUSE_ACTION_ID);

            if (e.getButton() == MouseEvent.BUTTON3) {
                Point clickPt = e.getPoint();
                if (currPg != null && currPg.contains(clickPt)) {
                    OneDimensionalScanner scanner =
                            new OneDimensionalScanner(currPg, new Line(clickPt, 0));

                    // TODO: make a panel where can vary the medianGroupSize in the scanner, and see
                    // directly on the graph.
                    JOptionPane.showMessageDialog(myGUI, new ScannerGroupSizeVarierPanel(scanner),
                            "Scanline Graph",
                            JOptionPane.PLAIN_MESSAGE, null);
                }
            }
        }// mouseClicked

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }// mouseExited

        @Override
        public void mousePressed(MouseEvent e) {
            clickLoc = e.getPoint();
        }// mousePressed

        @Override
        public void mouseReleased(MouseEvent e) {
        }// mouseReleased
    }// ClickHandler

    public class DragHandler implements MouseMotionListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (currPg != null) {
                dragLoc = e.getPoint();
                int[] picDims = currPg.getDimensions();
                if (clickLoc.x != dragLoc.x && clickLoc.y != dragLoc.y && dragLoc.x >= 0
                        && dragLoc.y >= 0 && dragLoc.x < picDims[0] && dragLoc.y < picDims[1]) {
                    Polygon newRec = new Polygon(new Point[] {
                            clickLoc,
                            new Point(clickLoc.x, dragLoc.y),
                            dragLoc,
                            new Point(dragLoc.x, clickLoc.y) });
                    DataSet mouseActionPixels = new UnorderedDataSet();
                    mouseActionPixels.addAll(newRec.getEdges());
                    mouseActionPixels.setRgb(Nepic.MOUSE_ACTION_COLOR);
                    myGUI.draw(Nepic.MOUSE_ACTION_ID, mouseActionPixels);
                } else {
                    myGUI.erase(Nepic.MOUSE_ACTION_ID);
                }
            }// if pic exists
        }// mouseDragged

        @Override
        public void mouseMoved(MouseEvent e) {
        }// mouseMoved
    }// DragHandler

    // *********************************************************************************************
    // Save CSV Data
    // *********************************************************************************************

    public class SaveDataHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (saveData()) {
                myGUI.enableSaveData(false);
            }
        }// actionPerformed
    }// ChooseFileHandler

    private boolean saveData() {
        if (unsavedDataOnCurrentImg) {
            logImageData();
        }

        File whereToSave = myGUI.selectCsvSaveLocation();
        if (canSaveData(whereToSave)) {
            // Generate the data to save

            // Save the data
            boolean dataSaved = Nepic.dWriter.saveData(whereToSave);
            if (dataSaved) {
                if (myGUI.userAgrees("Open Data File?",
                        "Would you like to open the data file you just saved?")) {
                    openDataFile(whereToSave);
                }
            } else {
                Nepic.log(EventType.ERROR, "Unable to save data to given file.");
            }
            return dataSaved;
        }
        return false;
    }// saveData

    private void openDataFile(File toOpen) {
        try {
            Desktop.getDesktop().open(toOpen);
        } catch (IOException e) {
            Nepic.log(EventType.ERROR, "An error occurred while trying to open "
                    + toOpen.getAbsolutePath(), EventLogger.formatException(e));
        }
    }

    private boolean canSaveData(File whereToSave) {
        if (whereToSave == null || !Nepic.dWriter.canSaveData(whereToSave)) {
            return false;
        }
        if (whereToSave.exists()) {
            if (whereToSave.canWrite()) {
                int overwrite = JOptionPane.showConfirmDialog(myGUI,
                        "The indicated file already exists. Would you like to overwrite it?",
                        "Overwrite File?", JOptionPane.YES_NO_OPTION);
                return (overwrite == JOptionPane.YES_OPTION);
            }
            return false;
        }
        return true;
    }

    // *********************************************************************************************
    // Exit Program
    // *********************************************************************************************

    public class ExitHandler extends WindowAdapter implements ActionListener {
        @Override
        public void windowClosing(WindowEvent e) {
            exit();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }

        private void exit() {
            boolean canClose = saveDataIfNecessary();
            if (canClose) {
                myGUI.close();
            }
        }

        private boolean saveDataIfNecessary() {
            if ((Nepic.dWriter.dataLogged() || unsavedDataOnCurrentImg)
                    && myGUI.userAgrees("Save Data",
                            "Do you want to save the data generated since starting "
                                    + Nepic.APP_NAME + "?")) {
                return saveData();
            }
            return true;
        }

    }

    // *********************************************************************************************
    // Main Method
    // *********************************************************************************************

    public static void main(String args[]) {
        @SuppressWarnings("unused")
        Tracker myAppF = new Tracker();
    }// main

}