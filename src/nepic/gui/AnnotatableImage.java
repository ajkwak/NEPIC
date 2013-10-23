package nepic.gui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import nepic.data.DataSet;
import nepic.util.DoubleLinkRing;
import nepic.util.Pixel;
import nepic.util.Verify;

/**
 * @author AJ Parmidge
 */
public class AnnotatableImage {
    private BufferedImage img; // The image being annotated.
    private final Stack<Annotation> annotationStack; // The stack of annotations to the image.

    /**
     * Creates an empty {@code AnnotatableImage}.
     */
    public AnnotatableImage(BufferedImage img) {
        this.img = img;
        annotationStack = new Stack<Annotation>();
    }

    public BufferedImage getImage() {
        return img;
    }

    /**
     * Annotates the image with the given {@link ColoredPointList} information. If the annotation
     * specified by the given ID already contains any values, the annotation is erased before these
     * new values are applied.
     *
     * In other words, draws or re-draws the annotation on the image.
     *
     * @param id the unique ID of the annotation
     * @param newVals the pixels with which to annotate the image, and the colors to paint those
     *        pixels on the image
     */
    public void annotate(int id, DataSet first, DataSet... rest) {
        Verify.notNull(first, "DataSet");

        // Preprocess stack so we're ready to work with the desired annotation.
        Annotation toRedraw = workWithAnnotation(id);
        if (toRedraw == null) {
            toRedraw = new Annotation(id);
        } else {
            toRedraw.clear();
        }
        Verify.argument(toRedraw != null, "Cannot redraw annotation with invalid ID " + id);

        // Clear draw it with the given values.
        toRedraw.add(new MonochromePixelSet(first.getRgb()).addAll(first));
        for (DataSet dataSet : rest) {
            Verify.notNull(dataSet, "DataSet");
            toRedraw.add(new MonochromePixelSet(dataSet.getRgb()).addAll(dataSet));
        }
        push(toRedraw);
    }

    /**
     * Recolors the annotation with the given ID to the given color (in RGB format).
     *
     * @param id the unique id of the annotation to recolor.
     * @param rgb the color (in RGB format) to paint the annotation.
     * @throws IllegalStateException if the annotation to recolor is empty (i.e. if there is nothing
     *         to recolor)
     */
    public void recolorAnnotation(int id, int rgb) {
        // Preprocess stack so we're ready to work with the desired annotation.
        Annotation toRecolor = workWithAnnotation(id);
        Verify.argument(toRecolor != null, "Cannot recolor annotation with invalid ID " + id);

        // Clear the annotation and re-draw it with the given values.
        for (MonochromePixelSet pixelSet : toRecolor.data) {
            pixelSet.rgb = rgb;
            for (Pixel pix : pixelSet.pixels) {
                img.setRGB(pix.x, pix.y, rgb);
            }
        }
        push(toRecolor);
    }

    public void appendAnnotation(int id, DataSet first, DataSet... rest) {
        Verify.notNull(first, "DataSet");

        // Preprocess stack so we're ready to work with the desired annotation.
        Annotation toRedraw = workWithAnnotation(id);
        if (toRedraw == null) { // Create annotation to append to, if it doesn't already exist.
            toRedraw = new Annotation(id);
        }

        // Add the given values to the current annotation.
        toRedraw.add(new MonochromePixelSet(first.getRgb()).addAll(first));
        for (DataSet dataSet : rest) {
            Verify.notNull(dataSet, "DataSet");
            toRedraw.add(new MonochromePixelSet(dataSet.getRgb()).addAll(dataSet));
        }
        push(toRedraw);
    }

    public void eraseAnnotation(int id) {
        workWithAnnotation(id); // Erases the annotation, if it was there.
    }

    /**
     * Erases all of the annotations currently drawn on the image.
     */
    public void clear() {
        while (!annotationStack.empty()) {
            pop();
        }
    }

    private void push(Annotation annotation) {
        // Push the given Annotation onto the stack.
        annotationStack.push(annotation);

        // Draw the given Annotation onto the image.
        for (MonochromePixelSet dataSet : annotation.data) {
            int rgb = dataSet.rgb;
            for (Pixel pix : dataSet.pixels) {
                int x = pix.x;
                int y = pix.y;
                pix.relLum = img.getRGB(x, y); // The color to restore when erasing this pixel.
                img.setRGB(x, y, rgb);
            }
        }
    }

    private Annotation pop() {
        Annotation poppedAnnotation = annotationStack.pop();

        // Erase the popped annotation from the image (iterate BACKWARDS)
        Iterator<MonochromePixelSet> annotationItr = poppedAnnotation.data.reverseIterator();
        while (annotationItr.hasNext()) {
            Iterator<Pixel> reversePixItr = annotationItr.next().pixels.reverseIterator();
            while (reversePixItr.hasNext()) {
                Pixel pixToRestore = reversePixItr.next();
                img.setRGB(pixToRestore.x, pixToRestore.y, pixToRestore.relLum);
            }
        }
        return poppedAnnotation;
    }

    /**
     * Removes the desired annotation from the stack (simultaneously erasing it from the image), so
     * that it may be worked with without affecting any other annotation in the stack.
     *
     * @param id the ID of the desired annotation
     * @return the annotation with the given {@code id} if it exists; otherwise {@code null}
     */
    private Annotation workWithAnnotation(int id) {
        Annotation desiredAnnotation = null;
        Stack<Annotation> otherStack = new Stack<Annotation>();

        // Erase all annotations up through the desired annotation in the stack.
        while (!annotationStack.empty() && desiredAnnotation == null) {
            Annotation poppedAnnotation = pop();
            if (poppedAnnotation.id == id) {
                desiredAnnotation = poppedAnnotation;
            } else {
                otherStack.push(poppedAnnotation);
            }
        }

        // Restore all annotations that were previously above the desired annotation in the stack.
        while (!otherStack.empty()) {
            push(otherStack.pop());
        }

        // The desired annotation is no longer in the stack (if it exists)
        return desiredAnnotation;
    }

    private class Annotation {
        private final int id; // The unique id of the annotation
        private final DoubleLinkRing<MonochromePixelSet> data;

        private Annotation(int id) {
            this.id = id;
            data = new DoubleLinkRing<MonochromePixelSet>();
        }

        private void add(MonochromePixelSet toRestore) {
            data.addLast(toRestore);
        }

        private void clear() {
            data.clear();
        }
    }

    private class MonochromePixelSet {
        private int rgb;
        private final DoubleLinkRing<Pixel> pixels;

        private MonochromePixelSet(int rgb) {
            this.rgb = rgb;
            pixels = new DoubleLinkRing<Pixel>();
        }

        private MonochromePixelSet add(Point pt) {
            pixels.addLast(new Pixel(pt.x, pt.y));
            return this;
        }

        private MonochromePixelSet addAll(Collection<Point> pts) {
            for (Point pt : pts) {
                add(pt);
            }
            return this;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Stack<Annotation> otherStack = new Stack<Annotation>();
        while (!annotationStack.empty()) {
            Annotation a = annotationStack.pop();
            otherStack.push(a);
            builder.append(a.id).append(" ").append(a.data).append('\n');
        }
        while (!otherStack.empty()) {
            annotationStack.push(otherStack.pop());
        }
        return builder.toString();
    }

    public BufferedImage zoom(int zoomFactor) {
        int origWidth = img.getWidth();
        int origHeight = img.getHeight();
        BufferedImage zoomedImg = new BufferedImage(origWidth * zoomFactor,
                origHeight * zoomFactor, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < origWidth; x++) {
            for (int y = 0; y < origHeight; y++) {
                int color = img.getRGB(x, y);
                // Color the relevant portion of the zoomed image.
                for (int zoomedX = zoomFactor * x; zoomedX < zoomFactor * (x + 1); zoomedX++) {
                    for (int zoomedY = zoomFactor * y; zoomedY < zoomFactor * (y + 1); zoomedY++) {
                        zoomedImg.setRGB(zoomedX, zoomedY, color);
                    }
                }
            }
        }
        return zoomedImg;
    }

    /**
     * Returns the width of the image being annotated.
     */
    public int getWidth() {
        return img.getWidth();
    }

    /**
     * Returns the height of the image being annotated.
     */
    public int getHeight() {
        return img.getHeight();
    }

    // public static void main(String[] args) {
    // AnnotatableImage ai = new AnnotatableImage(5);
    // ai.setImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));
    // ai.redraw(2, new ColoredPointList(Lists.newArrayList(new Point(0, 0), new Point(1, 1)),
    // 0xffffff));
    // ai.redraw(4, new ColoredPointList(Lists.newArrayList(new Point(0, 0)), 0xffffff));
    // ai.redraw(0, new ColoredPointList(Lists.newArrayList(new Point(1, 1)), 0xffffff));
    // ai.redraw(2, new ColoredPointList(Lists.newArrayList(new Point(15, 15)), 0x333333));
    // ai.erase(1);
    // ai.erase(1);
    // ai.erase(3);
    // ai.erase(2);
    // ai.erase(4);
    // ai.recolor(4, 0x333333);
    // System.out.println(ai);
    // System.out.println("stackTop = " + ai.stackTop);
    // }
}