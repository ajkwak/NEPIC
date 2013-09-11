package nepic.gui;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import nepic.io.Label;
import nepic.roi.model.Histogram;
import nepic.util.Verify;

/**
 * 
 * @author AJ Parmidge
 * @since Nepic_Alpha_v1-0-2013-03-05
 * @version Nepic_Alpha_v1-1-2013-03-13
 * 
 */
public class HistogramViewPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Default
    private static final int histColor = 0xFF0000; // red

    private JLabel histImgL; // for displaying BufferedImage of histogram
    private BufferedImage histImg;
    private int histHeight;
    private int columnWidth;

    private JTextArea histInfoTA;

    public HistogramViewPanel(Histogram hist, int desiredHistHeight, int desiredColumnWidth) {
        Verify.notNull(hist, "Histogram to display cannot be null");
        Verify.argument(desiredHistHeight > 0, "Desired height of histogram must be positive");
        Verify.argument(desiredColumnWidth > 0, "Desired column width must be positive");
        histHeight = desiredHistHeight;
        columnWidth = desiredColumnWidth;

        int width = columnWidth * hist.getSize();
        histImg = new BufferedImage(width, histHeight, BufferedImage.TYPE_INT_RGB);
        histImgL = new JLabel(new ImageIcon(histImg));
        histImgL.setSize(width, histHeight);
        histImgL.setLocation(0, 0);
        add(histImgL);
        histImgL.setVisible(true);

        int xPos = 0;
        int maxNumDataPtsInColumn = hist.numDataAt(hist.getMode());
        // System.out.println("Print histogram.");
        for (int i = hist.getLowerBound(); i <= hist.getUpperBound(); i++) {
            int columnHeight = hist.numDataAt(i) * histHeight / maxNumDataPtsInColumn;
            // System.out.println(i + "\t" + hist.numDataAt(i));
            for (int x = xPos; x < xPos + columnWidth; x++) {
                for (int y = histHeight - 1; y > (histHeight - columnHeight); y--) {
                    histImg.setRGB(x, y, histColor);
                }
            }
            xPos += columnWidth;
        }
        histImgL.repaint();

        Label[] infoLabels = Histogram.getCsvLabels();
        Object[] info = hist.getCsvData();
        histInfoTA = new JTextArea();
        int histInfoTAHeight = 20 * infoLabels.length;
        histInfoTA.setSize(width - 10, histInfoTAHeight);
        histInfoTA.setLocation(5, histHeight + 10);
        histInfoTA.setEditable(false);
        histInfoTA.setVisible(true);
        histInfoTA.setBackground(this.getBackground());
        add(histInfoTA);
        for (int i = 0; i < infoLabels.length; i++) {
            histInfoTA.append(infoLabels[i].toString() + " = " + info[i].toString() + "\r\n");
        }

        setLayout(null);
        setBorder(UtilityMethods.PANEL_BORDERS);
        setSize(width + 3, histHeight + 20 + histInfoTAHeight);
        this.setMinimumSize(getSize());
        this.setPreferredSize(getSize());
        setVisible(true);
    }
}
