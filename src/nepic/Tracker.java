package nepic;

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

import nepic.data.DataSet;
import nepic.data.GraphData;
import nepic.data.MutableDataSet;
import nepic.gui.Graph;
import nepic.gui.HistogramViewPanel;
import nepic.gui.Interface;
import nepic.image.ConstraintMap;
import nepic.image.ImagePage;
import nepic.image.MultiPageImageInfo;
import nepic.image.PageInfo;
import nepic.io.DataWriter;
import nepic.io.Files;
import nepic.io.TiffOpener;
import nepic.logging.EventLogger;
import nepic.logging.EventType;
import nepic.roi.Background;
import nepic.roi.BackgroundConstraint;
import nepic.roi.BackgroundFinder;
import nepic.roi.CellBody;
import nepic.roi.CellBodyConstraint;
import nepic.roi.CellBodyFinder;
import nepic.roi.DataScanner;
import nepic.data.Histogram;
import nepic.geo.Blob;
import nepic.geo.LineSegment;
import nepic.geo.Polygon;
import nepic.util.Pair;
import nepic.util.Range;
import nepic.util.Verify;

/**
 * Main class for the the Neuron-to-Environment Pixel Intensity Calculator. Controls and coordinates
 * the finding and tracking of Regions of Interest (ROIs) and the display of the resulting data to
 * the user.
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
    private final BackgroundFinder bkFinder;
    private final CellBodyFinder cbFinder;
    private MultiPageImageInfo pages;
    private boolean unsavedDataOnCurrentImg = false;

    // From last page.img: Use for tracking
    private PageInfo prevPgInfo = null;

    // Current page.img
    private ImagePage currPg = null;
    private int currPgNum = -1; // Start with invalid number!
    private CellBody cbCand = null;
    private Background bkCand = null;

    boolean bkCorrected; // True if the background correction has been done for this page.img

    public Tracker() {
        myGUI = new Interface(ImagePage.MAX_CAND_ID, new ExitHandler(), new ChooseFileHandler(),
                new SaveDataHandler(), new ToggleImageContrastHandler(),
                new IncrementPageHandler(-1), new IncrementPageHandler(1),
                new ClickHandler(), new DragHandler(), new BkCharacterizer(), new CBFinder(),
                new EnlargeCandHandler(), new ShrinkCandHandler(), new AcceptRoiHandler());
        Nepic.getEventLogger().registerObserver(myGUI);
        myOpener = new TiffOpener();
        cbFinder = new CellBodyFinder();
        bkFinder = new BackgroundFinder();
        Nepic.log(EventType.INFO, Nepic.getName() + " successfully initialized.");
        myGUI.displayCurrentAction("Please select image to analyze.");
    }

    // *********************************************************************************************
    // File Selection
    // *********************************************************************************************

    private boolean selectFileToAnalyze() {
        File fileToAnal = myGUI.selectTiffFile();
        if (fileToAnal == null) { // Keep the current file being analyzed.
            return false;
        }
        analFileClassPath = fileToAnal.getAbsolutePath();
        return true;
    }

    // *********************************************************************************************
    // Load Image File
    // *********************************************************************************************

    private class ChooseFileHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectFileToAnalyze()) {
                loadAndDisplayTiff(analFileClassPath);
            }
        }
    }

    private boolean loadAndDisplayTiff(String classpath) {
        if (myOpener.loadTiffInfo(classpath)) {
            int totNumPgs = myOpener.getNumPagesInTiff();
            updateImageBeingAnalyzed(totNumPgs);
            myGUI.setTitle(new StringBuilder(Nepic.getName())
                    .append(' ')
                    .append(Nepic.getMainVersion())
                    .append(" (")
                    .append(Files.getName(analFileClassPath))
                    .append(")")
                    .toString());
            updateDisplayedPage(0); // open the first page of the image
            return true;
        }
        Nepic.log(EventType.ERROR, "Unable to load image from the given classpath: " + classpath);
        return false;
    }

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
        updatePage(Files.getName(analFileClassPath), pgNum, imageBeingAnalyzed);
        paintCurrentPage();
        myGUI.clearDisplayedActions();
        Nepic.log(EventType.INFO, "Page " + (pgNum + 1) + " displayed.");
    }

    private void paintCurrentPage() {
        myGUI.setPage(currPgNum, pages.getNumPages(),
                currPg.displayImg(myGUI.shouldEqualizeHistogram()));
        redrawCbCand();
        redrawBkCand();
    }

    public boolean updatePage(String imgName, int pgNum, ImagePage page) {
        Verify.notNull(imgName, "Image name cannot be null.");
        pages.verifyPageNumLegal(pgNum);
        Verify.notNull(page, "ImagePage to update cannot be null.");

        // Set images on ROI finders
        cbFinder.setImage(page);
        bkFinder.setImage(page);

        bkCand = null;
        cbCand = null;

        // Make PageInfo for current page
        currPgNum = pgNum;
        currPg = page;
        PageInfo currPageInfo = pages.getPage(pgNum);
        boolean hasValidRois = false;
        if (currPageInfo == null) { // What if 'page' does not match currPageInfo when non-null?
            currPageInfo = new PageInfo(imgName, pgNum, page);
            pages.setPage(currPageInfo);
        } else {
            if (currPageInfo.hasValidBK()) {
                bkCand = currPageInfo.getBK();
                bkFinder.restoreFeature(bkCand);
                hasValidRois = true;
            }
            if (currPageInfo.hasValidCB()) {
                cbCand = currPageInfo.getCB();
                cbFinder.restoreFeature(cbCand);
                hasValidRois = true;
            }
        }
        prevPgInfo = tryFindPrevPageInfo(3); // Look in the last 3 pages for a previous PageInfo
        return hasValidRois;
    }

    /**
     *
     * @param numPgsSearch the number of pages before the previous page to go back and search for
     *        for a valid {@link PageInfo} with valid ROI candidates.
     * @return
     */
    private PageInfo tryFindPrevPageInfo(int numPgsSearch) {
        Verify.argument(numPgsSearch > 0,
                "Number of pages to search for previous page must be a positive integer");
        for (int prevPgNum = currPgNum - 1; prevPgNum > -1 && prevPgNum >= currPgNum - numPgsSearch; prevPgNum--) {
            PageInfo prevPgInfo = pages.getPage(prevPgNum);
            if (prevPgInfo != null && prevPgInfo.hasValidRois()) {
                return prevPgInfo;
            }
        }
        return null;
    }

    // *********************************************************************************************
    // Find Background
    // *********************************************************************************************

    public class BkCharacterizer extends TitledActionListener {
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
                if (userAcceptAsBackground(bkArea)) {
                    Nepic.log(EventType.INFO, "Background information recorded.", bkArea);
                }
                redrawBkCand();
            } else {
                myGUI.displayCurrentAction("Background not chosen.  Unable to accept.");
            }
        }
    }

    public boolean userAcceptAsBackground(Polygon p) {
        Verify.state(currPg != null, "Cannot accept background on null image page");

        ConstraintMap<BackgroundConstraint<?>> map = new ConstraintMap<BackgroundConstraint<?>>()
                .addConstraints(new BackgroundFinder.BackgroundArea(p));

        if (bkCand == null) {
            if (cbCand != null) {
                LineSegment cbLength = cbCand.getArea().getMaxDiameter();

                map.addConstraints(new BackgroundFinder.Origin(cbLength.getMidPoint()),
                        new BackgroundFinder.CurrTheta(cbLength.getAngleFromX()));
            }
            bkCand = bkFinder.createFeature(map);
        } else {
            bkFinder.editFeature(bkCand, map);
        }

        if (bkCand != null && bkCand.getArea() != null) {
            return true;
        }
        return false;
    }

    // *********************************************************************************************
    // Find CellBody
    // *********************************************************************************************

    public class CBFinder extends TitledActionListener {
        public CBFinder() {
            super("Find Cell Body");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean redrawBK; // TODO: what if CB doesn't exist
            if (clickLoc == null || dragLoc == null) {
                redrawBK = findCB();
            } else {
                myGUI.erase(Nepic.MOUSE_ACTION_ID);
                Polygon secCorners = new Polygon(new Point[] {
                        clickLoc,
                        new Point(dragLoc.x, clickLoc.y),
                        dragLoc,
                        new Point(clickLoc.x, dragLoc.y) });
                redrawBK = findCB(secCorners);
            }
            if (redrawBK) {
                Nepic.log(EventType.INFO, "Found CellBody candidate.  MinPi = "
                        + cbCand.getMinPi());
                redrawCbCand();
                redrawBkCand();
            } else {
                myGUI.displayCurrentAction("Unable to find cell body in indicated region.");
            }
        }
    }

    /**
     *
     * @return true if background updated, otherwise false
     */
    public boolean findCB() {
        Point[] polygonCorners = new Point[] {
                new Point(0, 0),
                new Point(currPg.width - 1, 0),
                new Point(currPg.width - 1, currPg.height - 1),
                new Point(0, currPg.height - 1) };
        return findCB(new Polygon(polygonCorners));
    }

    /**
     *
     * @param corners
     * @return true if background updated, otherwise false
     */
    public boolean findCB(Polygon corners) {
        ConstraintMap<CellBodyConstraint<?>> cbConstraints = new ConstraintMap<CellBodyConstraint<?>>()
                .addConstraints(new CellBodyFinder.SeedPolygon(corners));
        if (prevPgInfo != null && prevPgInfo.hasValidCB()) {
            int desiredSize = prevPgInfo.getCB().getArea().getSize();
            cbConstraints.addConstraints(new CellBodyFinder.DesiredSize(Pair.newPair(desiredSize,
                    CellBodyFinder.SizeEdgeCase.AS_CLOSE_AS_POSSIBLE)));
        }

        if (cbCand != null) { // Then must edit the current cand
            myGUI.erase(cbCand.getId());
            cbFinder.removeFeature(cbCand);
            // TODO: relase ROI number!
        }
        cbCand = cbFinder.createFeature(cbConstraints);

        if (bkCand != null) {
            LineSegment cbLength = cbCand.getArea().getMaxDiameter();

            ConstraintMap<BackgroundConstraint<?>> bkConstraints
            = new ConstraintMap<BackgroundConstraint<?>>().addConstraints(
                    new BackgroundFinder.Origin(cbLength.getMidPoint()),
                    new BackgroundFinder.CurrTheta(cbLength.getAngleFromX()));

            bkFinder.editFeature(bkCand, bkConstraints);
        }
        return cbCandValid();
    }

    // *********************************************************************************************
    // Modify CellBody
    // *********************************************************************************************

    public class EnlargeCandHandler extends TitledActionListener {
        public EnlargeCandHandler() {
            super("Enlarge Candidate");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (cbCand != null) {
                changeCbCandSize(true);
                redrawCbCand();
                redrawBkCand();
            } else {
                myGUI.displayCurrentAction("Cannot enlarge a candidate until has been selected.");
            }
        }
    }

    public class ShrinkCandHandler extends TitledActionListener {
        public ShrinkCandHandler() {
            super("Shrink Candidate");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (cbCand != null) {
                changeCbCandSize(false);
                redrawCbCand();
            } else {
                myGUI.displayCurrentAction("Cannot shrink a candidate until "
                        + "candidate has been selected.");
            }
        }
    }

    public void changeCbCandSize(boolean enlarge) {
        int desiredSize = cbCand.getArea().getSize();
        Pair<Integer, CellBodyFinder.SizeEdgeCase> constraint;
        if (enlarge) {
            constraint = Pair.newPair(desiredSize + 1, CellBodyFinder.SizeEdgeCase.BIGGER);
        } else { // shrink
            constraint = Pair.newPair(desiredSize - 1, CellBodyFinder.SizeEdgeCase.SMALLER);
        }
        ConstraintMap<CellBodyConstraint<?>> constraints = new ConstraintMap<CellBodyConstraint<?>>()
                .addConstraints(new CellBodyFinder.DesiredSize(constraint));
        cbFinder.editFeature(cbCand, constraints);
    }

    // *********************************************************************************************
    // Accept ROIs
    // *********************************************************************************************

    public class AcceptRoiHandler extends TitledActionListener {
        Point midpoint = new Point(200, 200);
        double theta = 0;
        final double phi = Math.PI / 12;

        public AcceptRoiHandler() {
            super("Accept Candidate");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (acceptRoiCandidates()) {
                myGUI.enableSaveData(true);
                if (incrementPage(1)) {
                    if (!hasValidCandidates()) {
                        if (canTrackFromPrevPage()) {
                            if (trackFromPrevPage()) {
                                Nepic.log(EventType.INFO, "Found CellBody candidate.", "MinPi =",
                                        cbCand.getMinPi());
                            } else {
                                myGUI.displayCurrentAction("Unable to find cell body.  "
                                        + "Please re-indicate cell body location.");
                            }
                        }
                    }
                }
                redrawCbCand();
                redrawBkCand();
            } else {
                myGUI.displayCurrentAction("Must have valid background and cell"
                        + " body candidates before accepting.");
            }
        }
    }

    public boolean acceptRoiCandidates() {
        if (!hasValidCandidates()) {
            return false;
        }

        // Accept candidates
        PageInfo currPgInfo = pages.getPage(currPgNum);
        currPgInfo.setBK(bkCand);
        currPgInfo.setCB(cbCand);
        bkCand.setModified(false);
        cbCand.setModified(false);
        Verify.state(pages.getPage(currPgNum).hasValidRois(),
                "After setting ROIs, should be valid.");
        currPgInfo = pages.getPage(currPgNum);

        Nepic.log(EventType.INFO, "ROI candidates accepted.", "PI ratio =",
                currPgInfo.getPiRatio(), "CellBody: seedPix =", cbCand.getSeedPixel(), "minPi =",
                cbCand.getMinPi(), "Background: corners =", bkCand.getArea());

        // Remove candidates (since have already been accepted)
        myGUI.erase(bkCand.getId());
        myGUI.erase(cbCand.getId());
        bkFinder.removeFeature(bkCand);
        cbFinder.removeFeature(cbCand);
        bkCand = null;
        cbCand = null;
        Nepic.log(EventType.INFO,
                "ROI candidates successfully removed from image.  Image analysis complete.");
        unsavedDataOnCurrentImg = true;
        return true;
    }

    public void logImageData() {
        if (!unsavedDataOnCurrentImg) {
            return;
        }
        DataWriter dataWriter = Nepic.getDataWriter();
        for (PageInfo page : pages) {
            if (page != null && page.hasValidRois()) {
                dataWriter.addDataRow(page.getCsvData());
            }
        }
        unsavedDataOnCurrentImg = false;
    }

    public boolean cbCandValid() {
        return cbCand != null && cbCand.isValid();
    }

    public boolean bkCandValid() {
        return bkCand != null && bkCand.isValid();
    }

    public boolean hasValidCandidates() {
        return cbCandValid() && bkCandValid();
    }

    // *********************************************************************************************
    // Track ROIs
    // *********************************************************************************************

    /**
     *
     * @return true if the previous page had valid ROIs; otherwise false
     */
    public boolean canTrackFromPrevPage() {
        return prevPgInfo != null && prevPgInfo.hasValidRois();
    }

    /**
     * NOTE: Only tracks the ROIs that have not already been accepted by the user on this page.
     * <p>
     * NOTE: It is recommended that {@link Tracker#canTrackFromPrevPage()} is called (and returns
     * true) before this method to avoid throwing an {@link IllegalStateException}
     * </p>
     *
     * @return true if the CellBody was successfully tracked from the last page; otherwise false
     * @throws IllegalStateException if the previous page did not have valid ROIs
     */
    public boolean trackFromPrevPage() {
        Verify.state(canTrackFromPrevPage(),
                "Unable to track ROIs.  ROIs from previous page were not indicated or invalid.");

        // If already have a valid CB candidate on this page (no need to track CB)
        if (cbCandValid()) {
            if (!bkCandValid()) {
                trackBackground();
            }
            return true;
        }

        // If need to find the CB
        Blob prevCb = prevPgInfo.getCB().getArea();
        Polygon prevCbLoc = prevCb.getBoundingBox().asPolygon();
        int prevCbSize = prevCb.getSize();
        if (trackCbInGivenArea(prevCbLoc, prevCbSize)) {
            if (!bkCandValid()) {
                trackBackground();
            }
            return true;
        }
        Nepic.log(EventType.INFO,
                "Unable to find cell body in previous location.  Enlarging region for search");
        prevCbLoc = prevCbLoc.changeSize(2).getBoundingBox().getIntersectionWith(
                currPg.getBoundingBox()).asPolygon();
        if (trackCbInGivenArea(prevCbLoc, prevCbSize)) {
            if (!bkCandValid()) {
                trackBackground();
            }
            return true;
        }
        Nepic.log(EventType.INFO,
                "Unable to find cell body near previous location.  Checking entire image.");
        prevCbLoc = currPg.getBoundingBox().asPolygon();
        if (trackCbInGivenArea(prevCbLoc, prevCbSize)) {
            if (!bkCandValid()) {
                trackBackground();
            }
            return true;
        }
        return false;
    }

    private boolean trackCbInGivenArea(Polygon location, int prevCbSize) {
        if (findCB(location) && cbCand.isValid()) {
            int newCbSize = cbCand.getArea().getSize();
            return newCbSize >= (0.75 * prevCbSize) && newCbSize <= (1.5 * prevCbSize);
        }
        return false;
    }

    private void trackBackground() { // TODO : what about when want to track bk with invalid
                                     // candidate (so when make valid, will track bk properly)
        LineSegment cbLength = cbCand.getArea().getMaxDiameter();

        ConstraintMap<BackgroundConstraint<?>> bkConstraints
            = new ConstraintMap<BackgroundConstraint<?>>().addConstraints(
                    new BackgroundFinder.Origin(cbLength.getMidPoint()),
                    new BackgroundFinder.CurrTheta(cbLength.getAngleFromX()));

        bkConstraints.addConstraints(new BackgroundFinder.PrevTheta(prevPgInfo.getBK().getTheta()));
        bkCand = bkFinder.createFeature(bkConstraints);
    }

    // *********************************************************************************************
    // Visualize ROIs
    // *********************************************************************************************

    private boolean redrawCbCand() {
        if (cbCand != null) {
            DataSet cbCandPixels = new MutableDataSet();
            cbCandPixels.addAll(cbCand.getEdges());
            if (cbCand.isModified()) {
                cbCandPixels.setRgb(Nepic.CELL_BODY_CAND_COLOR);
            } else {
                cbCandPixels.setRgb(Nepic.CELL_BODY_COLOR);
            }
            myGUI.draw(cbCand.getId(), cbCandPixels);
            return true;
        }
        return false;
    }

    private boolean redrawBkCand() {
        if (bkCand != null) {
            DataSet bkCandPixels = new MutableDataSet();
            bkCandPixels.addAll(bkCand.getEdges());
            if (bkCand.isModified()) {
                bkCandPixels.setRgb(Nepic.BACKGROUND_CAND_COLOR);
            } else {
                bkCandPixels.setRgb(Nepic.BACKGROUND_COLOR);
            }
            myGUI.draw(bkCand.getId(), bkCandPixels);
            return true;
        }
        return false;
    }

    public class ViewHistHandler extends TitledActionListener {
        private Histogram hist;

        public ViewHistHandler(String name, Histogram hist) {
            super(name);
            this.hist = hist;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            JOptionPane.showMessageDialog(myGUI, new HistogramViewPanel(hist, 350, 2, new Range(0,
                    255)));
        }

    }

    public class ViewScanlineHandler extends TitledActionListener {
        private GraphData data;

        public ViewScanlineHandler(String name, DataScanner scanner) {
            super(name);
            this.data = scanner.getGraphData();
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            DataSet minPiData = new MutableDataSet();
            minPiData.add(new Point(data.getMinX(), cbCand.getMinPi()));
            minPiData.add(new Point(data.getMaxX(), cbCand.getMinPi()));
            data.setDataSet("minPI", minPiData, 0xffff00 /* yellow */);
            JOptionPane.showMessageDialog(myGUI,
                    new Graph(800, 600, 0x000000).setData(data).setYGridlineInterval(5).refresh());
            data.removeDataSet("minPI");
        }

    }

    // *********************************************************************************************
    // Change Current Page
    // *********************************************************************************************

    public class IncrementPageHandler implements ActionListener {
        int incrementFactor;

        public IncrementPageHandler(int amntIncrementBy) {
            Verify.argument(amntIncrementBy != 0, "Illegal incrementation amount: 0");
            incrementFactor = amntIncrementBy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            incrementPage(incrementFactor);
        }
    }

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

    public class ToggleImageContrastHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            paintCurrentPage();
        }
    }

    public class ClickHandler implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(currPg == null){
                return;
            }
            clickLoc = null;
            dragLoc = null;
            myGUI.erase(Nepic.MOUSE_ACTION_ID);

            if (e.getButton() == MouseEvent.BUTTON3) {
                Point clickPt = e.getPoint();
                if (currPg != null && currPg.contains(clickPt)) {
                    int roiId = currPg.getId(clickPt.x, clickPt.y);
                    if (ImagePage.candNumLegal(roiId)) {
                        if (cbCand != null && roiId == cbCand.getId()) {
                            myGUI.openJPopupMenu(e.getComponent(), e.getX(), e.getY(),
                                    new ViewHistHandler("View CB Hist", cbCand.getPiHist()),
                                    new ViewScanlineHandler("0 Deg", cbCand.getEdgeFinder(0)),
                                    new ViewScanlineHandler("45 Deg", cbCand.getEdgeFinder(1)),
                                    new ViewScanlineHandler("90 Deg", cbCand.getEdgeFinder(2)),
                                    new ViewScanlineHandler("135 Deg", cbCand.getEdgeFinder(3)));
                            // TODO: also allow users to see the scanlines.
                        } else if (bkCand != null && roiId == bkCand.getId()) {
                            myGUI.openJPopupMenu(e.getComponent(), e.getX(), e.getY(),
                                    new ViewHistHandler("View BK Hist", bkCand.getPiHist()),
                                    new ViewHistHandler("View BK Edge Hist", bkCand.getEdgeHist()));
                        } else {
                            myGUI.displayCurrentAction(
                                    "Unable to determine identity of clicked ROI");
                        }
                    } else {
                        myGUI.openJPopupMenu(e.getComponent(), e.getX(), e.getY(),
                                new ViewHistHandler("View Image Hist", pages
                                        .getPage(currPgNum)
                                        .getPiHist()));
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            clickLoc = e.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {}
    }

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
                    DataSet mouseActionPixels = new MutableDataSet();
                    mouseActionPixels.addAll(newRec.getEdges());
                    mouseActionPixels.setRgb(Nepic.MOUSE_ACTION_COLOR);
                    myGUI.draw(Nepic.MOUSE_ACTION_ID, mouseActionPixels);
                } else {
                    myGUI.erase(Nepic.MOUSE_ACTION_ID);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
    }

    // *********************************************************************************************
    // Save CSV Data
    // *********************************************************************************************

    public class SaveDataHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (saveData()) {
                myGUI.enableSaveData(false);
            }
        }
    }

    private boolean saveData() {
        if (unsavedDataOnCurrentImg) {
            logImageData();
        }

        File whereToSave = myGUI.selectCsvSaveLocation();
        if (canSaveData(whereToSave)) {
            // Save the data
            boolean dataSaved = Nepic.getDataWriter().saveData(whereToSave);
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
    }

    private void openDataFile(File toOpen) {
        try {
            Desktop.getDesktop().open(toOpen);
        } catch (IOException e) {
            Nepic.log(EventType.ERROR, "An error occurred while trying to open "
                    + toOpen.getAbsolutePath(), EventLogger.formatException(e));
        }
    }

    private boolean canSaveData(File whereToSave) {
        if (whereToSave == null || !Nepic.getDataWriter().canSaveData(whereToSave)) {
            return false;
        }
        if (whereToSave.exists()) {
            if (whereToSave.canWrite()) {
                int overwrite = JOptionPane.showConfirmDialog(myGUI,
                        "The indicated file already exists.  Would you like to overwrite it?",
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
            if ((Nepic.getDataWriter().dataLogged() || unsavedDataOnCurrentImg)
                    && myGUI.userAgrees("Save Data",
                            "Do you want to save the data generated since starting "
                                    + Nepic.getName() + "?")) {
                return saveData();
            }
            return true;
        }
    }
}