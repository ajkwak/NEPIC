package nepic.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import nepic.data.DataSet;
import nepic.util.Verify;

public class AnnotatableImageLabel extends JLabel {
    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = 6310500552214650433L;
    private AnnotatableImage img;

    /**
     * Constructs an {@link AnnotatableImageLabel} with the given background color.
     */
    public AnnotatableImageLabel(Color bkColor) {
        super();
        setHorizontalAlignment(JLabel.LEFT);
        setVerticalAlignment(JLabel.TOP);
        setOpaque(true);
        setBackground(bkColor);
    }

    public void displayImage(BufferedImage img) {
        this.img = img != null ? new AnnotatableImage(img) : null;
        setIcon(new ImageIcon(img));
    }

    public void annotateImage(int annotationId, DataSet first, DataSet... rest) {
        Verify.state(img != null, "No image to annotate");
        img.annotate(annotationId, first, rest);
        repaint();
    }

    public void recolorImageAnnoation(int annotationId, int rgb) {
        Verify.state(img != null, "No image to annotate");
        img.recolorAnnotation(annotationId, rgb);
        repaint();
    }

    public void eraseImageAnnotation(int annotationId) {
        Verify.state(img != null, "No image to annotate");
        img.eraseAnnotation(annotationId);
        repaint();
    }

    public void removeImageAnnotations() {
        Verify.state(img != null, "No image to annotate");
        img.clear();
        repaint();
    }

}
